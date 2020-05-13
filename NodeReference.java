import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.net.ssl.SSLSocket;
import java.io.InputStreamReader;
import javax.net.ssl.SSLSocketFactory;

public class NodeReference {
    public BigInteger id;
    public String ip;
    public int port;

    NodeReference(String ip, int port) throws NoSuchAlgorithmException {
        this.ip = ip;
        this.port = port;
        this.id = getHash(ip, port);
    }

    NodeReference(String ip, String port) throws NoSuchAlgorithmException {
        this.ip = ip;
        System.out.println("PORT:" + port + ";");
        this.port = Integer.parseInt(port);
        this.id = getHash(this.ip, this.port);
    }

    public NodeReference findSuccessor(BigInteger id) throws NoSuchAlgorithmException {
        String ipAdress;
        int portNumber;
        SSLSocket Socket = null;
        try {
            SSLSocketFactory factory =  (SSLSocketFactory)SSLSocketFactory.getDefault();
            Socket = (SSLSocket) factory.createSocket(this.ip, this.port);

            Socket.startHandshake();

            DataOutputStream out = new DataOutputStream(Socket.getOutputStream());
            InputStream in = Socket.getInputStream();

            String fromServer;

            byte[] fromClient = new byte[65000];
            int msg_size;

            out.write(("CHORD FINDSUCCESSOR " + id).getBytes());
            System.out.println("YOU: " + "CHORD FINDSUCCESSOR " + id);

            if ((msg_size = in.read(fromClient)) != -1){
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                message.write(fromClient, 0, msg_size);
                String msg = message.toString();
                System.out.println("Server: " + msg);
                String[] answer = msg.split("\\s+|\n");
                ipAdress = answer[2];
                portNumber = Integer.parseInt(answer[3]);
                NodeReference node = new NodeReference(ipAdress, portNumber);
                return node;
            }
            else {
                System.out.println("DEU MERDA");
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void notify(NodeReference n) throws NoSuchAlgorithmException {
        SSLSocket Socket = null;
        try {
            SSLSocketFactory factory =  (SSLSocketFactory)SSLSocketFactory.getDefault();
            Socket = (SSLSocket) factory.createSocket(this.ip, this.port);

            Socket.startHandshake();

            DataOutputStream out = new DataOutputStream(Socket.getOutputStream());
            
            
            out.write(("CHORD NOTIFY " + n.ip + " " + n.port).getBytes());
            System.out.println("YOU: " +"CHORD NOTIFY " + n.ip + " " + n.port);

        } catch ( IOException e ){
            e.printStackTrace();
        }
        return;
    }

    public NodeReference getPredecessor() throws NoSuchAlgorithmException{
        String ipAdress;
        System.out.println("1");
        String portNumber;
        SSLSocket Socket = null;
        try {
            System.out.println("2");
            SSLSocketFactory factory =  (SSLSocketFactory)SSLSocketFactory.getDefault();
            Socket = (SSLSocket) factory.createSocket(this.ip, this.port);
            System.out.println("3");
            Socket.startHandshake();
            System.out.println("4");
            DataOutputStream out = new DataOutputStream(Socket.getOutputStream());
            InputStream in = Socket.getInputStream();
            System.out.println("5");
            //String fromServer;
            byte[] fromClient = new byte[65000];
            int msg_size;
            System.out.println("6");
            out.write(("CHORD GETPREDECESSOR").getBytes());
             System.out.println("YOU: " + "CHORD FINDSUCCESSOR " + id);
           if ((msg_size = in.read(fromClient)) != -1){
                System.out.println("7");
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                System.out.println("8");
                message.write(fromClient, 0, msg_size);
                System.out.println("9");
                String msg = message.toString();
                System.out.println("10");
                System.out.println("Server: " + msg);
                System.out.println("11");
                String[] answer = msg.split("\\s+|\n");
                System.out.println("12");
                if(answer[2].equals("NULL")) {
                    return null;
                }
                System.out.println("13");
                ipAdress = answer[2];
                portNumber = answer[3];
                System.out.println("15");
                NodeReference node = new NodeReference(ipAdress, portNumber);
                
                return node;
            }
            else {
                System.out.println("DEU MERDA");
            }

        }
        catch (IOException e) {
            System.out.println("20");
            e.printStackTrace();
        }
        System.out.println("21");
        return null;
    }

    private BigInteger getHash(String ip, int port) throws NoSuchAlgorithmException {
        String unhashedId = ip + ';' + Integer.toString(port);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(unhashedId.getBytes());
        return new BigInteger(1, messageDigest);
    }
}
