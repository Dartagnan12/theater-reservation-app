
import java.util.Date;
import java.util.Random;

public class ShowPerformance implements java.io.Serializable {

    private String id;
    private int showId;
    private Date date;
    private String time;
    private int totalSeats;
    private int availableSeats;
    private double price;

    public ShowPerformance(int showId, Date date, String time, int totalSeats, double price) {
        this.id = getRandomId(6);
        this.showId = showId;
        this.date = date;
        this.time = time;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.price = price;
    }

    public ShowPerformance(String id, int showId, Date date, String time, int totalSeats, double price) {
        this.id = id;
        this.showId = showId;
        this.date = date;
        this.time = time;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.price = price;
    }
    
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    public static String getRandomId(int length) {
        Random random = new Random();

        int id = random.nextInt(99999999);
        return String.valueOf(id);
    }
}
