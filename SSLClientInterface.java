import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SSLClientInterface extends SSLManager {
    private String passphrase;
    private String managerId;
    private InetSocketAddress address;

    private SSLContext context;
    private AsynchronousSocketChannel channel;

    public SSLClientInterface(String managerId, String passphrase, String hostname, int port) throws SSLManagerException {
        super();

        this.passphrase = passphrase;
        this.managerId = managerId;
        this.address = new InetSocketAddress(hostname, port);


        //Initialize the key and trust stores
        try {
            this.context = SSLManager.initSSLContext(this.managerId,this.passphrase);

            //Create the socket channel
            this.channel = AsynchronousSocketChannel.open();

            //Connect the channel
            Future<Void> connection = this.channel.connect(new InetSocketAddress(hostname,port));

            //Initialize the SSLManager -> concurrent with connection
            super.init(this.channel,address,this.context,true);

            connection.get(); //only exit when connection completes
        }
        catch (InterruptedException | ExecutionException | IOException e) {
            throw new SSLManagerException(e.getMessage());
        }
    }
}
