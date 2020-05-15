import java.math.BigInteger;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientInterface {
    public static void main(String[] args) {
        
        if (args.length == 3 && (args[1].equalsIgnoreCase("DELETE") || args[1].equalsIgnoreCase("RESTORE") || args[1].equalsIgnoreCase("RECLAIM"))){
            
        } else if (args.length == 4 && args[1].equalsIgnoreCase("BACKUP")){

        } else if (args.length == 2 && args[1].equalsIgnoreCase("STATE")){

        }
        else {
            System.err.println("Usage: java ClientInterface <PeerID>  <Protocol> :");
            System.err.println("   Backup protocol: ClientInterface <PeerID> Backup <File_Path> <Replication_Degree>");
            System.err.println("   Restore protocol: ClientInterface <PeerID> Restore <File_Path>");
            System.err.println("   Delete protocol: ClientInterface <PeerID> Delete <File_Path>");
            System.err.println("   Reclaim protocol: ClientInterface <PeerID> Reclaim <New_Max_Storage>");
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
                case "RESTORE":
                    interfaceStub.restore(args[2]);
                    break;
                case "DELETE":
                    interfaceStub.delete(args[2]);
                    break;
                case "RECLAIM":
                    try {
                        interfaceStub.space_reclaim(Long.parseLong(args[2]));
                    } catch (NumberFormatException e) {
                        System.out.println(e.getMessage());
                        System.err.println("<New_Max_Storage> must be a Number");
                        System.exit(-1);
                    }
                    break;
                case "STATE":
                    interfaceStub.print_state();
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
