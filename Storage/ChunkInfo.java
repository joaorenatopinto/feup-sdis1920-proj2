package Storage;

public class ChunkInfo {
    private int no;
    private String fileID;
    private int wanted_rep_degree;
    private int curr_rep_degree = 0;
    private int size;

    public ChunkInfo(int no, String fileID, int rep_degree, int size){
        this.no = no;
        this.fileID = fileID;
        this.size = size;
        this.wanted_rep_degree = rep_degree;
    }

    /**
     * @return the curr_rep_degree
     */
    public int getCurr_rep_degree() {
        return curr_rep_degree;
    }

    /**
     * @return the fileID
     */
    public String getFileID() {
        return fileID;
    }

    /**
     * @return the no
     */
    public int getNo() {
        return no;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the wanted_rep_degree
     */
    public int getWanted_rep_degree() {
        return wanted_rep_degree;
    }

    /**
     * @param curr_rep_degree the curr_rep_degree to set
     */
    public void setCurr_rep_degree(int curr_rep_degree) {
        this.curr_rep_degree = curr_rep_degree;
    }

    public void IncrementCurr_rep_degree() {
        this.curr_rep_degree += 1;
    }

    public void DecrementCurr_rep_degree() {
        this.curr_rep_degree -= 1;
    }

    @Override
    public String toString() {
        return "  FileID: " + fileID + "\n  ChunkNr: " + no  + "\n  Size: " + (size/1000) + "\n  Current Replication Degree: " + curr_rep_degree;
    }

    public String getChunkID(){
        return fileID + "_" + no;
    }
}
