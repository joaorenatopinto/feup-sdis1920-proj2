

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Storage {
  private long maxStorage;
  private long currStorage;

  private List<FileInfo> filesBacked;
  private List<ChunkInfo> chunksStored;
  private int peerId;

  /**
   * Storage constructor.
   */
  public Storage(int peerId) {
    this.peerId = peerId;
    this.maxStorage = -1;
    this.currStorage = 0;
    this.filesBacked = Collections.synchronizedList(new ArrayList<FileInfo>());
    this.chunksStored = Collections.synchronizedList(new ArrayList<ChunkInfo>());
  }

  /**
   * Get maximum capacity for file storage.
   *
   * @return maximum size of storage
   */
  public long getMaxStorage() {
    return maxStorage;
  }

  /**
   * Get the size of occupied storage.
   *
   * @return occupied storage size
   */
  public long getCurrStorage() {
    return currStorage;
  }

  /**
   * Set maximum storage size.
   * @param maxStorage the maxStorage to set
   */
  public void setMaxStorage(long maxStorage) {
    this.maxStorage = maxStorage;
  }

  /**
   * Set current storage size.
   * @param currStorage the currStorage to set
   */
  public void setCurrStorage(long currStorage) {
    this.currStorage = currStorage;
  }

  public void addToCurrStorage(long storage) {
    this.currStorage = currStorage + storage;
  }

  public void removeFromCurrStorage(long storage) {
    this.currStorage = currStorage - storage;
  }

  /**
   * Get stored chunks.
   * @return the chunksStored
   */
  public List<ChunkInfo> getChunksStored() {
    return chunksStored;
  }

  /**
   * Get files backed up.
   * @return the filesBacked
   */
  public List<FileInfo> getFilesBacked() {
    return filesBacked;
  }

  /**
   * Get file.
   */
  public FileInfo getFileInfo(String fileID) {
    try {
      Optional<FileInfo> result = filesBacked.stream().filter(file -> file.getId()
          .equals(fileID)).findFirst();
      if (result.isPresent()) {
        return result.get();
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  /**
   * Get file.
   */
  public FileInfo getFileInfoByFilePath(String filePath) {
    Optional<FileInfo> result = filesBacked.stream().filter(file -> file.getPath()
        .equals(filePath)).findFirst();
    return result.orElse(null);
  }

  /**
   * Get stored chunk.
   */
  public ChunkInfo getStoredChunkInfo(String fileID, int chunkNo) {
    Optional<ChunkInfo> result = chunksStored.stream()
        .filter(ch -> ((ch.getFileID().equals(fileID)) && (ch.getNo() == chunkNo))).findFirst();
    return result.orElse(null);
  }

  public void addBackedFile(FileInfo file) {
    filesBacked.add(file);
  }

  public void addStoredChunk(ChunkInfo chunk) {
    chunksStored.add(chunk);
  }

  public void removeBackedFile(FileInfo file) {
    filesBacked.remove(file);
  }

  public void removeStoredChunk(ChunkInfo chunk) {
    chunksStored.remove(chunk);
  }

  /**
   * Save file.
   */
  public void saveFile(byte[] chunk) {
    String path = "Peers/dir" + (peerId);
    File directory = new File(path);

    directory.mkdir();
    String chunkTxt = new String(chunk);
    String[] chunkpieces = chunkTxt.split("\\s+|\n");
    List<byte[]> parts = split(chunk);
    String fileName = chunkpieces[2] + "_" + chunkpieces[3] + "_" + chunkpieces[4];

    File file = new File(path + "/" + fileName);
    try {
      file.getParentFile().mkdirs();
      file.createNewFile();
      OutputStream os = new FileOutputStream(file);
      os.write(parts.get(1));
      this.addStoredChunk(new ChunkInfo(Integer.parseInt(chunkpieces[3]),
          chunkpieces[2], 0, (int) file.length()));
      this.addToCurrStorage(file.length());
      os.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  /**
   * Patter match.
   */
  public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
    for (int i = 0; i < pattern.length; i++) {
      if (pattern[i] != input[pos + i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Split.
   */
  public static List<byte[]> split(byte[] input) {
    byte[] pattern = "\r\n\r\n".getBytes();
    List<byte[]> l = new LinkedList<>();
    int blockStart = 0;
    for (int i = 0; i < input.length; i++) {
      if (isMatch(pattern, input, i)) {
        l.add(Arrays.copyOfRange(input, blockStart, i));
        blockStart = i + pattern.length;
        i = blockStart;
        break;
      }
    }
    l.add(Arrays.copyOfRange(input, blockStart, input.length));
    return l;
  }

  /**
   * Restore chunk.
   */
  public void restoreChunk(byte[] chunk) {
    String chunkTxt = new String(chunk);
    List<byte[]> parts = split(chunk);
    String[] chunkpieces = chunkTxt.split("\\s+|\n");

    String fileName = chunkpieces[2] + "_" + chunkpieces[3];

    String path = "Peers/dir" + (peerId) + "/temp/" + chunkpieces[2];
    File directory = new File(path);
    directory.mkdir();

    File file = new File(path + "/" + fileName);
    try {
      file.getParentFile().mkdirs();
      file.createNewFile();
      OutputStream os = new FileOutputStream(file);
      os.write(parts.get(1));
      os.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
