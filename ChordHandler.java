import java.security.NoSuchAlgorithmException;

public class ChordHandler implements Runnable {
    public final Node chordNode;

    public ChordHandler(Node node) {
        this.chordNode = node;
    }

    @Override
    public void run() {
        //System.out.println("Stabilize and Fix Fingers");
        try {
            chordNode.checkSuccessor();
            chordNode.checkPredecessor();
            chordNode.stabilize();
            chordNode.fixFingers();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println(chordNode);
    }
}