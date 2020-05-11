import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

public class Peer {
    static public Node chordNode;
    static public boolean shutdown = false;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Peer peer = new Peer();
        peer.run(args);  
    }

    public void run(String[] args) throws NoSuchAlgorithmException {
        System.setProperty("javax.net.ssl.trustStore", "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        int portNumber = Integer.parseInt(args[1]);
        String ipAddress = args[2];
        String chordOption = args[3];

        chordNode = new Node(ipAddress, portNumber, this);

        if(chordOption.equalsIgnoreCase("CREATE")) {
            System.setProperty("javax.net.ssl.keyStore", "server.keys");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
            chordNode.create();
        }
        else if(chordOption.equalsIgnoreCase("JOIN")) {
            System.setProperty("javax.net.ssl.keyStore", "client.keys");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
            chordNode.join(args[4], Integer.parseInt(args[5]));
        }
        else {
            System.out.println("vai te foder burro do caralho");
            return;
        }
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
                PeerMethods peer = new PeerMethods();

                PeerInterface interfaceStub = (PeerInterface) UnicastRemoteObject.exportObject(peer, 0);
    
                Registry registry = LocateRegistry.getRegistry();
    
                registry.bind("Peer" + args[0], interfaceStub);
            } catch (Exception e) {
                e.getStackTrace();
                return;
            }
            System.err.println("Peer ready");
            ExecutorService pool = Executors.newFixedThreadPool(3);

            try {
                System.out.println("Waiting Connection...");
                while (true) {
                    SSLSocket clientSocket = (SSLSocket)serverSocket.accept();
                    //serverSocket.setNeedClientAuth(true);
                    System.out.println("Recebi cenas");
                    Runnable task = new MessageProcessor(clientSocket);
                    pool.execute(task);

                    if(shutdown){
                        pool.shutdown();
                        clientSocket.close();
                        serverSocket.close();
                        break;
                    }
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
    }
}   
