import java.rmi.registry.LocateRegistry; 
import java.rmi.registry.Registry;  

public class ClientInterface {
    //Usage: java ClientInterface ID (ID do Peer a chamar)
    public static void main(String[] args) {
        try {
            // Getting the registry 
            Registry registry = LocateRegistry.getRegistry(null);

            PeerInterface interfaceStub = (PeerInterface) registry.lookup("Peer" + args[0]);
            switch (args[1].toUpperCase()) {
                case "BACKUP":
                    interfaceStub.backup();
                    break;
                case "SHUTDOWN":
                    interfaceStub.shutdown();
                default:
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("Client Interface exception: " + e.toString()); 
            e.printStackTrace(); 
        }
    }
}