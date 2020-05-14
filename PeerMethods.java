import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocket; 
import javax.net.ssl.SSLSocketFactory;

import Storage.ChunkInfo;
import Storage.FileInfo;

public class PeerMethods implements PeerInterface {
    static public final int CHUNK_SIZE = 16000;
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
    
    public void restore(String path) {
        Peer.pool.execute(() -> {
            try {
                restoreFile(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void delete(String path) {
        Peer.pool.execute(() -> {
            try {
                deleteFile(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void restoreFile(String file_path){
        FileInfo file = Peer.storage.getFileInfoByFilePath(file_path);
        if(file == null){
            System.out.println("You can only restore files that have been previously backed up by the system.");
            return;
        }
        String file_id = file.getId();
        int n_chunks = file.getChunks().size();

        for(int chunk_no = 1; chunk_no <= n_chunks; chunk_no++) {
            try {
                byte[] chunk = restoreChunk(file_id, chunk_no, file.getRep_degree());
                if(chunk != null) {
                    Peer.storage.restoreChunk(chunk);
                } else {
                    System.out.println("Couldn't restore the chunk number " + chunk_no);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dechunky_file(file_path);
    }

    public void deleteFile(String file_path) throws IOException {
        FileInfo file = Peer.storage.getFileInfoByFilePath(file_path);
        if(file == null){
            System.out.println("You can only delete files that have been previously backed up by the system.");
            return;
        }
        String file_id = file.getId();
        int n_chunks = file.getChunks().size();

        for(int chunk_no = 1; chunk_no <= n_chunks; chunk_no++) {
            try {
                boolean success = deleteChunk(file_id, chunk_no, file.getRep_degree());
                if(!success) {
                    System.out.println("Couldn't delete the chunk number " + chunk_no);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Peer.storage.removeBackedFile(file);
        System.out.println("All " + file_path + " chunks were deleted");
    }


    public byte[] restoreChunk(String file_id, int chunk_no, int rep_degree) throws IOException, NoSuchAlgorithmException {
        byte[] chunk = null;

        for(int i = 0; i < rep_degree; i++) {
            BigInteger chunkChordId = getHash(file_id, chunk_no, i);
            System.out.println(">>> Chunk Hash: " + chunkChordId + " <<<");
            NodeReference receiverNode = Peer.chordNode.findSuccessor(chunkChordId);
            System.out.println(">>> Successor ID: " + receiverNode.id + " <<<");
            byte[] msg = MessageBuilder.getGetchunkMessage(file_id, chunk_no);
            SSLSocket Socket = null;
            try {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                Socket = (SSLSocket) factory.createSocket(receiverNode.ip, receiverNode.port);

                Socket.startHandshake();

                DataOutputStream dataOut = new DataOutputStream(Socket.getOutputStream());
                InputStream in = Socket.getInputStream();

                dataOut.write(msg);

                final byte[] fromClient = new byte[65000];
                int msg_size;
                if ((msg_size = in.read(fromClient)) != -1) {
                    final ByteArrayOutputStream message = new ByteArrayOutputStream();
                    message.write(fromClient, 0, msg_size);
                    if (new String(fromClient).equals("ERROR")) {
                        System.out.println("~~~~~~~~~~~~~~");
                        System.out.println("errrrrrrooooooooooooo");
                        System.out.println("~~~~~~~~~~~~~~");
                        Socket.close();
                        continue;
                    } else {
                        // TODO: CHECK IF FILE ID AND CHUNK NO MATCH (IF YES:)
                        chunk = message.toByteArray();
                        Socket.close();
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Exception thrown: " + e.getMessage());
            }
        }
        return chunk;
    }

    public boolean deleteChunk(String file_id, int chunk_no, int rep_degree) throws IOException, NoSuchAlgorithmException {

        for(int i = 0; i < rep_degree; i++) {
            BigInteger chunkChordId = getHash(file_id, chunk_no, i);
            System.out.println(">>> Chunk Hash: " + chunkChordId + " <<<");
            NodeReference receiverNode = Peer.chordNode.findSuccessor(chunkChordId);
            System.out.println(">>> Successor ID: " + receiverNode.id + " <<<");
            byte[] msg = MessageBuilder.getDeleteMessage(file_id, chunk_no);
            SSLSocket Socket = null;
            try {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                Socket = (SSLSocket) factory.createSocket(receiverNode.ip, receiverNode.port);

                Socket.startHandshake();

                DataOutputStream dataOut = new DataOutputStream(Socket.getOutputStream());
                InputStream in = Socket.getInputStream();

                dataOut.write(msg);

                final byte[] fromClient = new byte[65000];
                int msg_size;
                if ((msg_size = in.read(fromClient)) != -1) {
                    final ByteArrayOutputStream message = new ByteArrayOutputStream();
                    message.write(fromClient, 0, msg_size);
                    if (new String(fromClient).equals("ERROR")) {
                        Socket.close();
                        continue;
                    } else if (new String(fromClient).equals("SUCCESS")){
                        System.out.println("NICE!");
                        Socket.close();
                        continue;
                    }
                }
            } catch (IOException e) {
                System.out.println("Exception thrown: " + e.getMessage());
            }
        }
        return true;
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
            //Peer.storage.saveFile(chunk_body);
            backupChunk(new_file.getId(), chunkno, rep_degree, chunk_body);
            chunkno++;
        }
        is.close();
    }

    public void dechunky_file(String file_path) {
        FileInfo file = Peer.storage.getFileInfoByFilePath(file_path);
        String file_id = file.getId();
        int n_chunks = file.getChunks().size();

        String restored_dir_path = "Peers/dir" + Integer.toString(Peer.id) + "/restored";
        File restored_path = new File(restored_dir_path);
        restored_path.mkdir();

        File restored_file = new File(restored_dir_path + "/" + file_path.split("/")[file_path.split("/").length-1]);
        try {
            restored_file.createNewFile();
            OutputStream os = new FileOutputStream(restored_file);
            for(int i = 1; i <= n_chunks; i++) {
                File chunk = new File("Peers/dir" + Integer.toString(Peer.id) + "/temp" + "/" + file_id + "/" + file_id + "_" + i);
                os.write(Files.readAllBytes(chunk.toPath()));
                chunk.delete();
                new File("Peers/dir" + Integer.toString(Peer.id) + "/temp" + "/" + file_id).delete();
            }
            os.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    public void backupChunk(String file_id, int chunk_no, int rep_degree, byte[] body) throws NoSuchAlgorithmException, IOException {
        for(int i = 0; i < rep_degree; i++) {
            BigInteger chunkChordId = getHash(file_id, chunk_no, i);
            System.out.println(">>> Chunk Hash: " + chunkChordId + " <<<");
            NodeReference receiverNode = Peer.chordNode.findSuccessor(chunkChordId);
            System.out.println(">>> Successor ID: " + receiverNode.id + " <<<");
            byte[] msg = MessageBuilder.getPutchunkMessage(file_id, chunk_no, body);
            SSLSocket Socket = null;
            try {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                Socket = (SSLSocket) factory.createSocket(receiverNode.ip, receiverNode.port);
                //Socket.setSendBufferSize(64000);
                //System.out.println("MANDAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                //System.out.println(Socket.getSendBufferSize());
                
                
                Socket.setReceiveBufferSize(64000);
                Socket.startHandshake();

                DataOutputStream out = new DataOutputStream(Socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));

                String fromServer;
                System.out.println("LEEEEEEEEEEEEEEEEEEEEEEEEEEEENGHT");
                System.out.println(msg.length);
                //System.out.println("LEEEEEEEEEEEEEEEEEEEEEEEEEEEENGHT");
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

    static public byte[] retrieveChunk(String file_id, int chunk_no) throws IOException {
        byte[] chunk = null;
        String key = file_id + "_" + chunk_no;

        Path file = Paths.get("Peers/dir" + Peer.id + "/" + key);
        byte[] file_data = Files.readAllBytes(file);
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(file_data);
        chunk = MessageBuilder.getChunkMessage(file_id, chunk_no, body.toByteArray());

        return chunk;
    }

    static public boolean deleteSavedChunk(String file_id, int chunk_no) {
        String key = file_id + "_" + chunk_no;
        ChunkInfo chunk = Peer.storage.getStoredChunkInfo(file_id, chunk_no);
        if (chunk != null) {
            Path file = Paths.get("Peers/dir" + Peer.id + "/" + key);
            if (!file.toFile().delete())
                return false;
            Peer.storage.removeStoredChunk(chunk);
            return true;
        }
        return false;
    }

    private BigInteger getHash(String file_id, int chunk_no, int copyNo) throws NoSuchAlgorithmException {
        String unhashedId = file_id + "_" + chunk_no + "_" + copyNo;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(unhashedId.getBytes());
        BigInteger toNum = new BigInteger(1, messageDigest);
        while(toNum.compareTo(new BigInteger("1000000000"))==1) {
            toNum = toNum.divide(new BigInteger("10"));
        }
        return toNum;
    }
}
