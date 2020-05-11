import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class PeerMethods implements PeerInterface {
    public void backup() {
        System.out.println("Remote method backup called.");

        return;
    }

    public void shutdown() {
        Peer.shutdown = true;
        return;
    }

    public void findSuccessorTest(BigInteger id) throws NoSuchAlgorithmException {
        System.out.println("AIODUBNAWIOUDBAWODBNAW=ODNAWODNAWODJNAWODNAOWD");
        NodeReference node = Peer.chordNode.findSuccessor(id);
        System.out.println("Node: " + node.ip + " " + node.port + " " + node.id);
    }
}