import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Peer {
    static public Node chordNode;
    static public boolean shutdown = false;

    public static void main(final String[] args) throws NoSuchAlgorithmException {
        final Peer peer = new Peer();
        peer.run(args);
    }

    public void run(final String[] args) throws NoSuchAlgorithmException {
        System.setProperty("javax.net.ssl.trustStore", "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        final int portNumber = Integer.parseInt(args[1]);
        final String ipAddress = args[2];
        final String chordOption = args[3];

        chordNode = new Node(ipAddress, portNumber, this);

        if (chordOption.equalsIgnoreCase("CREATE")) {
            System.setProperty("javax.net.ssl.keyStore", "server.keys");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
            chordNode.create();
        } else if (chordOption.equalsIgnoreCase("JOIN")) {
            System.setProperty("javax.net.ssl.keyStore", "client.keys");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
            chordNode.join(args[4], Integer.parseInt(args[5]));
        } else {
            System.out.println("vai te foder burro do caralho");
            return;
        }
        SSLServerSocket serverSocket = null;
        SSLServerSocketFactory ssf = null;

        ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try {
            serverSocket = (SSLServerSocket) ssf.createServerSocket(portNumber);
        } catch (final IOException e) {
            System.out.println("Server - Failed to create SSLServerSocket");
            e.getMessage();
            return;
        }
        try {
            final PeerMethods peer = new PeerMethods();

            final PeerInterface interfaceStub = (PeerInterface) UnicastRemoteObject.exportObject(peer, 0);

            final Registry registry = LocateRegistry.getRegistry();

            registry.bind("Peer" + args[0], interfaceStub);
        } catch (final Exception e) {
            e.getStackTrace();
            return;
        }
        System.err.println("Peer ready");
        final ScheduledExecutorService pool = Executors.newScheduledThreadPool(50);

        // Create task where stabilizes and notifies chord and gives it to ThreadPool to execute it every second
        final Runnable chordhandle = new ChordHandler(chordNode);
        pool.scheduleAtFixedRate(chordhandle, 1, 1, TimeUnit.SECONDS);


        try {
            System.out.println("Waiting Connection...");
            while (true) {
               
                //waits for a connection to occur and creates a Message Processor task and gives it to ThreadPool
                final SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("Recebi cenas");
                final Runnable task = new MessageProcessor(clientSocket);
                pool.execute(task);

                if (shutdown) {
                    System.out.println("Closing all sockets and threadPool...");
                    pool.shutdown();
                    clientSocket.close();
                    serverSocket.close();
                    System.out.println("Shuting down...");
                    System.out.println("Bye.");
                    break;
                }
            }
        } catch (final IOException e) {
                e.printStackTrace();
            }
    }
}   
