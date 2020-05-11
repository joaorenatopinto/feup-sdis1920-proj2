import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.BufferedReader;
import java.io.IOException;
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

    public NodeReference findSuccessor(BigInteger id) throws NoSuchAlgorithmException {
        String ipAdress;
        int portNumber;
        SSLSocket Socket = null;  
        try {
            SSLSocketFactory factory =  (SSLSocketFactory)SSLSocketFactory.getDefault();
            Socket = (SSLSocket) factory.createSocket(this.ip, this.port);  
            
            Socket.startHandshake();

            PrintWriter out = new PrintWriter(Socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));

            String fromServer;

            out.println("CHORD FINDSUCCESSOR " + id);
            
           if ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                String[] answer = fromServer.split(" ");
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

            PrintWriter out = new PrintWriter(Socket.getOutputStream(), true);

            out.println("CHORD NOTIFY " + n.ip + " " + n.port);
        } catch ( IOException e ){
            e.printStackTrace();
        }
        return;
    }

    public NodeReference getPredecessor() throws NoSuchAlgorithmException{
        String ipAdress;
        int portNumber;
        SSLSocket Socket = null;  
        try {
            SSLSocketFactory factory =  (SSLSocketFactory)SSLSocketFactory.getDefault();
            Socket = (SSLSocket) factory.createSocket(this.ip, this.port);  
            
            Socket.startHandshake();

            PrintWriter out = new PrintWriter(Socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));

            String fromServer;

            out.println("CHORD GETPREDECESSOR");
            
           if ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                String[] answer = fromServer.split(" ");
                if(answer[2].equals("NULL")) {
                    return null;
                }
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

    private BigInteger getHash(String ip, int port) throws NoSuchAlgorithmException {
        String unhashedId = ip + ';' + Integer.toString(port);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(unhashedId.getBytes());
        return new BigInteger(1, messageDigest);
    }
}