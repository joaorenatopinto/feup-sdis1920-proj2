import java.io.IOException;
import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface PeerInterface extends Remote {
  void backup(String path, int repDegree) throws RemoteException;

  void shutdown() throws RemoteException;

  void findSuccessorTest(BigInteger id) throws RemoteException, NoSuchAlgorithmException;

  void restore(String path) throws RemoteException;

  void delete(String path) throws RemoteException;

  void spaceReclaim(long newMaxStorage) throws RemoteException, IOException;

  void printState() throws RemoteException;
}
