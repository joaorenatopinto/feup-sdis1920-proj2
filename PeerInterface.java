import java.io.IOException;
import java.math.BigInteger;
import java.rmi.*;
import java.security.NoSuchAlgorithmException;

public interface PeerInterface extends Remote {
    void backup(String path, int rep_degree) throws RemoteException;
    void shutdown() throws RemoteException;
    void findSuccessorTest(BigInteger id) throws RemoteException, NoSuchAlgorithmException;
	void restore(String path) throws RemoteException;
    void delete(String path) throws RemoteException;
	void space_reclaim(long new_max_storage) throws RemoteException, IOException;
	void print_state() throws RemoteException;
}
