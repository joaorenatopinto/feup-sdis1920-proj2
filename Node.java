import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Node {
    public BigInteger id;
    public String ip;
    public int port;

    Node successor;
    Node predecessor;

    public Node(String ip, int port) throws NoSuchAlgorithmException {
        this.ip = ip;
        this.port = port;
        String unhashedId = ip + ';' + Integer.toString(port);

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(unhashedId.getBytes());
        this.id = new BigInteger(1, messageDigest);
    }

    private void createRing() {
        predecessor = null;
        successor = this;
    }

    private void joinRing(Node n) {
        predecessor = null;
        successor = n.findSuccessor(this.id);
    }

    /*
    public Node findSuccessor(BigInteger id) {
        if()
    }*/

    public static void main(String[] args) {
        try {
            Node node = new Node("128.69.0.0", 8000);
            System.out.println(node.id.toString());
        } catch (NoSuchAlgorithmException e) {
            
            e.printStackTrace();
        }
    }
}