package Storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

public class FileInfo {
    private String id;
    private String path;
    private int rep_degree;
    private List<ChunkInfo> chunks;

    FileInfo(String path, int rep_degree){
        this.path = path;
        this.rep_degree = rep_degree;
        try {
            this.id = this.hasher(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.chunks = Collections.synchronizedList(new ArrayList<ChunkInfo>());
    }

    private String hasher(String file_path) throws IOException, NoSuchAlgorithmException{
        Path path = Paths.get(file_path);
        ByteArrayOutputStream dataWMetaData = new ByteArrayOutputStream();
        StringBuffer hexString = new StringBuffer();
        
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            FileInputStream fis = new FileInputStream(file_path);
            byte[] data = new byte[200000000]; 
            fis.read(data);
            fis.close();

            dataWMetaData.write(file_path.getBytes());
            dataWMetaData.write(attr.lastModifiedTime().toString().getBytes());
            dataWMetaData.write(data);
       
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(dataWMetaData.toByteArray());
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if(hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
            }       
        
        return hexString.toString();
    }

    public void addChunk(ChunkInfo chunk){
        this.chunks.add(chunk);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the chunks
     */
    public List<ChunkInfo> getChunks() {
        return chunks;
    }

    /**
     * @return the rep_degree
     */
    public int getRep_degree() {
        return rep_degree;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    public ChunkInfo getChunkByNo(int no){
        Optional<ChunkInfo> result = chunks.stream().filter(chunk -> chunk.getNo() == no).findFirst();
        if(!result.isPresent())
            return null;
        return result.get();
    }

    @Override
    public String toString() {
        String aux =  "Path: " + path + "\n-\n  FileID: " + id + "\n  Desired Replication Degree: " + rep_degree + "\n  Chunks: " + chunks.size();
        for (ChunkInfo chunkInfo : chunks) {
            
            aux = aux + "\n-\n" + chunkInfo.toString();
            
        }
        return aux;
    }
}