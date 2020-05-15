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

  public FileInfo(String path, int rep_degree) {
    this.path = path;
    this.rep_degree = rep_degree;
    try {
      this.id = this.hasher(path);
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.chunks = Collections.synchronizedList(new ArrayList<ChunkInfo>());
  }

  private String hasher(String file_path) throws IOException, NoSuchAlgorithmException {
    Path path = Paths.get(file_path);
    ByteArrayOutputStream dataWMetaData = new ByteArrayOutputStream();
    StringBuilder hexString = new StringBuilder();

    BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
    FileInputStream fis = new FileInputStream(file_path);
    byte[] data = new byte[200000000];
    fis.read(data);
    fis.close();

    dataWMetaData.write(file_path.getBytes());
    dataWMetaData.write(attr.lastModifiedTime().toString().getBytes());
    dataWMetaData.write(data);

    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] encodedHash = digest.digest(dataWMetaData.toByteArray());
    for (byte b : encodedHash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1)
        hexString.append('0');
      hexString.append(hex);
    }

    return hexString.toString();
  }

  public void addChunk(ChunkInfo chunk) {
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

  public ChunkInfo getChunkByNo(int no) {
    Optional<ChunkInfo> result = chunks.stream().filter(chunk -> chunk.getNo() == no).findFirst();
    return result.orElse(null);
  }

  @Override
  public String toString() {
    StringBuilder aux = new StringBuilder("Path: " + path + "\n-\n  FileID: " + id + "\n  Desired Replication Degree: "
        + rep_degree + "\n  Chunks: " + chunks.size());
    for (ChunkInfo chunkInfo : chunks) {

      aux.append("\n-\n").append(chunkInfo.toString());

    }
    return aux.toString();
  }
}
