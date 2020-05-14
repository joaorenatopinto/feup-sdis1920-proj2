import java.math.BigInteger;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientInterface {
    // Usage: java ClientInterface ID (Id of Peer To Call) Protocol :
    //              java ClientInterface ID Backup File_Path Replication_Degree
    //              java ClientInterface ID Shutdown
    //              java ClientInterface ID Restore File_Path
    public static void main(String[] args) {

        if(!((args.length == 3 && args[1].equalsIgnoreCase("RESTORE")) || (args.length == 4 && args[1].equalsIgnoreCase("BACKUP")))){
            System.err.println("Usage: java ClientInterface <PeerID>  <Protocol> :");
            System.err.println("   Backup protocol: ClientInterface <PeerID> Backup <File_Path> <Replication_Degree>");
            System.err.println("   Restore protocol: ClientInterface <PeerID> Restore <File_Path>");
            System.exit(-1);
        }
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
