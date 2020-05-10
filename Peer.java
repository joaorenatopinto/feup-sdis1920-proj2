import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.InputStreamReader;
import java.util.Scanner;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

public class Peer {
    static Node chordNode;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Peer peer = new Peer();
        peer.run(args);  
    }

    public void run(String[] args) throws NoSuchAlgorithmException {
        System.setProperty("javax.net.ssl.trustStore", "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

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

            System.setProperty("javax.net.ssl.keyStore", "server.keys");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");

            SSLServerSocket serverSocket = null;  
            SSLServerSocketFactory ssf = null;  
            
            ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();  
            
            try {  
                serverSocket = (SSLServerSocket) ssf.createServerSocket(portNumber);  
            }  
            catch( IOException e) {  
                System.out.println("Server - Failed to create SSLServerSocket");  
                e.getMessage();  
                return;  
            } 


            try {
                System.out.println("Waiting Connection...");
                while (true) {
                        SSLSocket clientSocket = (SSLSocket)serverSocket.accept();
                        serverSocket.setNeedClientAuth(true);
                        System.out.println("Connected.");
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
                        //String inputLine, outputLine;
                        System.out.println("Client: " + in.readLine());
                        out.println("Pega uma resposta estilo Afonso: ASHOEARHWHESLDSFHSOF");
                        out.println("Bye.");
                        break;
                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
       } else {

            System.setProperty("javax.net.ssl.keyStore", "client.keys");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");

            Scanner myObj = new Scanner(System.in);  // Create a Scanner object
           
            SSLSocket Socket = null;  
            
            String hostName = "localhost";

            try {
                SSLSocketFactory factory =  (SSLSocketFactory)SSLSocketFactory.getDefault();
                Socket = (SSLSocket) factory.createSocket(hostName,portNumber);  
                
                Socket.startHandshake();

                PrintWriter out = new PrintWriter(Socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));

                String fromServer, fromUser;

                out.println("Ola server!");

                while ((fromServer = in.readLine()) != null) {
                    System.out.println("Server: " + fromServer);
                    if (fromServer.equals("Bye."))
                       break;

                    System.out.println("Enter message: ");

                    fromUser = myObj.nextLine();  // Read user input
                    if (fromUser != null) {
                        System.out.println("You: " + fromUser);
                        out.println(fromUser);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
    }
}
