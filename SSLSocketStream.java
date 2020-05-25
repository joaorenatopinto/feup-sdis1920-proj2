import java.io.IOException;

public class SSLSocketStream implements java.lang.AutoCloseable {
  SSLClientInterface client;

  /**
   * SSLSocket to read from and write to.
   */
  public SSLSocketStream(String ip, int port) throws SSLManagerException {
    this.client = new SSLClientInterface("client","123456",ip,port);
    client.handshake();
  }

  public void write(byte[] b) throws SSLManagerException {
    client.write(b);
  }

  public int read(byte[] buf) throws SSLManagerException {
    return client.read(buf);
  }


  public String readLine() throws SSLManagerException {
      return client.readln();
  }

  public void close() throws IOException, SSLManagerException {
    client.close();
  }
}
