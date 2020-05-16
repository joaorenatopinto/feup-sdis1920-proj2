import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MessageBuilder {
  /**
   * Return byte[] of PUTCHUNK message.
   */
  public static byte[] getPutchunkMessage(String fileId, int chunkNo, byte[] body, int copyN)
      throws IOException {
    String msg = getMessage("PUTCHUNK", fileId, chunkNo, copyN);
    // msg += "\r\n"; // CRLF
    // msg += "\r\n"; // CRLF
    ByteArrayOutputStream message = new ByteArrayOutputStream();
    message.write(msg.getBytes());
    message.write(0xD);
    message.write(0xA);
    message.write(0xD);
    message.write(0xA);
    message.write(body);
    return message.toByteArray();
  }

  /**
   * Return byte[] of CHUNK message.
   */
  public static byte[] getChunkMessage(String fileId, int chunkNo, byte[] body) throws IOException {
    String msg = getMessage("CHUNK", fileId, chunkNo);
    // msg += "\r\n"; // CRLF
    // msg += "\r\n"; // CRLF
    ByteArrayOutputStream message = new ByteArrayOutputStream();
    message.write(msg.getBytes());
    message.write(0xD);
    message.write(0xA);
    message.write(0xD);
    message.write(0xA);
    message.write(body);
    return message.toByteArray();
  }

    /**
   * Return byte[] of CHUNK message.
   */
  public static byte[] getChunkMessage(String fileId, int chunkNo, int copyNo, byte[] body) throws IOException {
    String msg = getMessage("CHUNK", fileId, chunkNo, copyNo);
    // msg += "\r\n"; // CRLF
    // msg += "\r\n"; // CRLF
    ByteArrayOutputStream message = new ByteArrayOutputStream();
    message.write(msg.getBytes());
    message.write(0xD);
    message.write(0xA);
    message.write(0xD);
    message.write(0xA);
    message.write(body);
    return message.toByteArray();
  }

  /**
   * Return byte[] of GETCHUNK message.
   */
  public static byte[] getGetchunkMessage(String fileId, int chunkNo, int copyNo) throws IOException {
    String msg = getMessage("GETCHUNK", fileId, chunkNo, copyNo);
    // msg += "\r\n"; // CRLF
    // msg += "\r\n"; // CRLF
    ByteArrayOutputStream message = new ByteArrayOutputStream();
    message.write(msg.getBytes());

    return message.toByteArray();
  }

  /**
   * Return byte[] of DELETE message.
   */
  public static byte[] getDeleteMessage(String fileId, int chunkNo) throws IOException {
    String msg = getMessage("DELETE", fileId, chunkNo);
    ByteArrayOutputStream message = new ByteArrayOutputStream();
    message.write(msg.getBytes());

    return message.toByteArray();
  }

  private static String getMessage(String msgType, String fileId, int chunkNo) {
    String string = "PROTOCOL " + msgType + " " + fileId + " " + chunkNo;
    return string;
  }

  private static String getMessage(String msgType, String fileId, int chunkNo, int copyN) {
    String string = "PROTOCOL " + msgType + " " + fileId + " " + chunkNo + " " + copyN;
    return string;
  }
}
