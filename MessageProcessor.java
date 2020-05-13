import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.io.ByteArrayOutputStream;

import javax.net.ssl.SSLSocket;

public class MessageProcessor implements Runnable{

    SSLSocket clientSocket;

    public MessageProcessor (SSLSocket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        InputStream in = null;
        PrintWriter out = null;

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            return;
        }
        byte[] fromClient = new byte[65000];
        int msg_size;
        try {
            while ((msg_size = in.read(fromClient)) != -1){
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                message.write(fromClient, 0, msg_size);
                if(new String(fromClient).equals("Bye.")) {
                    out.println("Bye.");
                    clientSocket.close();
                    break;
                }
                else {
                    String answer = processMessage(message.toByteArray());
                    if (answer != null) {
                        System.out.println("YOU: " + answer);
                        out.println(answer);
                    }
                    clientSocket.close();
                    break;
                }
            }
        } catch (Exception e) {
           e.printStackTrace();
           return;
        }

    }

    public String processMessage(byte[] msg) throws NoSuchAlgorithmException {
        String[] msgParts = new String(msg).split(" ");
        NodeReference node = null;
        if(msgParts[0].equals("CHORD")) {
            switch(msgParts[1]) {
                case "FINDSUCCESSOR":
                    node = Peer.chordNode.findSuccessor(new BigInteger(msgParts[2]));
                    break;
                case "NOTIFY":
                    NodeReference notifier = new NodeReference(msgParts[2], Integer.parseInt(msgParts[3]));
                    Peer.chordNode.notify(notifier);
                    break;
                case "GETPREDECESSOR":
                    node = Peer.chordNode.predecessor;
                    break;
            }
        }
        else if(msgParts[0].equals("PROTOCOL")) {
            switch (msgParts[1]) {
                case "PUTCHUNK":
                    //GUARDAR O FDP DO FICHEIRO
                    Peer.storage.saveFile(msg);
                    return "PROTOCOL BACKUP OH YEAH YEAH YEAH";
                default:
                    break;
            }
        }
        if (node != null) {
            return "CHORD NODE " + node.ip + " " + node.port;
        } else return null;
    }
}
