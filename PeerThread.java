public class PeerThread implements Runnable {
  public Peer peer;

  public PeerThread(Peer peer) {
    this.peer = peer;
  }

  @Override
  public void run() {
    SSLEngineServer server;

    try {
      server = new SSLEngineServer("server","123456",Peer.portNumber);

    } catch ( SSLManagerException e) {
      System.out.println("Server - Failed to create SSLEngineServer\n" + e.getMessage());
      return;
    }
    try {
      // System.out.println("Waiting Connection...");
      while (true) {
        // waits for a connection to occur and creates a Message Processor task and
        // gives it to ThreadPool
        SSLServerInterface serverInterface = server.accept();
        Runnable task = new MessageProcessor(serverInterface);
        Peer.pool.execute(task);
      }
    } catch (SSLManagerException e) {
      e.printStackTrace();
    }
  }
}
