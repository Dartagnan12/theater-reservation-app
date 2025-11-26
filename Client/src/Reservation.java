
import java.util.Date;

public class Reservation implements java.io.Serializable {

    private int id;
    private String username;
    private int performanceId;
    private int numTickets;
    private double totalPrice;
    private boolean paid;
    private Date reservationDate;

    public Reservation(int id, String username, int performanceId, int numTickets, double totalPrice) {
        this.id = id;
        this.username = username;
        this.performanceId = performanceId;
        this.numTickets = numTickets;
        this.totalPrice = totalPrice;
        this.paid = false;
        this.reservationDate = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPerformanceId() {
        return performanceId;
    }

    public void setPerformanceId(int performanceId) {
        this.performanceId = performanceId;
    }

    public int getNumTickets() {
        return numTickets;
    }

    public void setNumTickets(int numTickets) {
        this.numTickets = numTickets;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public Date getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(Date reservationDate) {
        this.reservationDate = reservationDate;
    }
}
