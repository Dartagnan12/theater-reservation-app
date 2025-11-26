
import java.util.ArrayList;
import java.util.List;

public class Show implements java.io.Serializable {

    private int id;
    private String title;
    private String type;
    private String description;
    private List<ShowPerformance> performances;
    private boolean active;

    public Show(int id, String title, String type, String description, boolean active) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.description = description;
        this.active = active;
        this.performances = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<ShowPerformance> getPerformances() {
        return performances;
    }

    public void setPerformances(List<ShowPerformance> performances) {
        this.performances = performances;
    }

    public void addPerformance(ShowPerformance performance) {
        this.performances.add(performance);
    }
}
