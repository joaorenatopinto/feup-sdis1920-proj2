import Storage.ChunkInfo;
import Storage.FileInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PeerMethods implements PeerInterface {
  public static int CHUNK_SIZE = 16000;
  public static double FILE_MAX_SIZE = 1000 * 1000000;

  /**
   * Divide file into chunks and store them.
   */
  public void backup(String path, int repDegree) {
    Peer.pool.execute(() -> {
      try {
        chunkify_file(path, repDegree);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * Receive chunks from peers and rebuild file.
   */
  public void restore(String path) {
    Peer.pool.execute(() -> {
      try {
        restoreFile(path);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * Delete file from network.
   */
  public void delete(String path) {
    Peer.pool.execute(() -> {
      try {
        deleteFile(path);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * Restore file.
   */
  public void restoreFile(String filePath) {
    FileInfo file = Peer.storage.getFileInfoByFilePath(filePath);
    if (file == null) {
      System.out.println("You can only restore files that have been previously backed up by the system.");
      return;
    }
    String fileId = file.getId();
    int numChunks = file.getChunks().size();

    for (int chunkNo = 1; chunkNo <= numChunks; chunkNo++) {
      try {
        byte[] chunk = restoreChunk(fileId, chunkNo, file.getRep_degree());
        if (chunk != null) {
          Peer.storage.restoreChunk(chunk);
        } else {
          System.out.println("Couldn't restore the chunk number " + chunkNo);
          return;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    dechunky_file(filePath);
  }

  /**
   * Delete file.
   */
  public void deleteFile(String filePath) throws IOException {
    FileInfo file = Peer.storage.getFileInfoByFilePath(filePath);
    if (file == null) {
      System.out.println("You can only delete files that have been previously backed up by the system.");
      return;
    }
    String fileId = file.getId();
    int numChunks = file.getChunks().size();

    for (int chunkNo = 1; chunkNo <= numChunks; chunkNo++) {
      try {
        boolean success = deleteChunk(fileId, chunkNo, file.getRep_degree());
        if (!success) {
          System.out.println("Couldn't delete the chunk number " + chunkNo);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    Peer.storage.removeBackedFile(file);
    System.out.println("All " + filePath + " chunks were deleted");
  }

  /**
   * Get reference to node with chunk chunk_no from file file_id.
   */
  private NodeReference getNode(String fileId, int chunkNo, int i) throws NoSuchAlgorithmException {
    BigInteger chunkChordId = getHash(fileId, chunkNo, i);
    NodeReference receiverNode = Peer.chordNode.findSuccessor(chunkChordId);
    System.out.println(">>> Chunk Hash: " + chunkChordId + " <<<");
    System.out.println(">>> Successor ID: " + receiverNode.id + " <<<");
    return receiverNode;
  }

  /**
   * Return contents of chunk of file.
   */
  public byte[] restoreChunk(String fileId, int chunkNo, int repDegree) throws IOException, NoSuchAlgorithmException {
    byte[] chunk = null;

    /* For each owner of a copy of the chunk */
    for (int i = 0; i < repDegree; i++) {
      NodeReference receiverNode = getNode(fileId, chunkNo, i);
      byte[] msg = MessageBuilder.getGetchunkMessage(fileId, chunkNo);

      try (SSLSocketStream socket = new SSLSocketStream(receiverNode.ip, receiverNode.port)) {
        /* Send GETCHUNK message */
        socket.write(msg);

        /* Get CHUNK message from peer */
        byte[] fromClient = new byte[65000];
        int msgSize;
        if ((msgSize = socket.read(fromClient)) != -1) {
          ByteArrayOutputStream message = new ByteArrayOutputStream();
          message.write(fromClient, 0, msgSize);
          String headerString = new String(fromClient).split("\\r?\\n")[0];
          if (headerString.equals("ERROR")) {
            System.out.println("Warning: error while restoring chunk");
            continue;
          } else {
            String[] tokens = headerString.split(" ");
            if (tokens[0].equals("PROTOCOL") && tokens[1].equals("CHUNK")) {
              if (tokens[2].equals(fileId) && tokens[3].equals(String.valueOf(chunkNo))) {
                chunk = message.toByteArray();
                break;
              } else {
                System.out.println("Warning: wrong chunk while restoring chunk");
                System.out
                    .println("   : received " + tokens[2] + "_" + tokens[3] + " | wanted" + fileId + "_" + chunkNo);
              }
            } else {
              System.out.println("Warning: wrong message while restoring chunk");
              System.out.println("   : received \"" + tokens[0] + " " + tokens[1] + "\" | wanted \"PROTOCOL CHUNK\"");
            }
          }
        }
      } catch (IOException e) {
        System.out.println("Exception thrown: " + e.getMessage());
      }
    }
    return chunk;
  }

  /**
   * Delete chunk from storage.
   */
  public boolean deleteChunk(String fileId, int chunkNo, int repDegree) throws IOException, NoSuchAlgorithmException {

    for (int i = 0; i < repDegree; i++) {
      NodeReference receiverNode = getNode(fileId, chunkNo, i);

      byte[] msg = MessageBuilder.getDeleteMessage(fileId, chunkNo);

      try (SSLSocketStream socket = new SSLSocketStream(receiverNode.ip, receiverNode.port)) {
        /* Send DELETE message */
        socket.write(msg);

        byte[] fromClient = new byte[65000];
        int msgSize;
        if ((msgSize = socket.read(fromClient)) != -1) {
          ByteArrayOutputStream message = new ByteArrayOutputStream();
          message.write(fromClient, 0, msgSize);
          if (new String(fromClient).equals("ERROR")) {
            continue;
          } else if (new String(fromClient).equals("SUCCESS")) {
            System.out.println("NICE!");
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

  /**
   * Divide file into chunks.
   */
  public void chunkify_file(String filePath, int repDegree) throws IOException, NoSuchAlgorithmException {
    // file handling
    InputStream is;
    try {
      is = new FileInputStream(filePath);
    } catch (FileNotFoundException e) {
      System.out.println("File not found");
      return;
    }

    Path path = Paths.get(filePath);
    if (path.toFile().length() > FILE_MAX_SIZE) {
      System.out.println("File too big, max size: 1GBytes");
      is.close();
      return;
    }
    FileInfo newFile = new FileInfo(filePath, repDegree);
    Peer.storage.addBackedFile(newFile);

    int chunkno = 1;
    int chunkSize = 0;
    byte[] b = new byte[CHUNK_SIZE];

    while ((chunkSize = is.read(b)) != -1) {
      ByteArrayOutputStream body = new ByteArrayOutputStream();
      body.write(b, 0, chunkSize);
      byte[] chunkBody = body.toByteArray();
      newFile.addChunk(new ChunkInfo(chunkno, newFile.getId(), repDegree, chunkBody.length));
      backupChunk(newFile.getId(), chunkno, repDegree, chunkBody, newFile);
      chunkno++;
    }
    is.close();
  }

  /**
   * Build file from chunks.
   */
  public void dechunky_file(String filePath) {
    FileInfo file = Peer.storage.getFileInfoByFilePath(filePath);
    String fileId = file.getId();
    int numChunks = file.getChunks().size();

    String restoredDirPath = "Peers/dir" + Integer.toString(Peer.id) + "/restored";
    File restoredPath = new File(restoredDirPath);
    restoredPath.mkdir();

    File restoredFile = new File(restoredDirPath + "/" + filePath.split("/")[filePath.split("/").length - 1]);
    try {
      restoredFile.createNewFile();
      OutputStream os = new FileOutputStream(restoredFile);
      for (int i = 1; i <= numChunks; i++) {
        File chunk = new File(
            "Peers/dir" + Integer.toString(Peer.id) + "/temp" + "/" + fileId + "/" + fileId + "_" + i);
        os.write(Files.readAllBytes(chunk.toPath()));
        chunk.delete();
        new File("Peers/dir" + Integer.toString(Peer.id) + "/temp" + "/" + fileId).delete();
      }
      os.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  /**
   * Store chunk in peer.
   */
  public void backupChunk(String fileId, int chunkNo, int repDegree, byte[] body, FileInfo file)
      throws NoSuchAlgorithmException, IOException {
    for (int i = 0; i < repDegree; i++) {
      NodeReference receiverNode = getNode(fileId, chunkNo, i);

      byte[] msg = MessageBuilder.getPutchunkMessage(fileId, chunkNo, body);
      try (SSLSocketStream socket = new SSLSocketStream(receiverNode.ip, receiverNode.port)) {
        /* Send PUTCHUNK message */
        socket.write(msg);

        String fromServer;
        if ((fromServer = socket.readLine()) != null) {
          if (fromServer.equals("SUCCESS")) {
            // If Node receives a sucess as answer we increment the chunk current
            // Replication Degree on the System.
            file.getChunkByNo(chunkNo).IncrementCurr_rep_degree();
          }
          if (fromServer.equals("ERROR")) {
            // TODO: IF ERROR AND HANDLE IT NEEDS A RETRY WITH SOME OTHER ALGORITHM
            System.out.print("ERROR: Peer couldn't store chunk.");
          }
        } else {
          System.out.println("ERROR: Backup answer was empty.");
        }
      } catch (IOException e) {
        System.out.println("Exception thrown: " + e.getMessage());
      }
    }
  }

  /**
   * Num sei.
   */
  public static boolean saveChunk(byte[] chunk) {
    long diff = (Peer.storage.getCurr_storage() + chunk.length) - Peer.storage.getMax_storage();
    if (diff > 0 && Peer.storage.getMax_storage() != -1) {
      // if (!manage_storage(diff, false)){
      System.out.println("No Space Available");
      return false;
      // }
    }
    Peer.storage.saveFile(chunk);
    return true;
  }

  /**
   * Get chunk from peer.
   */
  public static byte[] retrieveChunk(String fileId, int chunkNo) throws IOException {
    byte[] chunk = null;
    String key = fileId + "_" + chunkNo;

    Path file = Paths.get("Peers/dir" + Peer.id + "/" + key);
    byte[] fileData = Files.readAllBytes(file);
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    body.write(fileData);
    chunk = MessageBuilder.getChunkMessage(fileId, chunkNo, body.toByteArray());

    return chunk;
  }

  /**
   * Delete chunk.
   */
  public static boolean deleteSavedChunk(String fileId, int chunkNo) {
    String key = fileId + "_" + chunkNo;
    ChunkInfo chunk = Peer.storage.getStoredChunkInfo(fileId, chunkNo);
    if (chunk != null) {
      Path file = Paths.get("Peers/dir" + Peer.id + "/" + key);
      if (!file.toFile().delete()) {
        return false;
      }
      Peer.storage.removeStoredChunk(chunk);
      return true;
    }
    return false;
  }

  /**
   * Reclaim storage space.
   */
  public void space_reclaim(long newMaxStorage) throws IOException {
    newMaxStorage *= 1000;
    if (newMaxStorage < 0) {
      Peer.storage.setMax_storage(-1);
    } else {
      long spaceToFree = Peer.storage.getCurr_storage() - newMaxStorage;
      Peer.storage.setMax_storage(newMaxStorage);
      if (spaceToFree > 0) {
        manage_storage(spaceToFree, true);
      }
    }
    return;
  }

  /**
   * Delete chunks to free space.
   */
  public static boolean manage_storage(long spaceToFree, boolean mustDelete) throws IOException {
    // If Max Storage is -1 it means it is unlimited
    if (Peer.storage.getMax_storage() == -1) {
      return true;
    }
    int maxRepdegreeDif;
    long freedSpace = 0;
    ChunkInfo toRemove;
    while (freedSpace < spaceToFree) {
      maxRepdegreeDif = -10;
      toRemove = null;
      for (ChunkInfo chunk : Peer.storage.getChunks_Stored()) {
        int repDegreeDif = chunk.getCurr_rep_degree() - chunk.getWanted_rep_degree();
        if (repDegreeDif > maxRepdegreeDif) {
          maxRepdegreeDif = repDegreeDif;
          toRemove = chunk;
        }
      }
      if (mustDelete) {
        File chunkFile = new File("Peers/" + "dir" + Peer.id + "/" + toRemove.getChunkID());
        Peer.storage.RemoveFromCurr_storage(chunkFile.length());
        freedSpace += chunkFile.length();
        Peer.storage.removeStoredChunk(toRemove);
        // TODO: INFORM NODES THAT FILE WAS DELETED ???
        chunkFile.delete();
      } else if (maxRepdegreeDif > 0) {
        File chunkFile = new File("Peers/" + "dir" + Peer.id + "/" + toRemove);
        Peer.storage.RemoveFromCurr_storage(chunkFile.length());
        freedSpace += chunkFile.length();
        Peer.storage.removeStoredChunk(toRemove);
        // TODO: INFORM NODES THAT FILE WAS DELETED ???
        chunkFile.delete();
      } else {
        return false;
      }
    }
    return true;
  }

  /**
   * Print storage state.
   */
  public void print_state() {
    System.out.println("Files Backed Up:");
    for (FileInfo file : Peer.storage.getFiles_backed()) {
      System.out.println(file.toString());
    }
    System.out.println("\nChunks Stored:\n-");
    for (ChunkInfo chunkInfo : Peer.storage.getChunks_Stored()) {
      System.out.println(chunkInfo.toString());
      System.out.println('-');
    }
    if (Peer.storage.getMax_storage() == 0) {
      System.out.println("Storage Capacity: Unlimited");
    } else {
      System.out.println("Storage Capacity: " + (Peer.storage.getMax_storage() / 1000) + " KBytes");
    }
    System.out.println("Storage Used: " + (Peer.storage.getCurr_storage() / 1000) + " KBytes");
    return;
  }

  private BigInteger getHash(String fileId, int chunkNo, int copyNo) throws NoSuchAlgorithmException {
    String unhashedId = fileId + "_" + chunkNo + "_" + copyNo;
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] messageDigest = md.digest(unhashedId.getBytes());
    BigInteger toNum = new BigInteger(1, messageDigest);
    while (toNum.compareTo(new BigInteger("1000000000")) == 1) {
      toNum = toNum.divide(new BigInteger("10"));
    }
    return toNum;
  }
}
