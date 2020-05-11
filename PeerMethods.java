public class PeerMethods implements PeerInterface {
    public void backup() {
        System.out.println("Remote method backup called.");

        return;
    }
    public void shutdown() {
        Peer.shutdown = true;
        return;
    }
}