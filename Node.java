import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Node {
    public BigInteger id;
    public String ip;
    public int port;

    Node successor;
    Node predecessor;
    List<Node> finger_table;

    public Node(String ip, int port) throws NoSuchAlgorithmException {
        this.ip = ip;
        this.port = port;
        this.finger_table = new ArrayList<>();

        String unhashedId = ip + ';' + Integer.toString(port);

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(unhashedId.getBytes());
        this.id = new BigInteger(1, messageDigest);
    }

    private void createRing() {
        this.predecessor = null;
        this.successor = this;
    }

    private void joinRing(Node n) {
        this.predecessor = null;
        this.successor = n.findSuccessor(this.id);
    }

    public Node findSuccessor(BigInteger id) {
        if(id == this.successor.id) {
            return successor;
        }
        else {
            Node n = this.closestPreceedingNode(id);
            return n.findSuccessor(id);
        }
    }

    public Node closestPreceedingNode(BigInteger id) {
        
    }

    public static void main(String[] args) {
        try {
            Node node = new Node("128.69.0.0", 8000);
            System.out.println(node.id.toString());
        } catch (NoSuchAlgorithmException e) {
            
            e.printStackTrace();
        }
    }
}