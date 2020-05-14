import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocket; 
import javax.net.ssl.SSLSocketFactory;

import Storage.ChunkInfo;
import Storage.FileInfo;

public class PeerMethods implements PeerInterface {
    static public final int CHUNK_SIZE = 64000;
    static public final double FILE_MAX_SIZE = 1000 * 1000000;

    public void backup(String path, int rep_degree) {
        Peer.pool.execute(() -> {
            try {
                chunkify_file(path, rep_degree);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        Peer.shutdown = true;
    }

    public void findSuccessorTest(BigInteger id) throws NoSuchAlgorithmException {
        NodeReference node = Peer.chordNode.findSuccessor(id);
        System.out.println("Node: " + node.ip + " " + node.port + " " + node.id);
    }

    public void chunkify_file(String file_path, int rep_degree) throws IOException, NoSuchAlgorithmException {
        // file handling
        InputStream is;
        try{
            is = new FileInputStream(file_path);
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return;
        }

        Path path = Paths.get(file_path);
        if(path.toFile().length() > FILE_MAX_SIZE) {
            System.out.println("File too big, max size: 1GBytes");
            is.close();
            return;
        }
        FileInfo new_file = new FileInfo(file_path, rep_degree);
        Peer.storage.addBackedFile(new_file);

        int chunkno = 1;
        int chunk_size = 0;
        byte[] b = new byte[CHUNK_SIZE];

        while((chunk_size = is.read(b)) != -1) {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            body.write(b, 0, chunk_size);
            byte[] chunk_body = body.toByteArray();
            new_file.addChunk(new ChunkInfo(chunkno, new_file.getId(), rep_degree, chunk_body.length));
            backupChunk(new_file.getId(), chunkno, rep_degree, chunk_body);
        }
        is.close();
    }

    public void backupChunk(String file_id, int chunk_no, int rep_degree, byte[] body) throws NoSuchAlgorithmException, IOException {
        for(int i = 0; i < rep_degree; i++) {
            BigInteger chunkChordId = getHash(file_id, chunk_no, i);
            NodeReference receiverNode = Peer.chordNode.findSuccessor(chunkChordId);
            byte[] msg = MessageBuilder.getPutchunkMessage(file_id, chunk_no, body);
            SSLSocket Socket = null;
            try {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                Socket = (SSLSocket) factory.createSocket(receiverNode.ip, receiverNode.port);

                Socket.startHandshake();

                DataOutputStream out = new DataOutputStream(Socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));

                String fromServer;
                out.write(msg);

                if ((fromServer = in.readLine()) != null) {
                    System.out.println("Server: " + fromServer);
                    // String[] answer = fromServer.split(" ");
                } else {
                    System.out.println("ERROR: Backup answer was empty.");
                }

            } catch (IOException e) {
                System.out.println("Exception thrown: " + e.getMessage());
            }
        }
    }

    private BigInteger getHash(String file_id, int chunk_no, int copyNo) throws NoSuchAlgorithmException {
        String unhashedId = file_id + "_" + chunk_no + "_" + copyNo;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(unhashedId.getBytes());
        return new BigInteger(1, messageDigest);
    }
}
