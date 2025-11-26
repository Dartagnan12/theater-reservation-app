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
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server1 extends UnicastRemoteObject implements ReservationInterface {
    
    private Map<String, User> users; 
    private Map<Integer, Show> shows; 
    private Map<String, ShowPerformance> performances; 
    private Map<Integer, Reservation> reservations; 
    
    //locks gia tin mi tautoxroni prospelasi porwn
    private final ReentrantReadWriteLock usersLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock showsLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock performancesLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock reservationsLock = new ReentrantReadWriteLock();
    private Socket dbSocket;
    private PrintWriter out;
    private BufferedReader in;
    
    private static final String DB_SERVER_HOST = "localhost";
    private static final int DB_SERVER_PORT = 5000;
    private static final String USERS_FILE = "users.txt";
    
    public Server1() throws RemoteException {
        super();
        users = new HashMap<>();
        shows = new HashMap<>();
        performances = new HashMap<>();
        reservations = new HashMap<>();
        
        //diavasma xristwn apo to arxeio
        loadUsers();
        
        try {
            connectToDatabaseServer();
        } catch (IOException e) {
            System.err.println("Error connecting to database server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void connectToDatabaseServer() throws IOException {
        dbSocket = new Socket(DB_SERVER_HOST, DB_SERVER_PORT);
        out = new PrintWriter(dbSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(dbSocket.getInputStream()));
        System.out.println("Connected to database server.");
    }
    
    // Send request to database server and get response
    private String sendDbRequest(String request) throws IOException {
        // Check if connection is alive, if not reconnect
        if (dbSocket == null || dbSocket.isClosed() || !dbSocket.isConnected()) {
            connectToDatabaseServer();
        }
        
        out.println(request);
        return in.readLine();
    }
    
    // Load users from file
    private void loadUsers() {
        try {
            File file = new File(USERS_FILE);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                users = (Map<String, User>) ois.readObject();
                
                ois.close();
                fis.close();
                System.out.println("Users loaded from file.");
            } 
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Save users to file
    private void saveUsers() {
        try {
            FileOutputStream fos = new FileOutputStream(USERS_FILE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            
            oos.writeObject(users);
            
            oos.close();
            fos.close();
            System.out.println("Users saved to file.");
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // User Authentication Methods
    @Override
    public boolean registerUser(String username, String password, String firstName, String lastName, 
                               String email, String phone, boolean isAdmin) throws RemoteException {
        usersLock.writeLock().lock();
        try {
            // Check if username already exists
            if (users.containsKey(username)) {
                return false;
            }
            
            // Create new user
            User newUser = new User(username, password, firstName, lastName, email, phone, isAdmin);
            users.put(username, newUser);
            
            // Save users to file
            saveUsers();
            
            return true;
        } finally {
            usersLock.writeLock().unlock();
        }
    }
    
    @Override
    public User login(String username, String password) throws RemoteException {
        //elegxos ean uparxei to username
        if (!users.containsKey(username)) {
            return null;
        }

        //elegxos ean to password einai swsto
        User user = users.get(username);
        if (user.getPassword().equals(password))
            return user;
        else return null;
        
    }
    
    @Override
    public void logout(String username) throws RemoteException {
        System.out.println("User logged out: " + username);
    }
    
    @Override
    public boolean deleteAccount(String username) throws RemoteException {
        usersLock.writeLock().lock();
        try {
            // Check if username exists
            if (!users.containsKey(username)) {
                return false;
            }
            
            // Remove user
            users.remove(username);
            
            // Save users to file
            saveUsers();
            
            return true;
        } finally {
            usersLock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean addShow(String title, String type, String description, 
                          List<ShowPerformance> showPerformances, String adminUsername) throws RemoteException {
        usersLock.readLock().lock();
        try {
            System.out.println(users.containsKey(adminUsername));
            System.out.println(users.get(adminUsername).isAdmin());
            if (!users.containsKey(adminUsername) || !users.get(adminUsername).isAdmin()) {
                System.out.println("edw1");
                return false;
            }
        } finally {
            usersLock.readLock().unlock();
        }
        
        try {
            int showId = getNextShowId();
            Show newShow = new Show(showId, title, type, description, true);
            
            StringBuilder showData = new StringBuilder();
            showData.append("ADD_SHOW|").append(showId).append("|")
                   .append(title).append("|").append(type).append("|")
                   .append(description).append("|1");
            
            String response = sendDbRequest(showData.toString());
            
            if (response.equals("SUCCESS")) {
                for (ShowPerformance performance : showPerformances) {
                   
                    String performanceId = performance.getId();
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = dateFormat.format(performance.getDate());
                    
                    StringBuilder perfData = new StringBuilder();
                    perfData.append("ADD_PERFORMANCE|").append(performanceId).append("|")
                           .append(showId).append("|").append(formattedDate).append("|")
                           .append(performance.getTime()).append("|")
                           .append(performance.getTotalSeats()).append("|")
                           .append(performance.getPrice());
                    
                    String perfResponse = sendDbRequest(perfData.toString());
                    
                    if (perfResponse.equals("SUCCESS")) {
                        showsLock.writeLock().lock();
                        performancesLock.writeLock().lock();
                        try {
                            performances.put(performanceId, performance);
                            newShow.addPerformance(performance);
                        } finally {
                            performancesLock.writeLock().unlock();
                            showsLock.writeLock().unlock();
                        }
                    } else {
                        System.out.println("edw2");
                        return false;
                    }
                }
                
                showsLock.writeLock().lock();
                try {
                    shows.put(showId, newShow);
                } finally {
                    showsLock.writeLock().unlock();
                }
                
                return true;
            } else {
                System.out.println("edw3");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error adding show: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deactivateShow(int showId, String adminUsername) throws RemoteException {
        usersLock.readLock().lock();
        try {
            if (!users.containsKey(adminUsername) || !users.get(adminUsername).isAdmin()) {
                return false;
            }
        } finally {
            usersLock.readLock().unlock();
        }
        
        showsLock.writeLock().lock();
        try {
            //elegxos ean to show yparxei
            if (!shows.containsKey(showId)) {
                return false;
            }
            
            //apenergopoiisi toy show
            shows.get(showId).setActive(false);
            
            try {
                // Send request to database server
                String request = "DEACTIVATE_SHOW|" + showId;
                String response = sendDbRequest(request);
                
                return response.equals("SUCCESS");
            } catch (IOException e) {
                System.err.println("Error deactivating show: " + e.getMessage());
                e.printStackTrace();
                // Revert the change if failed
                shows.get(showId).setActive(true);
                return false;
            }
        } finally {
            showsLock.writeLock().unlock();
        }
    }
    
    @Override
    public List<Show> searchShows(Map<String, String> searchCriteria) throws RemoteException {
        List<Show> results = new ArrayList<>();
        
        try {
            StringBuilder searchRequest = new StringBuilder("SEARCH_SHOWS");
            
            for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
                searchRequest.append("|").append(entry.getKey())
                            .append("=").append(entry.getValue());
            }
            
            String response = sendDbRequest(searchRequest.toString());
            
            if (!response.equals("NO_RESULTS")) {
                String[] showIds = response.split("\\|");
                
                for (String idStr : showIds) {
                    int showId = Integer.parseInt(idStr);
                    
                    //eyresi pliroforiwn toy show
                    String showRequest = "GET_SHOW|" + showId;
                    String showResponse = sendDbRequest(showRequest);
                    
                    if (!showResponse.equals("NOT_FOUND")) {
                        String[] showData = showResponse.split("\\|");
                        int id = Integer.parseInt(showData[0]);
                        String title = showData[1];
                        String type = showData[2];
                        String description = showData[3];
                        boolean active = Integer.parseInt(showData[4]) == 1;
                     
                        Show show = new Show(id, title, type, description, active);
                        
                        //anaktisi tou show
                        String perfRequest = "GET_PERFORMANCES|" + showId;
                        String perfResponse = sendDbRequest(perfRequest);
                        
                        if (!perfResponse.equals("NO_RESULTS")) {
                            String[] perfIds = perfResponse.split("\\|");
                            
                            for (String perfIdStr : perfIds) {
                                
                                // Get performance details
                                String perfDetailsRequest = "GET_PERFORMANCE|" + perfIdStr;
                                String perfDetailsResponse = sendDbRequest(perfDetailsRequest);
                                //ean den vrethun oi plirofories tou show
                                if (!perfDetailsResponse.equals("NOT_FOUND")) {
                                    
                                    System.out.println(perfDetailsResponse);
                                    String[] perfData = perfDetailsResponse.split("\\|");
                                    int showIdFromDb = Integer.parseInt(perfData[1]);
                                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(perfData[2]);
                                    String time = perfData[3];
                                    int totalSeats = Integer.parseInt(perfData[4]);
                                    int availableSeats = Integer.parseInt(perfData[5]);
                                    double price = Double.parseDouble(perfData[6]);
                                    
                                    ShowPerformance performance = new ShowPerformance(
                                        showIdFromDb, date, time, totalSeats, price);
                                    performance.setAvailableSeats(availableSeats);
                                    
                                    show.addPerformance(performance);
                                }
                            }
                        }
                        
                        results.add(show);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error searching shows: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    @Override
    public Show getShowDetails(int showId) throws RemoteException {
        try {
            String showRequest = "GET_SHOW|" + showId;
            String showResponse = sendDbRequest(showRequest);
            
            if (!showResponse.equals("NOT_FOUND")) {
                String[] showData = showResponse.split("\\|");
                int id = Integer.parseInt(showData[0]);
                String title = showData[1];
                String type = showData[2];
                String description = showData[3];
                boolean active = Integer.parseInt(showData[4]) == 1;
                
                //dimiourgia tou show
                Show show = new Show(id, title, type, description, active);
                
                //pairnume ta performance gia to show
                String perfRequest = "GET_PERFORMANCES|" + showId;
                String perfResponse = sendDbRequest(perfRequest);
                
                if (!perfResponse.equals("NO_RESULTS")) {
                    String[] perfIds = perfResponse.split("\\|");
                    
                    for (String perfIdStr : perfIds) {
                        //anaktisi stoixeiwn performance
                        String perfDetailsRequest = "GET_PERFORMANCE|" + perfIdStr;
                        String perfDetailsResponse = sendDbRequest(perfDetailsRequest);
                        System.out.println("RESP " + perfDetailsResponse);
                        if (!perfDetailsResponse.equals("NOT_FOUND")) {
                            String[] perfData = perfDetailsResponse.split("\\|");
                            String performanceId = perfData[0];
                            int showIdFromDb = Integer.parseInt(perfData[1]);
                            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(perfData[2]);
                            String time = perfData[3];
                            int totalSeats = Integer.parseInt(perfData[4]);
                            int availableSeats = Integer.parseInt(perfData[5]);
                            double price = Double.parseDouble(perfData[6]);
                            
                            ShowPerformance performance = new ShowPerformance(
                                showIdFromDb, date, time, totalSeats, price);
                            performance.setAvailableSeats(availableSeats);
                            
                            show.addPerformance(performance);
                        }
                    }
                }
                
                return show;
            }
        } catch (Exception e) {
            System.err.println("Error getting show details: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override
    public boolean reserveTickets(int performanceId, int numTickets, String username) throws RemoteException {
        performancesLock.writeLock().lock();
        try {
            //elegxos ean to performance iparxei
            String perfRequest = "GET_PERFORMANCE|" + performanceId;
            String perfResponse = sendDbRequest(perfRequest);
            
            if (perfResponse.equals("NOT_FOUND")) {
                return false;
            }
            
            String[] perfData = perfResponse.split("\\|");
            int availableSeats = Integer.parseInt(perfData[5]);
            
            //tha prepei na uparxoun eparkeis theseis
            if (availableSeats < numTickets) {
                return false;
            }
            
            //dimiourgia tis kratisis kai twn stoixeiwn
            int reservationId = getNextReservationId();
            double price = Double.parseDouble(perfData[6]);
            double totalPrice = price * numTickets;
            
            //ananewsi twn thesewn
            String updateRequest = "UPDATE_PERFORMANCE_SEATS|" + performanceId + "|" + (availableSeats - numTickets);
            String updateResponse = sendDbRequest(updateRequest);
            
            if (!updateResponse.equals("SUCCESS")) {
                return false;
            }
            
            //prosthiki kratisis
            Reservation reservation = new Reservation(reservationId, username, performanceId, numTickets, totalPrice);
            
            reservationsLock.writeLock().lock();
            try {
                reservations.put(reservationId, reservation);
            } finally {
                reservationsLock.writeLock().unlock();
            }
          
            return true;
        } catch (Exception e) {
            System.err.println("Error reserving tickets: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            performancesLock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean completePayment(int reservationId, String cardHolder, String cardNumber) throws RemoteException {
        reservationsLock.writeLock().lock();
        try {
            //tha prepeie na uparxei i kratisi
            if (!reservations.containsKey(reservationId)) {
                return false;
            }
            
            //i kratisi einai plirwmeni
            Reservation reservation = reservations.get(reservationId);
            reservation.setPaid(true);
         
            return true;
        } finally {
            reservationsLock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean cancelReservation(int reservationId, String username) throws RemoteException {
        reservationsLock.writeLock().lock();
        try {
            //yparxei i kratisi?
            if (!reservations.containsKey(reservationId)) {
                return false;
            }
            
            Reservation reservation = reservations.get(reservationId);
            
            //tha prepei i kratisi na anoikei se kapoion xristi
            if (!reservation.getUsername().equals(username)) {
                return false;
            }
            
            //anaktisi id gia tin kratisi gia elegxo 
            int performanceId = reservation.getPerformanceId();
            
            String perfRequest = "GET_PERFORMANCE|" + performanceId;
            String perfResponse = sendDbRequest(perfRequest);
            
            if (perfResponse.equals("NOT_FOUND")) {
                return false;
            }
            
            String[] perfData = perfResponse.split("\\|");
            Date performanceDate = new SimpleDateFormat("yyyy-MM-dd").parse(perfData[2]);
            
            //euresi simerinis imerominias
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            Calendar perfDay = Calendar.getInstance();
            perfDay.setTime(performanceDate);
            perfDay.set(Calendar.HOUR_OF_DAY, 0);
            perfDay.set(Calendar.MINUTE, 0);
            perfDay.set(Calendar.SECOND, 0);
            perfDay.set(Calendar.MILLISECOND, 0);
            
            //den prepei na epitrepetai akurwsi tin idia mera
            if (perfDay.equals(today)) {
                return false;
            }
            
            //ananewsi thesewn
            int availableSeats = Integer.parseInt(perfData[5]);
            int numTickets = reservation.getNumTickets();
            
            String updateRequest = "UPDATE_PERFORMANCE_SEATS|" + performanceId + "|" + (availableSeats + numTickets);
            String updateResponse = sendDbRequest(updateRequest);
            
            if (!updateResponse.equals("SUCCESS")) {
                return false;
            }
            
            //diagrafi kratisis
            reservations.remove(reservationId);
            
            return true;
        } catch (Exception e) {
            System.err.println("Error cancelling reservation: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            reservationsLock.writeLock().unlock();
        }
    }
    
    @Override
    public List<Reservation> getUserReservations(String username) throws RemoteException {
        List<Reservation> userReservations = new ArrayList<>();
        
        reservationsLock.readLock().lock();
        try {
            for (Reservation reservation : reservations.values()) {
                if (reservation.getUsername().equals(username)) {
                    userReservations.add(reservation);
                }
            }
        } finally {
            reservationsLock.readLock().unlock();
        }
        
        return userReservations;
    }
    
    
    private int nextShowId = 1;
    private int nextPerformanceId = 1;
    private int nextReservationId = 1;
    
    private synchronized int getNextShowId() {
        return nextShowId++;
    }
   
    
    private synchronized int getNextReservationId() {
        return nextReservationId++;
    }
    
   
    public static void main(String[] args) {
        try {
            //dimiourgia tou antikeimenou 
            ReservationInterface server = new Server1();
            
            //Dimiourgia tis registry
            Registry registry = LocateRegistry.createRegistry(1099);
            
            //anoigoume tin dieuthisni stin opoia tha akouei o server1
            registry.rebind("ReservationService", server);
            
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}