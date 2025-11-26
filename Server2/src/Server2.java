// Stylianopoulos Nikolaos icsd17182
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server2 {
    
    private ServerSocket serverSocket;
    private final int port = 5000;
    private boolean running = true;
    
    private Map<Integer, Show> shows;
    private Map<String, ShowPerformance> performances;
    
   
    private static final String SHOWS_FILE = "shows.txt";
    private static final String PERFORMANCES_FILE = "performances.txt";

    public Server2() {
        shows = new ConcurrentHashMap<>();
        performances = new ConcurrentHashMap<>();
        
        loadData();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Database Server started on port " + port);
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New connection from " + clientSocket.getInetAddress());
                    
                    ClientHandler handler = new ClientHandler(clientSocket, shows, performances);
                    Thread t = new Thread(handler);
                    t.start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop();
        }
    }
    
    private void saveData() {
        try {
            FileOutputStream fos = new FileOutputStream(SHOWS_FILE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            
            oos.writeObject(shows);
            
            oos.close();
            fos.close();
            System.out.println("Shows saved to file.");
            
            fos = new FileOutputStream(PERFORMANCES_FILE);
            oos = new ObjectOutputStream(fos);
            
            oos.writeObject(performances);
            
            oos.close();
            fos.close();
            System.out.println("Performances saved to file.");
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stop() {
        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        saveData();
        
        System.out.println("Server stopped");
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            File showsFile = new File(SHOWS_FILE);
            if (showsFile.exists()) {
                FileInputStream fis = new FileInputStream(showsFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                shows = (Map<Integer, Show>) ois.readObject();
                
                ois.close();
                fis.close();
                System.out.println("Shows loaded from file.");
            }
            
            File performancesFile = new File(PERFORMANCES_FILE);
            if (performancesFile.exists()) {
                FileInputStream fis = new FileInputStream(performancesFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                performances = (Map<String, ShowPerformance>) ois.readObject();
                
                ois.close();
                fis.close();
                System.out.println("Performances loaded from file.");
            }
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
   
    
    public static void main(String[] args) {
        Server2 server = new Server2();
        
        //start the server
        server.start();
    }
    
  
}