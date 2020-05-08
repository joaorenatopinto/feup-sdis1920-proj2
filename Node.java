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
    Node[] finger_table;

    private static int M = 6;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        try {
            Node node1 = new Node(1);
            Node node2 = new Node(8);
            Node node3 = new Node(14);
            Node node4 = new Node(21);
            Node node5 = new Node(32);
            Node node6 = new Node(38);
            Node node7 = new Node(42);
            Node node8 = new Node(48);
            Node node9 = new Node(51);
            Node node10 = new Node(56);

            node1.create();
            
            node2.join(node1);
            node2.stabilize();
            node1.stabilize();
            node2.fixFingers();
            node1.fixFingers();
            
            node3.join(node2);
            node3.stabilize();
            node2.stabilize();
            node1.stabilize();
            node3.fixFingers();
            node2.fixFingers();
            node1.fixFingers();

            node10.join(node1);
            node10.stabilize();
            node3.stabilize();
            node2.stabilize();
            node1.stabilize();
            node10.fixFingers();
            node3.fixFingers();
            node2.fixFingers();
            node1.fixFingers();

            node6.join(node3);
            node6.stabilize();
            node10.stabilize();
            node3.stabilize();
            node2.stabilize();
            node1.stabilize();
            node6.fixFingers();
            node10.fixFingers();
            node3.fixFingers();
            node2.fixFingers();
            node1.fixFingers();

            System.out.println(node1);
            System.out.println(node2);
            System.out.println(node3);

            System.out.println("Query result: " + node2.findSuccessor(new BigInteger("5")).id);
                                            
        } catch (NoSuchAlgorithmException e) {
            
            e.printStackTrace();
        }
    }

    public Node(String ip, int port) throws NoSuchAlgorithmException {
        this.ip = ip;
        this.port = port;
        this.id = getHash(ip, port);
        this.finger_table = new Node[M];
    }

    public Node(int id) {
        this.ip = null;
        this.port = 0;
        this.id = new BigInteger(Integer.toString(id));
        this.finger_table = new Node[M];
    }

    public void create() {
        this.predecessor = null;
        this.successor = this;

        for (int i = 0; i < M; i++) {
            finger_table[i] = this;
        }
    }

    public void join(Node ring_reference) throws NoSuchAlgorithmException {
        this.successor = ring_reference.findSuccessor(this.id);
        this.predecessor = null;

        finger_table[0] = this.successor;
        for (int i = 1; i < finger_table.length; i++) {
            BigInteger finger_id = (this.id.add(new BigInteger("2").pow(i))).mod(new BigInteger("2").pow(M));
            if (clockwiseInclusiveBetween(finger_id, this.id, this.successor.id))
              finger_table[i] = this.successor;
      
            else
              finger_table[i] = this;
          }
    }

    public Node findSuccessor(BigInteger id) throws NoSuchAlgorithmException {
        if(clockwiseInclusiveBetween(id, this.id, this.successor.id)) {
            return this.successor;
        }
        else {
            Node n = closestPrecedingNode(id);
            return n.findSuccessor(id);
        }
    }

    public Node closestPrecedingNode(BigInteger id) {
        for(int i = M-1; i > 0; i--) {
            
            if(clockwiseExclusiveBetween(finger_table[i].id, this.id, id)) {
                return finger_table[i];
            }
        }
        //return this;
        return this.successor; // assim, qd n se sabe ou a finger table está mal / desactualizada, manda-se para o sucessor para pesquisa linear. no entanto, n está assim no paper
    }

    public void stabilize() {
        Node x = this.successor.predecessor;

        if(x!=null && clockwiseExclusiveBetween(x.id, this.id, this.successor.id)) {
            this.successor = x;
            this.finger_table[0] = this.successor;
        }

        this.successor.notify(this);
    }

    public void notify(Node n) {
        if(this.predecessor == null || clockwiseExclusiveBetween(n.id, this.predecessor.id, this.id)) {
            this.predecessor = n;
        }
    }

    public void fixFingers() throws NoSuchAlgorithmException {
        // dar fix a todos de uma só vez para não termos que estar a chamar tantas vezes. pelo menos para já (testar localmente) dá jeito
        for(int i = M-1; i >= 1; i--) {
            BigInteger finger_id = (this.id.add(new BigInteger("2").pow(i))).mod(new BigInteger("2").pow(M));
            finger_table[i] = findSuccessor(finger_id);
        }
    }

    public boolean clockwiseInclusiveBetween(BigInteger id, BigInteger id1, BigInteger id2) {
        if(id2.compareTo(id1)==1) {
            return id.compareTo(id1)==1 && id.compareTo(id2)<=0;
        }
        else {
            return id.compareTo(id1)==1 || id.compareTo(id2)<=0;
        }
    }

    public boolean clockwiseExclusiveBetween(BigInteger id, BigInteger id1, BigInteger id2) {
        if(id2.compareTo(id1)==1) {
            return id.compareTo(id1)==1 && id.compareTo(id2)==-1;
        }
        else {
            return id.compareTo(id1)==1 || id.compareTo(id2)==-1;
        }
    }

    private BigInteger getHash(String ip, int port) throws NoSuchAlgorithmException {
        String unhashedId = ip + ';' + Integer.toString(port);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(unhashedId.getBytes());
        return new BigInteger(1, messageDigest);
    }

    @Override
    public String toString() {
        String str = "~~~~~\nID: " + this.id + "\nSuccessor:";
        if(this.successor!=null) str += this.successor.id;
        else str += null;
        str+="\nPredeccessor: ";
        if(this.predecessor!=null) str += this.predecessor.id;
        else str+=null;
        str+="\nFinger Table: \n";
        for(int i = 0; i<finger_table.length; i++) {
            BigInteger finger_id = (this.id.add(new BigInteger("2").pow(i))).mod(new BigInteger("2").pow(M));
            str+="N" + finger_id + ": ";
            if(finger_table[i]!=null) str += "" + finger_table[i].id + '\n';
            else str += "null \n";
        }
        str += "~~~~~";
        return str;
    }
    
}