import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class NodeReference {
    public BigInteger id;
    public final String ip;
    public final int port;

    NodeReference(String ip, int port) throws NoSuchAlgorithmException {
        this.ip = ip;
        this.port = port;
        this.id = getHash(ip, port);
    }

    NodeReference(String ip, String port) throws NoSuchAlgorithmException {
        this.ip = ip;
        //System.out.println("PORT:" + port + ";");
        this.port = Integer.parseInt(port);
        this.id = getHash(this.ip, this.port);
    }

    public NodeReference findSuccessor(BigInteger id) throws NoSuchAlgorithmException {
        String ipAddress;
        int portNumber;
        SSLSocket Socket = null;
        try {
            SSLSocketFactory factory =  (SSLSocketFactory)SSLSocketFactory.getDefault();
            Socket = (SSLSocket) factory.createSocket(this.ip, this.port);

            Socket.startHandshake();

            DataOutputStream out = new DataOutputStream(Socket.getOutputStream());
            InputStream in = Socket.getInputStream();

            byte[] fromClient = new byte[65000];
            int msg_size;

            out.write(("CHORD FINDSUCCESSOR " + id).getBytes());
            //System.out.println("YOU: " + "CHORD FINDSUCCESSOR " + id);

            if ((msg_size = in.read(fromClient)) != -1){
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                message.write(fromClient, 0, msg_size);
                String msg = message.toString();
                //System.out.println("Server: " + msg);
                String[] answer = msg.split("\\s+|\n");
                ipAddress = answer[2];
                portNumber = Integer.parseInt(answer[3]);
                NodeReference node = new NodeReference(ipAddress, portNumber);
                return node;
            }
            else {
                System.out.println("ERROR: Chord findSuccessor answer was empty.");
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void notify(NodeReference n) {
        SSLSocket Socket = null;
        try {
            SSLSocketFactory factory =  (SSLSocketFactory)SSLSocketFactory.getDefault();
            Socket = (SSLSocket) factory.createSocket(this.ip, this.port);

            Socket.startHandshake();

            DataOutputStream out = new DataOutputStream(Socket.getOutputStream());
            
            
            out.write(("CHORD NOTIFY " + n.ip + " " + n.port).getBytes());
            //System.out.println("YOU: " + "CHORD NOTIFY " + n.ip + " " + n.port);

        } catch ( IOException e ){
            e.printStackTrace();
        }
    }

    public NodeReference getPredecessor() throws NoSuchAlgorithmException{
        String ipAddress;
        String portNumber;
        SSLSocket Socket = null;

        try {
            SSLSocketFactory factory =  (SSLSocketFactory)SSLSocketFactory.getDefault();
            Socket = (SSLSocket) factory.createSocket(this.ip, this.port);
            Socket.startHandshake();
            
            DataOutputStream out = new DataOutputStream(Socket.getOutputStream());
            InputStream in = Socket.getInputStream();
            //String fromServer;
            byte[] fromClient = new byte[65000];
            int msg_size;
            
            out.write(("CHORD GETPREDECESSOR").getBytes());
            //System.out.println("YOU: " + "CHORD FINDSUCCESSOR " + id);
           if ((msg_size = in.read(fromClient)) != -1){
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                message.write(fromClient, 0, msg_size);
                String msg = message.toString();
                //System.out.println("Server: " + msg);
                String[] answer = msg.split("\\s+|\n");
                if(answer[2].equals("NULL")) {
                    return null;
                }
                ipAddress = answer[2];
                portNumber = answer[3];
                NodeReference node = new NodeReference(ipAddress, portNumber);

                return node;
            }
            else {
                System.out.println("ERROR: Chord getPredecessor answer was empty.");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BigInteger getHash(String ip, int port) throws NoSuchAlgorithmException {
        String unhashedId = ip + ';' + port;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(unhashedId.getBytes());
        return new BigInteger(1, messageDigest);
    }
}
