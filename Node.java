import java.lang.FdLibm.Pow;
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

    private static int M = 49;

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

    private void joinRing(Node n) throws NoSuchAlgorithmException {
        this.predecessor = null;
        this.successor = n.findSuccessor(this.id);
    }

    public Node findSuccessor(BigInteger id) throws NoSuchAlgorithmException {
        if( id.equals(this.id ) ) {
            return this;
        }
        else if ( id.compareTo(this.id)==1 && id.compareTo(this.successor.id)==-1 ) {
            return this.successor;
        }
        else {
            return this.closestPreceedingNode(id);
        }
    }

    // TODO rever isto
    public Node closestPreceedingNode(BigInteger id) throws NoSuchAlgorithmException {
        for(int i = 0; i < this.finger_table.size() i++) {
            if( this.finger_table.get(i).id.compareTo(this.id) == 1 && (this.finger_table.get(i).id.compareTo(id)<=0  )  ) {
                return finger_table.get(i);
            }
        }
        return this;
    }

    public void stabilize() {
        Node n = this.successor.predecessor;

        if( n != null && !(this.id == n.id) && ( this.id == this.successor.id || ( n.id.compareTo(this.id)==1 && n.id.compareTo(this.successor.id)==-1 ) ) ) {
            this.successor = n;
            finger_table.set(0, n);
        }

        this.successor.notify(this);
    }

    public void fixFingers() {
        for(int i = 1; i < finger_table.size(); i++) {
            finger_table.set(i, findSuccessor( new BigInteger("" + i).add( new BigInteger("2").pow(i - 1) ) ) );
        }
    }

    public void notify(Node n) {
        if( this.predecessor == null || ( n.id.compareTo(this.predecessor.id)==1 && n.id.compareTo(this.id)==-1 ) ) {
            this.predecessor = n;
        }
    }

    public void checkPredecessor() {
        // é preciso comunicaçao entre nodes e assim. acho que n faz sentido implementar já
    }

    public static void main(String[] args) {
        try {
            Node node = new Node("193.136.33.132", 8000);
            System.out.println(node.id.toString().length() );
        } catch (NoSuchAlgorithmException e) {
            
            e.printStackTrace();
        }
    }
}