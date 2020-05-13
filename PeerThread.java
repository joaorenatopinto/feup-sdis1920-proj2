import java.io.IOException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class PeerThread implements Runnable {
    public final Peer peer;

    public PeerThread (Peer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {
        SSLServerSocket serverSocket = null;
        SSLServerSocketFactory ssf = null;

        ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try {
            serverSocket = (SSLServerSocket) ssf.createServerSocket(Peer.portNumber);
        } catch (final IOException e) {
            System.out.println("Server - Failed to create SSLServerSocket");
            e.getMessage();
            return;
        }
        try {
            //System.out.println("Waiting Connection...");
            while (true) {
                // waits for a connection to occur and creates a Message Processor task and gives it to ThreadPool
                final SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                //System.out.println("Recebi cenas");
                final Runnable task = new MessageProcessor(clientSocket);
                Peer.pool.execute(task); 
            }
        } catch (final IOException e) {
                e.printStackTrace();
            }
    }
}