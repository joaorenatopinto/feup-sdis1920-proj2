import java.security.NoSuchAlgorithmException;

public class ChordHandler implements Runnable {
    public Node chordNode;

    public ChordHandler(Node node) {
        this.chordNode = node;
    }

    @Override
    public void run() {
        System.out.println("Stabilizing Node");
        try {
            chordNode.stabilize();
            chordNode.fixFingers();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return;
    }
}