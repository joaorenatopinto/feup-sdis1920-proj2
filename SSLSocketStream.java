import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLSocketStream implements java.lang.AutoCloseable {
  DataOutputStream out;
  BufferedReader bufferedIn;
  InputStream in;
  SSLSocket socket;

  /**
   * SSLSocket to read from and write to.
   */
  public SSLSocketStream(String ip, int port) throws IOException {
    SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    socket = (SSLSocket) factory.createSocket(ip, port);
    socket.startHandshake();
    out = new DataOutputStream(socket.getOutputStream());
    bufferedIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    in = socket.getInputStream();
  }

  public void write(byte[] b) throws IOException {
    out.write(b);
  }

  public int read(byte[] buf) throws IOException {
    return in.read(buf);
  }

  public String readLine() throws IOException {
    return bufferedIn.readLine();
  }

  public void close() throws IOException {
    socket.close();
  }
}
