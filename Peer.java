import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import Storage.*;

public class Peer extends PeerMethods {
  static public Node chordNode;
  static public boolean shutdown = false;
  static public String ipAddress = null;
  static public int portNumber;
  static public ScheduledExecutorService pool;
  static public Storage storage;
  static public int id;

  public static void main(String[] args) throws NoSuchAlgorithmException {

    // java Peer 9 localhost 8009 JOIN localhost 8003

    Peer peer = new Peer();
    peer.run(args);
    id = Integer.parseInt(args[0]);
    storage = new Storage(id);
  }

  public void run(String[] args) throws NoSuchAlgorithmException {
    System.setProperty("javax.net.ssl.trustStore", "truststore");
    System.setProperty("javax.net.ssl.trustStorePassword", "123456");

    if (!((args.length == 4 && args[3].equalsIgnoreCase("CREATE"))
        || (args.length == 6 && args[3].equalsIgnoreCase("JOIN")))) {
      System.err.println("Usage: Peer <PeerID> <IpAddress> <PortNumber> <ChordOption> :");
      System.err.println("   Create Option: Peer <PeerID> <IpAddress> <PortNumber> create");
      System.err.println(
          "   Join Option: Peer <PeerID> <IpAddress> <PortNumber>  join <ChordMemberIpAddress> <ChordMemberPortNumber>");
      System.exit(-1);
    }
    try {
      ipAddress = args[1];
      portNumber = Integer.parseInt(args[2]);

      String chordOption = args[3];

      chordNode = new Node(ipAddress, portNumber, this);

      if (chordOption.equalsIgnoreCase("CREATE")) {
        try {
          LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
          System.err.println("ERROR: Failed to start RMI on port : 1099");
          System.exit(-1);
        }
        System.setProperty("javax.net.ssl.keyStore", "server.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        chordNode.create();
      } else if (chordOption.equalsIgnoreCase("JOIN")) {
        System.setProperty("javax.net.ssl.keyStore", "client.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        chordNode.join(args[4], Integer.parseInt(args[5]));
      } else {
        System.out.println("ERROR: Failed to initiate Peer.");
        return;
      }
    } catch (NumberFormatException e) {
      System.err.println("<PortNumber> must be a integer");
      System.exit(-1);
    }

    try {
      PeerMethods peer = new PeerMethods();

      PeerInterface interfaceStub = (PeerInterface) UnicastRemoteObject.exportObject(peer, 0);

      Registry registry = LocateRegistry.getRegistry();

      registry.bind("Peer" + args[0], interfaceStub);
    } catch (Exception e) {
      e.getStackTrace();
      return;
    }
    // System.err.println("Peer ready");
    pool = Executors.newScheduledThreadPool(50);

    // Create task where stabilizes and notifies chord and gives it to ThreadPool to
    // execute it every second
    Runnable chordhandle = new ChordHandler(chordNode);
    pool.scheduleAtFixedRate(chordhandle, 1, 5, TimeUnit.SECONDS);
    // Create task where a thread is permanently listening to the server socket and
    // gives it to the ThreadPool
    Runnable listener = new PeerThread(this);
    pool.execute(listener);
  }

}
