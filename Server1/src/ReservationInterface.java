
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ReservationInterface extends Remote{
    boolean registerUser(String username, String password, String firstName, String lastName, 
                         String email, String phone, boolean isAdmin) throws RemoteException;
    
    User login(String username, String password) throws RemoteException;
    
    void logout(String username) throws RemoteException;
    
    boolean deleteAccount(String username) throws RemoteException;
    
    boolean addShow(String title, String type, String description, 
                   List<ShowPerformance> performances, String adminUsername) throws RemoteException;
    
    boolean deactivateShow(int showId, String adminUsername) throws RemoteException;
    
    List<Show> searchShows(Map<String, String> searchCriteria) throws RemoteException;
    
    Show getShowDetails(int showId) throws RemoteException;
    
    boolean reserveTickets(int performanceId, int numTickets, String username) throws RemoteException;
    
    boolean completePayment(int reservationId, String cardHolder, String cardNumber) throws RemoteException;
    
    boolean cancelReservation(int reservationId, String username) throws RemoteException;
    
    List<Reservation> getUserReservations(String username) throws RemoteException;
    
  //  List<Show> getShows() throws RemoteException;
   
}
