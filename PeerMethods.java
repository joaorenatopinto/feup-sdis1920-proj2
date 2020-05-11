import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PeerMethods implements PeerInterface {
    public void backup(String path) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(path.getBytes());
        BigInteger pathId = new BigInteger(1, messageDigest);
        System.out.println("Backing up file with ID: " + pathId);
        Peer.pool.execute(new Runnable() {
            public void run() {
                try {
                    Peer.backupFile(pathId);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
    }

    public void shutdown() {
        Peer.shutdown = true;
        return;
    }

    public void findSuccessorTest(BigInteger id) throws NoSuchAlgorithmException {
        System.out.println("AIODUBNAWIOUDBAWODBNAWODNAWODNAWODJNAWODNAOWD");
        NodeReference node = Peer.chordNode.findSuccessor(id);
        System.out.println("Node: " + node.ip + " " + node.port + " " + node.id);
    }
}