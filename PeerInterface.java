import java.rmi.*;

public interface PeerInterface extends Remote {
    void backup() throws RemoteException;
    void shutdown() throws RemoteException;
}