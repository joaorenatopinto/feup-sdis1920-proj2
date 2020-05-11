public class ChordHandler implements Runnable{
    public Node cordNode;

    public ChordHandler (Node node){
        this.cordNode = node;
    }

    @Override
    public void run() {
        System.out.println("Stabilizing Node");
        //TODO: Fazer tudo o que for preciso para o chord estar sempre fixe.
        //this.cordNode.stabilize();
        //this.cordNode.notify();
        return;
    }
}