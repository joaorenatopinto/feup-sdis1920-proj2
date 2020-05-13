import java.security.NoSuchAlgorithmException;

public class ChordHandler implements Runnable {
    public final Node chordNode;

    public ChordHandler(Node node) {
        this.chordNode = node;
    }

    @Override
    public void run() {
        System.out.println("Stabilize and Fix Fingers");
        try {
           // System.out.println("Before stabilize");
            chordNode.stabilize();
            //System.out.println("After stabilize and before fix fingers");
            chordNode.fixFingers();
            //System.out.println("After fixFingers");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}