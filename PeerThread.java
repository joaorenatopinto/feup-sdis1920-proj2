import java.io.IOException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class PeerThread implements Runnable {
  public Peer peer;

  public PeerThread(Peer peer) {
    this.peer = peer;
  }

  @Override
  public void run() {
    SSLServerSocket serverSocket = null;
    SSLServerSocketFactory ssf = null;

    ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

    try {
      serverSocket = (SSLServerSocket) ssf.createServerSocket(Peer.portNumber);

    } catch (IOException e) {
      System.out.println("Server - Failed to create SSLServerSocket");
      e.getMessage();
      return;
    }
    try {
      // System.out.println("Waiting Connection...");
      while (true) {
        // waits for a connection to occur and creates a Message Processor task and
        // gives it to ThreadPool
        SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
        Runnable task = new MessageProcessor(clientSocket);
        Peer.pool.execute(task);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
