import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import javax.net.ssl.SSLSocket;

public class MessageProcessor implements Runnable{

    final SSLSocket clientSocket;

    public MessageProcessor (final SSLSocket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        InputStream in = null;
        //PrintWriter out = null;
        OutputStream dataOut = null;

        try {
            
            dataOut = new DataOutputStream(clientSocket.getOutputStream());
            //out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new DataInputStream(clientSocket.getInputStream());
        } catch (final IOException e) {
            return;
        }
        final byte[] fromClient = new byte[65000];
        int msg_size;
        try {
            if ((msg_size = in.read(fromClient)) != -1) {
                final ByteArrayOutputStream message = new ByteArrayOutputStream();
                message.write(fromClient, 0, msg_size); 

                //System.out.println(msg_size);
                if (new String(fromClient).equals("Bye.")) {
                    dataOut.write("Bye.".getBytes());
                    clientSocket.close();
                } else {
                    byte[] meh = processMessage(message.toByteArray());
                    if (meh != null) {
                        //final String answer = new String(meh);
                        //System.out.println("YOU: " + answer);
                        dataOut.write(meh);
                    } 
                    clientSocket.close();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    public byte[] processMessage(final byte[] msg) throws NoSuchAlgorithmException {
        final String[] msgParts = new String(msg).split("\\s+|\n");
        NodeReference node = null;
        if (msgParts[0].equals("CHORD")) {
            switch (msgParts[1]) {
                case "FINDSUCCESSOR":
                    node = Peer.chordNode.findSuccessor(new BigInteger(msgParts[2]));
                    break;
                case "NOTIFY":
                    final NodeReference notifier = new NodeReference(msgParts[2], Integer.parseInt(msgParts[3]));
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
                    // Save file
                    // TODO: RETURN SUCESS OR NOT AND ANSWER WITH SUCESS OR ERROR MESSAGE
                    
                    Peer.storage.saveFile(msg);
                    return "PROTOCOL BACKUP OH YEAH YEAH YEAH".getBytes();
                case "GETCHUNK":
                    // Get the information of the needed chunk
                    String file_id = msgParts[2];
                    int chunk_no = Integer.parseInt(msgParts[3]);
                    byte[] chunk;

                    // Send Chunk
                    // TODO: SEND CHUNK OR ERROR MESSAGE
                    try {
                        chunk = Peer.retrieveChunk(file_id, chunk_no);
                    } catch (IOException e) {
                        // If it catches an IOException it means it couldn't retrieve the chunk so it informs the node
                        return "ERROR".getBytes();
                    }
                    return chunk;
                default:
                    break;
            }
        }
        if (node != null) {
            return ("CHORD NODE " + node.ip + " " + node.port).getBytes();
        } else return null;
    }
}
