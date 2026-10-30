/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorph.ui;

import java.util.logging.Logger;

/**
 * Admin dashboard frame
 */
public class DashboardFrame extends javax.swing.JFrame {
    private static final Logger logger = Logger.getLogger(DashboardFrame.class.getName());
    private static final int FRAME_WIDTH = 600;

    /**
     * Creates new form DashboardFrame
     */
    public DashboardFrame() {
        initComponents();
        setSize(FRAME_WIDTH, 500);
        setupLayout();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }

    /**
     * Initializes components and registers listeners only. No layout or styling here.
     */
    private void initComponents() {
        motorphLabel = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        employeeButton = new javax.swing.JButton();
        payrollButton = new javax.swing.JButton();
        logoutButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        motorphLabel.setText("MotorPH");
        welcomeLabel.setText("Welcome, Admin!");

        employeeButton.setText("Employees");
        employeeButton.setMnemonic('E');
        employeeButton.addActionListener(this::employeeButtonActionPerformed);

        payrollButton.setText("Payroll");
        payrollButton.setMnemonic('P');
        payrollButton.addActionListener(this::payrollButtonActionPerformed);

        logoutButton.setText("Logout");
        logoutButton.setMnemonic('O');
        logoutButton.addActionListener(this::logoutButtonActionPerformed);
    }

    /**
     * Lays out components, sets bounds, and applies styling. Called after initComponents().
     */
    private void setupLayout() {
        getContentPane().setLayout(null);
        getContentPane().setBackground(new java.awt.Color(245, 245, 248));

        // Header panel
        javax.swing.JPanel headerPanel = new javax.swing.JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBackground(new java.awt.Color(28, 57, 110));
        headerPanel.setBounds(0, 0, FRAME_WIDTH, 70);

        motorphLabel.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.BOLD, 22));
        motorphLabel.setForeground(new java.awt.Color(255, 255, 255));
        motorphLabel.setBounds(16, 12, 200, 30);
        headerPanel.add(motorphLabel);

        welcomeLabel.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.PLAIN, 12));
        welcomeLabel.setForeground(new java.awt.Color(210, 225, 245));
        welcomeLabel.setBounds(16, 40, 200, 16);
        headerPanel.add(welcomeLabel);

        // Logout button lives INSIDE the header panel so it always paints with the header
        logoutButton.setBounds(490, 20, 90, 30);
        applySecondaryButtonStyle(logoutButton);
        headerPanel.add(logoutButton);

        getContentPane().add(headerPanel);

        // Separator
        javax.swing.JLabel separator = new javax.swing.JLabel();
        separator.setOpaque(true);
        separator.setBackground(new java.awt.Color(44, 90, 160));
        separator.setBounds(0, 70, FRAME_WIDTH, 2);
        getContentPane().add(separator);

        // Main action buttons
        employeeButton.setBounds(170, 150, 260, 50);
        payrollButton.setBounds(170, 210, 260, 50);

        applyButtonStyle(employeeButton);
        applyButtonStyle(payrollButton);

        // Make main buttons visually prominent
        employeeButton.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.BOLD, 14));
        payrollButton.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.BOLD, 14));

        getContentPane().add(employeeButton);
        getContentPane().add(payrollButton);
    }

    // NAVIGATION
    private void openFrame(javax.swing.JFrame frame){
        frame.setVisible(true);
        this.dispose();
    }

    // EVENT HANDLERS
    private void employeeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        openFrame(new EmployeeFrame());
    }

    private void payrollButtonActionPerformed(java.awt.event.ActionEvent evt) {
        showProcessPayrollMenu();
    }

    /**
     * Shows the "Process Payroll" submenu.
     */
    private void showProcessPayrollMenu() {
        Object[] options = {"One Employee", "All Employees", "Exit"};
        int choice = javax.swing.JOptionPane.showOptionDialog(
                this,
                "Process Payroll",
                "Process Payroll",
                javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            openFrame(new SalaryComputationFrame(true));
        } else if (choice == 1) {
            openFrame(new SalaryComputationFrame(false));
        } else {
            // Dialog was cancelled/closed: force the frame to repaint so the
            // header and logout button redraw cleanly.
            getContentPane().revalidate();
            getContentPane().repaint();
        }
    }

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Are you sure you want to Logout?", "Logout", javax.swing.JOptionPane.YES_NO_OPTION);

        if(confirm == javax.swing.JOptionPane.YES_OPTION){
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }

    /**
     * Applies standard styling to a JButton according to the UI guidelines.
     *
     * @param button the JButton to style
     */
    private void applyButtonStyle(javax.swing.JButton button) {
        UIStyleHelper.applyButtonStyle(button);
    }

    /**
     * Applies secondary button styling (gray).
     *
     * @param button the JButton to style
     */
    private void applySecondaryButtonStyle(javax.swing.JButton button) {
        UIStyleHelper.applySecondaryButtonStyle(button);
    }

    /**
     * Applies standard styling to a text input field (JTextField or subclasses).
     *
     * @param field the JTextField to style
     */
    private void applyFieldStyle(javax.swing.JTextField field) {
        UIStyleHelper.applyFieldStyle(field);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new DashboardFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton employeeButton;
    private javax.swing.JButton logoutButton;
    private javax.swing.JLabel motorphLabel;
    private javax.swing.JButton payrollButton;
    private javax.swing.JLabel welcomeLabel;
    // End of variables declaration//GEN-END:variables
}