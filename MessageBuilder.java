import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MessageBuilder {
    public static byte[] getPutchunkMessage(String fileId, int chunkNo, byte[] body) throws IOException {
        String msg = getMessage("PUTCHUNK", fileId, chunkNo);
        msg += "\r\n"; // CRLF
        msg += "\r\n"; // CRLF
        ByteArrayOutputStream message = new ByteArrayOutputStream();
        message.write(msg.getBytes());
        message.write(body);
        return message.toByteArray();
    }

    private static String getMessage(String msgType, String fileId, int chunkNo){
        String string = "PROTOCOL " + msgType + " " + fileId + " " + Integer.toString(chunkNo);
        return string;
    }
}
