package Storage;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.*;

public class Storage {
    private long max_storage;
    private long curr_storage;

    private List<FileInfo> files_backed;
    private List<ChunkInfo> chunks_Stored;
    private int peer_id;

    public Storage(int peer_id){
        this.peer_id = peer_id;
        this.max_storage = 0;
        this.curr_storage = 0;
        this.files_backed = Collections.synchronizedList(new ArrayList<FileInfo>());
        this.chunks_Stored = Collections.synchronizedList(new ArrayList<ChunkInfo>());
    }

    /**
     * Get maximum capacity for file storage
     * @return maximum size of storage
     */
    public long getMax_storage() {
        return max_storage;
    }

    /**
     * Get the size of occupied storage
     * @return occupied storage size
     */
    public long getCurr_storage() {
        return curr_storage;
    }

    /**
     * @param max_storage the max_storage to set
     */
    public void setMax_storage(long max_storage) {
        this.max_storage = max_storage;
    }

    /**
     * @param curr_storage the curr_storage to set
     */
    public void setCurr_storage(long curr_storage) {
        this.curr_storage = curr_storage;
    }

    public void addToCurr_storage(long storage) {
        this.curr_storage = curr_storage + storage;
    }

    public void RemoveFromCurr_storage(long storage) {
        this.curr_storage = curr_storage - storage;
    }

    /**
     * @return the chunks_Stored
     */
    public List<ChunkInfo> getChunks_Stored() {
        return chunks_Stored;
    }

    /**
     * @return the files_backed
     */
    public List<FileInfo> getFiles_backed() {
        return files_backed;
    }

    public FileInfo getFileInfo(String file_id){
        try{
            Optional<FileInfo> result = files_backed.stream().filter(file -> file.getId().equals(file_id)).findFirst();
            if(result.isPresent())
                return result.get();
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public FileInfo getFileInfoByFilePath(String file_path){
        Optional<FileInfo> result = files_backed.stream().filter(file -> file.getPath().equals(file_path)).findFirst();
        if(result.isPresent())
            return result.get();
        else return null;
    }

    public ChunkInfo getStoredChunkInfo(String file_id, int chunk_no){
        Optional<ChunkInfo> result  = chunks_Stored.stream().filter(ch -> ((ch.getFileID().equals(file_id)) && (ch.getNo() == chunk_no))).findFirst();
        if(result.isPresent())
            return result.get();
        else return null;
    }

    public void addBackedFile(FileInfo file){
        files_backed.add(file);
    }

    public void addStoredChunk(ChunkInfo chunk){
        chunks_Stored.add(chunk);
    }

    public void removeBackedFile(FileInfo file){
        files_backed.remove(file);
    }

    public void removeStoredChunk(ChunkInfo chunk){
        chunks_Stored.remove(chunk);
    }

    public void saveFile(byte[] chunk){
        String path = "Peers/dir" + (Integer.toString(peer_id));
        File directory = new File(path);
        directory.mkdir();
        String chunkTxt = new String(chunk);
        String[] chunkpieces = chunkTxt.split("\\s+|\n");
        List<byte[]> parts = split(chunk);

        String fileName = chunkpieces[2] + "_" + chunkpieces[3];

        File file = new File(path + "/" + fileName);
        try{
            file.getParentFile().mkdirs();
            file.createNewFile();
            OutputStream os  = new FileOutputStream(file);
            os.write(parts.get(1));
            this.addToCurr_storage(file.length());
            os.close();
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
        for(int i=0; i< pattern.length; i++) {
            if(pattern[i] != input[pos+i]) {
                return false;
            }
        }
        return true;
    }

    public static List<byte[]> split(byte[] input) {
        byte[] pattern = {0xD,0xA,0xD,0xA};
        List<byte[]> l = new LinkedList<byte[]>();
        int blockStart = 0;
        for(int i=0; i<input.length; i++) {
           if(isMatch(pattern,input,i)) {
              l.add(Arrays.copyOfRange(input, blockStart, i));
              blockStart = i+pattern.length;
              i = blockStart;
              break;
           }
        }
        l.add(Arrays.copyOfRange(input, blockStart, input.length ));
        return l;
    }

    public void restoreChunk(byte[] chunk) {
        String chunkTxt = new String(chunk);
        List<byte[]> parts = split(chunk);
        String[] chunkpieces = chunkTxt.split(" ");

        String fileName = chunkpieces[3] + "_" + chunkpieces[4];

        String path = "Peers/dir" + (Integer.toString(peer_id)) + "/temp/" + chunkpieces[3];
        File directory = new File(path);
        directory.mkdir();

        File file = new File(path + "/" + fileName);
        try{
            file.getParentFile().mkdirs();
            file.createNewFile();
            OutputStream os  = new FileOutputStream(file);
            os.write(parts.get(1));
            os.close();
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        };
    }
}
