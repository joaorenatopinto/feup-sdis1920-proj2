import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocket;

public class MessageProcessor implements Runnable {

  SSLSocket clientSocket;

  public MessageProcessor(SSLSocket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    InputStream in = null;
    // PrintWriter out = null;
    OutputStream dataOut = null;

    try {

      dataOut = new DataOutputStream(clientSocket.getOutputStream());
      // out = new PrintWriter(clientSocket.getOutputStream(), true);
      in = new DataInputStream(clientSocket.getInputStream());
    } catch (IOException e) {
      return;
    }
    byte[] fromClient = new byte[65000];
    int msgSize;
    try {
      if ((msgSize = in.read(fromClient)) != -1) {
        ByteArrayOutputStream message = new ByteArrayOutputStream();
        message.write(fromClient, 0, msgSize);

        // System.out.println(msgSize);
        if (new String(fromClient).equals("Bye.")) {
          dataOut.write("Bye.".getBytes());
          clientSocket.close();
        } else {
          byte[] meh = processMessage(message.toByteArray());
          if (meh != null) {
            // String answer = new String(meh);
            // System.out.println("YOU: " + answer);
            dataOut.write(meh);
          }
          clientSocket.close();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Process message.
   */
  public byte[] processMessage(byte[] msg) throws NoSuchAlgorithmException {
    String[] msgParts = new String(msg).split("\\s+|\n");
    NodeReference node = null;
    if (msgParts[0].equals("CHORD")) {
      switch (msgParts[1]) {
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
        default:
          break;
      }
    } else if (msgParts[0].equals("PROTOCOL")) {
      String fileID;
      int chunkNo;
      int copyNo;
      switch (msgParts[1]) {
        case "PUTCHUNK":
          // Save file
          if (Peer.saveChunk(msg)) {
            return "SUCCESS".getBytes();
          }
          return "ERROR".getBytes();
        case "GETCHUNK":
          // Get the information of the needed chunk
          fileID = msgParts[2];
          chunkNo = Integer.parseInt(msgParts[3]);
          copyNo = Integer.parseInt(msgParts[4]);
          byte[] chunk;
          try {
            // Send Chunk
            chunk = Peer.retrieveChunk(fileID, chunkNo, copyNo);
          } catch (IOException e) {
            // If it catches an IOException it means it couldn't retrieve the chunk so it
            // informs the node
            return "ERROR".getBytes();
          }
          return chunk;
        case "DELETE":
          // Get the information of the needed chunk
          fileID = msgParts[2];
          chunkNo = Integer.parseInt(msgParts[3]);
          // Delete chunk
          if (Peer.deleteSavedChunk(fileID, chunkNo)) {
            return "SUCCESS".getBytes();
          }
          return "ERROR".getBytes();
        default:
          break;
      }
    }
    if (node != null) {
      return ("CHORD NODE " + node.ip + " " + node.port).getBytes();
    } else {
      return null;
    }
  }
}
