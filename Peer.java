import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;

public class Peer {
    public static void main(String[] args) {

        int portNumber = Integer.parseInt(args[0]);
        boolean init = Boolean.parseBoolean(args[1]);
       
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