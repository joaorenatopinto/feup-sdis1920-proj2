import java.math.BigInteger;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientInterface {
    // Usage: java ClientInterface ID (ID do Peer a chamar) Protocol :
    //              java ClientInterface ID Backup File_Path Replication_Degree
    //              java ClientInterface ID Shutdown
    //              java ClientInterface ID Restore File_Path
    public static void main(String[] args) {
        try {
            // Getting the registry
            Registry registry = LocateRegistry.getRegistry(null);

            PeerInterface interfaceStub = (PeerInterface) registry.lookup("Peer" + args[0]);
            switch (args[1].toUpperCase()) {
                case "BACKUP":
                    interfaceStub.backup(args[2], Integer.parseInt(args[3]));
                    break;
                case "SHUTDOWN":
                    interfaceStub.shutdown();
                case "FINDSUCCESSOR":
                    interfaceStub.findSuccessorTest(new BigInteger(args[2]));
                default:
                    break;
            }

        } catch (Exception e) {
            System.err.println("Client Interface exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
