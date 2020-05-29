
import java.io.IOException;
import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface PeerInterface extends Remote {
  public void backup(String path, int repDegree) throws RemoteException;

  public void shutdown() throws RemoteException;

  public void findSuccessorTest(BigInteger id) throws RemoteException, NoSuchAlgorithmException;

  public void restore(String path) throws RemoteException;

  public void delete(String path) throws RemoteException;

  public void spaceReclaim(long newMaxStorage) throws RemoteException, IOException;

  public void printState() throws RemoteException;
}
