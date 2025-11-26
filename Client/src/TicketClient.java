// Stylianopoulos Nikolaos icsd17182
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicketClient {
    
    ReservationInterface service;
    
    public static void main(String[] args) {
        System.out.println("hi");
        TicketClient tick = new TicketClient();
        try {
            tick.connectToServer();
        } catch (RemoteException ex) {
            Logger.getLogger(TicketClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(TicketClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void connectToServer() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        System.out.println("ok");
        service = (ReservationInterface) registry.lookup("ReservationService");
        
        
        System.out.println("Connected to server successfully");
        new RegisterFrame(service);
    }
}
