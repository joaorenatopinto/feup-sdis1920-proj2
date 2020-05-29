import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Node {
  public BigInteger id;
  public String ip;
  public int port;

  NodeReference successor;
  NodeReference predecessor;
  NodeReference[] fingerTable;
  NodeReference ownReference;

  private static int M = 32;

  /**
   * Node constructor.
   */
  public Node(String ip, int port, Peer peer) throws NoSuchAlgorithmException {
    this.ip = ip;
    this.port = port;
    this.id = getHash(ip, port);
    this.fingerTable = new NodeReference[M];
    this.ownReference = new NodeReference(ip, port);
  }

  /**
   * Node constructor.
   */
  public Node(int id) {
    this.ip = null;
    this.port = 0;
    this.id = new BigInteger(Integer.toString(id));
    this.fingerTable = new NodeReference[M];
  }

  /**
   * Create circle.
   */
  public void create() {
    System.out.println("CREATOR ID: " + this.id);

    this.predecessor = null;
    this.successor = this.ownReference;

    for (int i = 0; i < M; i++) {
      fingerTable[i] = this.ownReference;
    }
  }

  /**
   * Join circle.
   */
  public void join(String ip, int port) throws NoSuchAlgorithmException {
    System.out.println("JOINER ID: " + this.id);
    this.successor = new NodeReference(ip, port).findSuccessor(this.id);
    this.predecessor = null;

    fingerTable[0] = this.successor;
    for (int i = 1; i < fingerTable.length; i++) {
      BigInteger fingerId = (this.id.add(new BigInteger("2").pow(i)))
          .mod(new BigInteger("2").pow(M));
      if (clockwiseInclusiveBetween(fingerId, this.id, this.successor.id)) {
        fingerTable[i] = this.successor;
      } else {
        fingerTable[i] = this.ownReference;
      }
    }

    System.out.println("SUCCESSOR: " + this.successor.id);
  }

  /**
   * Create circle.
   */
  public void join(NodeReference ringReference) throws NoSuchAlgorithmException {
    this.successor = ringReference.findSuccessor(this.id);
    this.predecessor = null;

    fingerTable[0] = this.successor;
    for (int i = 1; i < fingerTable.length; i++) {
      BigInteger fingerId = (this.id.add(new BigInteger("2").pow(i)))
          .mod(new BigInteger("2").pow(M));
      if (clockwiseInclusiveBetween(fingerId, this.id, this.successor.id)) {
        fingerTable[i] = this.successor;
      } else {
        fingerTable[i] = this.ownReference;
      }
    }
  }

  /**
   * Find successor in circle.
   */
  public NodeReference findSuccessor(BigInteger id) throws NoSuchAlgorithmException {
    if (clockwiseInclusiveBetween(id, this.id, this.successor.id)) {
      return this.successor;
    } else {
      NodeReference n = closestPrecedingNode(id);
      if (n.id.equals(this.id)) {
        return this.ownReference;
      }
      return n.findSuccessor(id);
    }
  }

  /**
   * Closest preceding node.
   */
  public NodeReference closestPrecedingNode(BigInteger id) {
    for (int i = M - 1; i > 0; i--) {

      if (clockwiseExclusiveBetween(fingerTable[i].id, this.id, id)) {
        return fingerTable[i];
      }
    }
    // return this;
    return this.successor;
    /* assim, qd n se sabe ou a finger table está mal / desactualizada, manda-se
       para o sucessor para pesquisa linear. no entanto, n está assim no paper */
  }

  /**
   * Stabilize circle.
   */
  public void stabilize() throws NoSuchAlgorithmException {
    // System.out.println("Before get successor predecessor");
    NodeReference x = getSuccessorPredecessor();

    if (x != null && clockwiseExclusiveBetween(x.id, this.id, this.successor.id)) {
      this.successor = x;
      this.fingerTable[0] = this.successor;
    }
    // System.out.println("Before notify");
    this.successor.notify(this.ownReference);
    // System.out.println("After notify");
  }

  public NodeReference getSuccessorPredecessor() throws NoSuchAlgorithmException {
    return this.successor.getPredecessor();
  }

  /**
   * Notify circle.
   */
  public void notify(NodeReference n) {
    if (this.predecessor == null || clockwiseExclusiveBetween(n.id, this.predecessor.id, this.id)) {
      this.predecessor = n;
      //Peer.giveChunks(n);
    }
  }

  /**
   * Fix circle.
   */
  public void fixFingers() throws NoSuchAlgorithmException {
    // dar fix a todos de uma só vez para não termos que estar a chamar tantas
    // vezes. pelo menos para já (testar localmente) dá jeito
    for (int i = M - 1; i >= 1; i--) {
      BigInteger fingerId = (this.id.add(new BigInteger("2").pow(i)))
          .mod(new BigInteger("2").pow(M));
      fingerTable[i] = findSuccessor(fingerId);
    }
  }

  /**
   * Check predecessor.
   */
  public void checkPredecessor() {
    if (this.predecessor!=null && predecessor.hasFailed()) {
      this.predecessor = null;
    }
  }

  /**
   * Check successor.
   */
  public void checkSuccessor() {
    if (successor.hasFailed()) {
      this.successor = this.ownReference;
    }
  }

  /**
   * Clockwise inclusive between.
   */
  public boolean clockwiseInclusiveBetween(BigInteger id, BigInteger id1, BigInteger id2) {
    if (id2.compareTo(id1) == 1) {
      return id.compareTo(id1) == 1 && id.compareTo(id2) <= 0;
    } else {
      return id.compareTo(id1) == 1 || id.compareTo(id2) <= 0;
    }
  }

  /**
   * Clockwise exclusive between.
   */
  public boolean clockwiseExclusiveBetween(BigInteger id, BigInteger id1, BigInteger id2) {
    if (id2.compareTo(id1) == 1) {
      return id.compareTo(id1) == 1 && id.compareTo(id2) == -1;
    } else {
      return id.compareTo(id1) == 1 || id.compareTo(id2) == -1;
    }
  }

  private BigInteger getHash(String ip, int port) throws NoSuchAlgorithmException {
    String unhashedId = ip + ';' + port;
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] messageDigest = md.digest(unhashedId.getBytes());
    BigInteger toNum = new BigInteger(1, messageDigest);
    while (toNum.compareTo(new BigInteger("1000000000")) == 1) {
      toNum = toNum.divide(new BigInteger("10"));
    }
    return toNum;
  }

  @Override
  public String toString() {
    String str = "~~~~~\nID: " + this.id + "\nSuccessor:";
    if (this.successor != null) {
      str += this.successor.id;
    } else {
      str += null;
    }
    str += "\nPredeccessor: ";
    if (this.predecessor != null) {
      str += this.predecessor.id;
    } else {
      str += null;
    }
    // str+="\nFinger Table: \n";
    // for(int i = 0; i<fingerTable.length; i++) {
    // BigInteger fingerId = (this.id.add(new BigInteger("2").pow(i))).mod(new
    // BigInteger("2").pow(M));
    // str+="N" + fingerId + ": ";
    // if(fingerTable[i]!=null) str += "" + fingerTable[i].id + '\n';
    // else str += "null \n";
    // }
    str += "\n~~~~~";
    return str;
  }

}
