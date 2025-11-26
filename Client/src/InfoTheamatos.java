
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


public class InfoTheamatos extends javax.swing.JFrame {

    private ReservationInterface service; // Changed from ReservationInterface
    private Show currentShow;
    private String currentUsername;
    private DefaultTableModel tableModel;
    
    public InfoTheamatos(ReservationInterface service, Show show, String username) {
        this.service = service;
        this.currentShow = show;
        this.currentUsername = username;
        initComponents();
        setupTable();
        displayShowInfo();
        loadPerformances();
        this.setVisible(true);
    }
    
     private void setupTable() {
        // Setup the table model with proper column names
        String[] columnNames = {"ID", "Ημερομηνία", "Ώρα", "Διαθέσιμες Θέσεις", "Τιμή (€)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        jTable1.setModel(tableModel);
        
        // Set column widths
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(80);  // Time
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(120); // Seats
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(80);  // Price
        
        // Add double-click listener to table
        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double click
                    int selectedRow = jTable1.getSelectedRow();
                    if (selectedRow >= 0) {
                        openPerformanceBooking(selectedRow);
                    }
                }
            }
        });
    }
    
    private void displayShowInfo() {
        if (currentShow != null) {
            jLabel5.setText(currentShow.getTitle());
            jLabel6.setText(currentShow.getType());
            jLabel7.setText("<html><body style='width: 200px'>" + currentShow.getDescription() + "</body></html>");
        }
    }
    
    private void loadPerformances() {
        try {
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Get fresh show details with performances
            Show showWithPerformances = service.getShowDetails(currentShow.getId());
            
            if (showWithPerformances != null && showWithPerformances.getPerformances() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                
                for (ShowPerformance performance : showWithPerformances.getPerformances()) {
                    Object[] rowData = {
                        performance.getId(),
                        dateFormat.format(performance.getDate()),
                        performance.getTime(),
                        performance.getAvailableSeats(),
                        String.format("%.2f", performance.getPrice())
                    };
                    tableModel.addRow(rowData);
                }
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, 
                "Σφάλμα κατά τη φόρτωση των παραστάσεων: " + e.getMessage(), 
                "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void openPerformanceBooking(int selectedRow) {
        try {
            String performanceId = (String) tableModel.getValueAt(selectedRow, 0);
            String date = (String) tableModel.getValueAt(selectedRow, 1);
            String time = (String) tableModel.getValueAt(selectedRow, 2);
            int availableSeats = (int) tableModel.getValueAt(selectedRow, 3);
            String priceStr = (String) tableModel.getValueAt(selectedRow, 4);
            double price = Double.parseDouble(priceStr);
            
            // Check if seats are available
            if (availableSeats <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Δεν υπάρχουν διαθέσιμες θέσεις για αυτή την παράσταση.", 
                    "Εξαντλημένες Θέσεις", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Open booking dialog/window
            // You can implement a booking window here
            int response = JOptionPane.showConfirmDialog(this, 
                "Θέλετε να κάνετε κράτηση για την παράσταση:\n" +
                "Ημερομηνία: " + date + "\n" +
                "Ώρα: " + time + "\n" +
                "Τιμή: €" + price + "\n" +
                "Διαθέσιμες θέσεις: " + availableSeats, 
                "Κράτηση Εισιτηρίων", JOptionPane.YES_NO_OPTION);
            
            if (response == JOptionPane.YES_OPTION) {
                // Ask for number of tickets
                String ticketsStr = JOptionPane.showInputDialog(this, 
                    "Πόσα εισιτήρια θέλετε να κρατήσετε;", "1");
                
                if (ticketsStr != null && !ticketsStr.trim().isEmpty()) {
                    try {
                        int numTickets = Integer.parseInt(ticketsStr.trim());
                        
                        if (numTickets > 0 && numTickets <= availableSeats) {
                            // Make reservation
                            boolean success = service.reserveTickets(Integer.parseInt(performanceId), numTickets, currentUsername);
                            
                            if (success) {
                                JOptionPane.showMessageDialog(this, 
                                    "Κράτηση επιτυχής! Συνολικό κόστος: €" + (price * numTickets), 
                                    "Επιτυχής Κράτηση", JOptionPane.INFORMATION_MESSAGE);
                                
                                // Refresh the table to show updated available seats
                                loadPerformances();
                            } else {
                                JOptionPane.showMessageDialog(this, 
                                    "Η κράτηση απέτυχε. Παρακαλώ δοκιμάστε ξανά.", 
                                    "Σφάλμα Κράτησης", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, 
                                "Μη έγκυρος αριθμός εισιτηρίων.", 
                                "Σφάλμα", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, 
                            "Παρακαλώ εισάγετε έγκυρο αριθμό.", 
                            "Σφάλμα", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Σφάλμα: " + e.getMessage(), 
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
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 0, 24)); // NOI18N
        jLabel1.setText("Λεπτομέρειες Θεάματος");

        jLabel2.setText("Τίτλος:");

        jLabel3.setText("Είδος:");

        jLabel4.setText("Περιγραφή:");

        jLabel5.setText("jLabel5");

        jLabel6.setText("jLabel6");

        jLabel7.setText("jLabel7");

        jLabel8.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel8.setText("Διαθέσιμες Παραστάσεις:");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton1.setText("Κράτηση");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Πίσω");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jLabel8))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(34, 34, 34)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(81, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(70, 70, 70))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel7))
                .addGap(37, 37, 37)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(109, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       int selectedRow = jTable1.getSelectedRow();
        if (selectedRow >= 0) {
            openPerformanceBooking(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Παρακαλώ επιλέξτε μία παράσταση από τον πίνακα.", 
                "Επιλογή Παράστασης", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

}
