
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ManageTheama extends javax.swing.JFrame {

    private ReservationInterface service;
    
    private List<ShowPerformance> performances; // Store performances for this show
    private DefaultTableModel tableModel;
    private String currentUsername; // To track who's adding the show
    private List<Show> currentShows;
    
    public ManageTheama(ReservationInterface service, String currentUsername) {
        this.service = service;
        initComponents();
        this.currentUsername = currentUsername;
        this.setVisible(true);
        setupTable();
        loadAllShows(); // Load all shows initially
    }
    
    private void setupTable() {
        // Setup the table model with proper column names
        String[] columnNames = {"ID", "Τίτλος", "Είδος"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        jTable1.setModel(tableModel);
        
        // Set column widths
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(250); // Title
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(150); // Type
        
        // Add double-click listener to table
        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double click
                    int selectedRow = jTable1.getSelectedRow();
                    if (selectedRow >= 0) {
                        cancelShow(selectedRow);
                    }
                }
            }
        });
    }
    
    private void openShowDetails(int selectedRow) {
        try {
            int showId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // Find the show object
            Show selectedShow = null;
            if (currentShows != null) {
                for (Show show : currentShows) {
                    if (show.getId() == showId) {
                        selectedShow = show;
                        break;
                    }
                }
            }
            
            if (selectedShow != null) {
                // Open InfoTheamatos window
                new InfoTheamatos(service, selectedShow, currentUsername);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Δεν βρέθηκε το επιλεγμένο θέαμα", 
                    "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Σφάλμα κατά το άνοιγμα των λεπτομερειών: " + e.getMessage(), 
                "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void cancelShow(int selectedRow) {
        try {
            int showId = (int) tableModel.getValueAt(selectedRow, 0);
            String showTitle = (String) tableModel.getValueAt(selectedRow, 1);
            String status = (String) tableModel.getValueAt(selectedRow, 3);
            
            // Check if show is already cancelled
            if (status.equals("Ακυρωμένο")) {
                JOptionPane.showMessageDialog(this, 
                    "Το θέαμα '" + showTitle + "' είναι ήδη ακυρωμένο.", 
                    "Θέαμα Ήδη Ακυρωμένο", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Confirm cancellation
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Είστε βέβαιοι ότι θέλετε να ακυρώσετε το θέαμα:\n'" + showTitle + "'?\n\n" +
                "Αυτή η ενέργεια δεν μπορεί να αναιρεθεί.", 
                "Επιβεβαίωση Ακύρωσης", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Call service to deactivate show
                boolean success = service.deactivateShow(showId, currentUsername);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Το θέαμα '" + showTitle + "' ακυρώθηκε επιτυχώς.", 
                        "Επιτυχής Ακύρωση", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Update the table row to show cancelled status
                    tableModel.setValueAt("Ακυρωμένο", selectedRow, 3);
                    
                    // Optionally refresh the entire table to get updated data
                    // loadAllShows();
                    
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Αποτυχία ακύρωσης του θεάματος '" + showTitle + "'.\n" +
                        "Παρακαλώ δοκιμάστε ξανά.", 
                        "Σφάλμα Ακύρωσης", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, 
                "Σφάλμα επικοινωνίας με τον εξυπηρετητή: " + e.getMessage(), 
                "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Σφάλμα κατά την ακύρωση: " + e.getMessage(), 
                "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void loadAllShows() {
        try {
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Search for all shows (empty criteria returns all)
            Map<String, String> emptyCriteria = new HashMap<>();
            currentShows = service.searchShows(emptyCriteria);
            
            if (currentShows != null && !currentShows.isEmpty()) {
                for (Show show : currentShows) {
                    if (show.isActive()) { // Only show active shows
                        Object[] rowData = {
                            show.getId(),
                            show.getTitle(),
                            show.getType()
                        };
                        tableModel.addRow(rowData);
                    }
                }
                System.out.println("Loaded " + currentShows.size() + " shows");
            } else {
                System.out.println("No shows found");
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, 
                "Σφάλμα κατά τη φόρτωση των θεαμάτων: " + e.getMessage(), 
                "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 0, 24)); // NOI18N
        jLabel1.setText("Διαχείριση Θεαμάτων");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Θεάμα", "Είδος", "Κατάσταση"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton2.setText("Απενεργοποίηση");

        jButton3.setText("Πίσω");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(126, 126, 126))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(189, 189, 189)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 89, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

}
