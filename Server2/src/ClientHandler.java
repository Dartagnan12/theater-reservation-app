
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Map<Integer, Show> shows;
    private Map<String, ShowPerformance> performances;
    
    private static final String SHOWS_FILE = "shows.dat";
    private static final String PERFORMANCES_FILE = "performances.dat";

    public ClientHandler(Socket socketprivate, Map<Integer, Show> shows,Map<String, ShowPerformance> performances) {
        this.shows = shows;
        this.performances = performances;
        this.clientSocket = socketprivate;
    }

    @Override
    public void run() {
        try {
            // Set up input and output streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                // Process request
                String response = processRequest(line);

                // Send response
                out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client streams: " + e.getMessage());
            }
        }
    }

    // Process client request
    private String processRequest(String request) {
        System.out.println("Received: " + request);

        String[] parts = request.split("\\|");
        String command = parts[0];

        try {
            switch (command) {
                case "ADD_SHOW":
                    return addShow(parts);
                case "DEACTIVATE_SHOW":
                    return deactivateShow(parts);
                case "GET_SHOW":
                    return getShow(parts);
                case "SEARCH_SHOWS":
                    return searchShows(parts);
                case "ADD_PERFORMANCE":
                    return addPerformance(parts);
                case "GET_PERFORMANCE":
                    return getPerformance(parts);
                case "GET_PERFORMANCES":
                    return getPerformances(parts);
                case "UPDATE_PERFORMANCE_SEATS":
                    return updatePerformanceSeats(parts);
                default:
                    return "INVALID_COMMAND";
            }
        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
            e.printStackTrace();
            return "ERROR";
        }
    }

    // Add a new show
    private String addShow(String[] parts) {
        try {
            int id = Integer.parseInt(parts[1]);
            String title = parts[2];
            String type = parts[3];
            String description = parts[4];
            boolean active = Integer.parseInt(parts[5]) == 1;

            Show show = new Show(id, title, type, description, active);

            synchronized (shows) {
                shows.put(id, show);
            }

            saveData();

            return "SUCCESS";
        } catch (Exception e) {
            System.err.println("Error adding show: " + e.getMessage());
            return "ERROR";
        }
    }

    // Deactivate a show
    private String deactivateShow(String[] parts) {
        try {
            int id = Integer.parseInt(parts[1]);

            synchronized (shows) {
                if (shows.containsKey(id)) {
                    shows.get(id).setActive(false);
                    saveData();
                    return "SUCCESS";
                } else {
                    return "NOT_FOUND";
                }
            }
        } catch (Exception e) {
            System.err.println("Error deactivating show: " + e.getMessage());
            return "ERROR";
        }
    }
    
     // Save data to files
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

    // Get show details
    private String getShow(String[] parts) {
        try {
            int id = Integer.parseInt(parts[1]);

            synchronized (shows) {
                if (shows.containsKey(id)) {
                    Show show = shows.get(id);
                    return id + "|"
                            + show.getTitle() + "|"
                            + show.getType() + "|"
                            + show.getDescription() + "|"
                            + (show.isActive() ? "1" : "0");
                } else {
                    return "NOT_FOUND";
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting show: " + e.getMessage());
            return "ERROR";
        }
    }

    // Search shows
    private String searchShows(String[] parts) {
        try {
            Map<String, String> criteria = new HashMap<>();

            for (int i = 1; i < parts.length; i++) {
                String[] criterionParts = parts[i].split("=");
                if (criterionParts.length == 2) {
                    criteria.put(criterionParts[0], criterionParts[1]);
                }
            }

            List<Integer> matchingIds = new ArrayList<>();

            synchronized (shows) {
                for (Show show : shows.values()) {
                    if (show.isActive() && matchesCriteria(show, criteria)) {
                        matchingIds.add(show.getId());
                    }
                }
            }

            if (matchingIds.isEmpty()) {
                return "NO_RESULTS";
            } else {
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < matchingIds.size(); i++) {
                    if (i > 0) {
                        result.append("|");
                    }
                    result.append(matchingIds.get(i));
                }
                return result.toString();
            }
        } catch (Exception e) {
            System.err.println("Error searching shows: " + e.getMessage());
            return "ERROR";
        }
    }

    // Check if show matches search criteria
    private boolean matchesCriteria(Show show, Map<String, String> criteria) {
        for (Map.Entry<String, String> entry : criteria.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toLowerCase();

            switch (key) {
                case "title":
                    if (!show.getTitle().toLowerCase().contains(value)) {
                        return false;
                    }
                    break;
                case "type":
                    if (!show.getType().toLowerCase().contains(value)) {
                        return false;
                    }
                    break;
                case "startDate":
                case "endDate":
                        // Date filters would check performances
                        try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    Date filterDate = dateFormat.parse(value);

                    boolean hasMatchingPerformance = false;
                    for (ShowPerformance performance : getPerformancesForShow(show.getId())) {
                        Date performanceDate = performance.getDate();

                        if (key.equals("startDate") && performanceDate.compareTo(filterDate) >= 0) {
                            hasMatchingPerformance = true;
                            break;
                        } else if (key.equals("endDate") && performanceDate.compareTo(filterDate) <= 0) {
                            hasMatchingPerformance = true;
                            break;
                        }
                    }

                    if (!hasMatchingPerformance) {
                        return false;
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing date: " + e.getMessage());
                }
                break;
            }
        }

        return true;
    }

    // Get performances for a show
    private List<ShowPerformance> getPerformancesForShow(int showId) {
        List<ShowPerformance> result = new ArrayList<>();

        synchronized (performances) {
            for (ShowPerformance performance : performances.values()) {
                if (performance.getShowId() == showId) {
                    result.add(performance);
                }
            }
        }

        return result;
    }

    // Add a new performance
    private String addPerformance(String[] parts) {
        try {
            String id = parts[1];
            int showId = Integer.parseInt(parts[2]);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(parts[3]);

            String time = parts[4];
            int totalSeats = Integer.parseInt(parts[5]);
            double price = Double.parseDouble(parts[6]);

            ShowPerformance performance = new ShowPerformance(id, showId, date, time, totalSeats, price);

            synchronized (performances) {
                System.out.println("PERFORMANCE ADDED "+ id);
                performances.put(id, performance);
            }

            saveData();

            return "SUCCESS";
        } catch (Exception e) {
            System.err.println("Error adding performance: " + e.getMessage());
            return "ERROR";
        }
    }

    
        private String getPerformance(String[] parts) {
            try {
                String id = parts[1];
                System.out.println(id);
                
                synchronized (performances) {
                    if (performances.containsKey(id)) {
                        ShowPerformance performance = performances.get(id);
                        
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String formattedDate = dateFormat.format(performance.getDate());
                        
                        return id + "|" +
                              performance.getShowId() + "|" +
                              formattedDate + "|" +
                              performance.getTime() + "|" +
                              performance.getTotalSeats() + "|" +
                              performance.getAvailableSeats() + "|" +
                              performance.getPrice();
                    } else {
                        return "NOT_FOUND";
                    }
                }
            } catch (Exception e) {
                System.err.println("Error getting performance: " + e.getMessage());
                return "ERROR";
            }
        }
        
        // Get performances for a show
        private String getPerformances(String[] parts) {
            try {
                int showId = Integer.parseInt(parts[1]);
                
                List<String> performanceIds = new ArrayList<>();
                
                synchronized (performances) {
                    for (ShowPerformance performance : performances.values()) {
                        if (performance.getShowId() == showId) {
                            performanceIds.add(performance.getId());
                        }
                    }
                }
                
                if (performanceIds.isEmpty()) {
                    return "NO_RESULTS";
                } else {
                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < performanceIds.size(); i++) {
                        if (i > 0) {
                            result.append("|");
                        }
                        result.append(performanceIds.get(i));
                    }
                    return result.toString();
                }
            } catch (Exception e) {
                System.err.println("Error getting performances: " + e.getMessage());
                return "ERROR";
            }
        }

    // Update available seats for a performance
    private String updatePerformanceSeats(String[] parts) {
        try {
            String id = parts[1];
            int availableSeats = Integer.parseInt(parts[2]);

            synchronized (performances) {
                if (performances.containsKey(id)) {
                    performances.get(id).setAvailableSeats(availableSeats);
                    saveData();
                    return "SUCCESS";
                } else {
                    return "NOT_FOUND";
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating performance seats: " + e.getMessage());
            return "ERROR";
        }
    }
}
