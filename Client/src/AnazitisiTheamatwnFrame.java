
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


public class AnazitisiTheamatwnFrame extends javax.swing.JFrame {

     private ReservationInterface service;
    private String currentUsername;
    private DefaultTableModel tableModel;
    private List<Show> currentShows;
    
    public AnazitisiTheamatwnFrame(ReservationInterface service, String username) {
        this.service = service;
        this.currentUsername = username;
        initComponents();
        setupTable();
        loadAllShows();
        this.setVisible(true);
    }
    
    private void setupTable() {
        String[] columnNames = {"ID", "Τίτλος", "Είδος"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        jTable1.setModel(tableModel);
        
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(50);  
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(250);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(150); 
        
        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { 
                    int selectedRow = jTable1.getSelectedRow();
                    if (selectedRow >= 0) {
                        openShowDetails(selectedRow);
                    }
                }
            }
        });
    }
    
    private void loadAllShows() {
        try {
            tableModel.setRowCount(0);
            
            Map<String, String> emptyCriteria = new HashMap<>();
            currentShows = service.searchShows(emptyCriteria);
            
            if (currentShows != null && !currentShows.isEmpty()) {
                for (Show show : currentShows) {
                   if (show.isActive()) { 
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
    
    private void searchShows() {
        String searchTitle = jTextField1.getText().trim();
        String searchType = jTextField2.getText().trim();
        
        try {
            tableModel.setRowCount(0);
            
            Map<String, String> searchCriteria = new HashMap<>();
            if (!searchTitle.isEmpty()) {
                searchCriteria.put("title", searchTitle);
            }
            if (!searchType.isEmpty()) {
                searchCriteria.put("type", searchType);
            }
           
            currentShows = service.searchShows(searchCriteria);
            
            if (currentShows != null && !currentShows.isEmpty()) {
                for (Show show : currentShows) {
                    if (show.isActive()) {
                        Object[] rowData = {
                            show.getId(),
                            show.getTitle(),
                            show.getType()
                        };
                        tableModel.addRow(rowData);
                    }
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Βρέθηκαν " + tableModel.getRowCount() + " θεάματα", 
                    "Αποτελέσματα Αναζήτησης", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Δεν βρέθηκαν θεάματα με τα κριτήρια που δώσατε", 
                    "Αποτελέσματα Αναζήτησης", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, 
                "Σφάλμα κατά την αναζήτηση: " + e.getMessage(), 
                "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
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
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel2.setText("Τίτλος:");

        jLabel3.setText("Είδος:");

        jButton1.setText("Αναζήτηση");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton2.setText("Καθαρισμός");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Λεπτομέρειες");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel1.setText("ANAΖΗΤΗΣΗ ΘΕΑΜΑΤΩΝ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(88, 88, 88)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addGap(28, 28, 28)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(137, 137, 137)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(53, 53, 53)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(54, 54, 54)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(84, 84, 84))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(63, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        searchShows();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jTextField1.setText("");
        jTextField2.setText("");
        
        // Load all shows
        loadAllShows();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
         // Details button clicked
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow >= 0) {
            openShowDetails(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Παρακαλώ επιλέξτε ένα θέαμα από τον πίνακα.", 
                "Επιλογή Θεάματος", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables

}
