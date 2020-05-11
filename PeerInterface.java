import java.math.BigInteger;
import java.rmi.*;
import java.security.NoSuchAlgorithmException;

public interface PeerInterface extends Remote {
    void backup(String path) throws RemoteException, NoSuchAlgorithmException;
    void shutdown() throws RemoteException;
    void findSuccessorTest(BigInteger id) throws RemoteException, NoSuchAlgorithmException;
}