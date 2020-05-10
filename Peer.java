import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.io.InputStreamReader;

public class Peer {
    static Node chordNode;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        int portNumber = Integer.parseInt(args[0]);
        String ipAddress = args[1];
        String chordOption = args[2];

        chordNode = new Node(ipAddress, portNumber);

        boolean init;
        if(chordOption.equals("CREATE")) {
            chordNode.create();
            init = Boolean.parseBoolean(args[3]);
        }
        else if(chordOption.equals("JOIN")) {
            chordNode.join(args[3], Integer.parseInt(args[4]));
            init = Boolean.parseBoolean(args[5]);
        }
        else {
            System.out.println("vai te foder burro do caralho");
            return;
        }
        
       
        if(!init){
            try {
                ServerSocket serverSocket = new ServerSocket(portNumber);
                System.out.println("Waiting Connection...");
                while (true) {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Connected.");
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
                        //String inputLine, outputLine;
                        System.out.println(in.readLine());
                        out.println("Pega uma resposta estilo Afonso: ASHOEARHWHESLDSFHSOF");
                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
       }
        
    }
}