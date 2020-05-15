package Storage;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FileInfo {
  private String id;
  private final String path;
  private final int repDegree;
  private final List<ChunkInfo> chunks;

  /**
   * Return id.
   */
  public FileInfo(final String path, final int repDegree) {
    this.path = path;
    this.repDegree = repDegree;
    try {
      this.id = this.hasher(path);
    } catch (final Exception e) {
      e.printStackTrace();
    }

    this.chunks = Collections.synchronizedList(new ArrayList<ChunkInfo>());
  }

  private String hasher(final String filePath) throws IOException, NoSuchAlgorithmException {
    final Path path = Paths.get(filePath);
    final ByteArrayOutputStream dataWMetaData = new ByteArrayOutputStream();
    final StringBuilder hexString = new StringBuilder();

    final BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
    final FileInputStream fis = new FileInputStream(filePath);
    final byte[] data = new byte[200000000];
    fis.read(data);
    fis.close();

    dataWMetaData.write(filePath.getBytes());
    dataWMetaData.write(attr.lastModifiedTime().toString().getBytes());
    dataWMetaData.write(data);

    final MessageDigest digest = MessageDigest.getInstance("SHA-256");
    final byte[] encodedHash = digest.digest(dataWMetaData.toByteArray());
    for (final byte b : encodedHash) {
      final String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }

    return hexString.toString();
  }

  public void addChunk(final ChunkInfo chunk) {
    this.chunks.add(chunk);
  }

  /**
   * Return id.
   */
  public String getId() {
    return id;
  }

  /**
   * Return chunks.
   */
  public List<ChunkInfo> getChunks() {
    return chunks;
  }

  /**
   * Return replication degree.
   */
  public int getRepDegree() {
    return repDegree;
  }

  /**
   * Return path.
   */
  public String getPath() {
    return path;
  }

  /**
   * Return chunk.
   */
  public ChunkInfo getChunkByNo(final int no) {
    final Optional<ChunkInfo> result = chunks.stream()
        .filter(chunk -> chunk.getNo() == no).findFirst();
    return result.orElse(null);
  }

  @Override
  public String toString() {
    final StringBuilder aux = new StringBuilder("Path: " + path + "\n-\n  FileID: " + id
        + "\n  Desired Replication Degree: " + repDegree + "\n  Chunks: " + chunks.size());
    for (final ChunkInfo chunkInfo : chunks) {

      aux.append("\n-\n").append(chunkInfo.toString());

    }
    return aux.toString();
  }
}
