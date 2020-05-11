import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocket;

public class MessageProcessor implements Runnable{

    SSLSocket clientSocket;

    public MessageProcessor (SSLSocket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;

        try { 
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            return;
        }
        String fromClient;
        try {
            while ((fromClient = in.readLine()) != null){
                if(fromClient.equals("Bye.")) {
                    out.println("Bye.");
                    clientSocket.close();
                    break;
                } 
                else {
                    NodeReference node = processMessage(fromClient); 
                    String answer = "CHORD SUCCESSOR " + node.ip + " " + node.port;
                    System.out.println("YOU: " + answer);
                    out.println(answer);
                    clientSocket.close();
                    break;
                }
            } 
        } catch (Exception e) {
           e.printStackTrace();
           return;
        }
        
    }

    public NodeReference processMessage(String msg) throws NoSuchAlgorithmException {
        String[] msgParts = msg.split(" ");
        NodeReference node = null;
        if(msgParts[0].equals("CHORD")) {
            switch(msgParts[1]) {
                case "FINDSUCCESSOR":
                    node = Peer.chordNode.findSuccessor(new BigInteger(msgParts[2]));
            }
        }
        return node;
    }
}