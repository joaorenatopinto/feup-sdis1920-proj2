package Storage;

public class ChunkInfo {
  private int no;
  private String fileID;
  private int wantedRepDegree;
  private int currRepDegree = 0;
  private int size;

  /**
   * Chunk information.
   */
  public ChunkInfo(int no, String fileID, int repDegree, int size) {
    this.no = no;
    this.fileID = fileID;
    this.size = size;
    this.wantedRepDegree = repDegree;
  }

  /**
   * Return current replication degree.
   */
  public int getCurrRepDegree() {
    return currRepDegree;
  }

  /**
   * Return file id.
   */
  public String getFileID() {
    return fileID;
  }

  /**
   * Return chunk number.
   */
  public int getNo() {
    return no;
  }

  /**
   * Return chunk size.
   */
  public int getSize() {
    return size;
  }

  /**
   * Return wanted replication degree.
   */
  public int getWantedRepDegree() {
    return wantedRepDegree;
  }

  /**
   * Set current replication degree.
   */
  public void setCurrRepDegree(int currRepDegree) {
    this.currRepDegree = currRepDegree;
  }

  /**
   * Increment current replication degree.
   */
  public void incrementCurrRepDegree() {
    this.currRepDegree += 1;
  }

  /**
   * Decrement current replication degree.
   */
  public void decrementCurrRepDegree() {
    this.currRepDegree -= 1;
  }

  @Override
  public String toString() {
    return "  FileID: " + fileID + "\n  ChunkNr: " + no + "\n  Size: " + (size / 1000)
        + "\n  Current Replication Degree: " + currRepDegree;
  }

  public String getChunkID() {
    return fileID + "_" + no;
  }
}
