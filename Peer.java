import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Peer {
    static public Node chordNode;
    static public boolean shutdown = false;
    static public String ipAddress = null;
    static public int portNumber;
    static public ScheduledExecutorService pool;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Peer peer = new Peer();
        peer.run(args);
    }

    public void run(String[] args) throws NoSuchAlgorithmException {
        System.setProperty("javax.net.ssl.trustStore", "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        portNumber = Integer.parseInt(args[1]);
        ipAddress = args[2];
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
         pool = Executors.newScheduledThreadPool(50);

        // Create task where stabilizes and notifies chord and gives it to ThreadPool to execute it every second
        final Runnable chordhandle = new ChordHandler(chordNode);
        pool.scheduleAtFixedRate(chordhandle, 20, 20, TimeUnit.SECONDS);
        // Create task where a thread permantly is listening to the server socket and gives it to the ThreadPool
        final Runnable listener = new PeerThread(this);
        pool.execute(listener);
    }
}   
