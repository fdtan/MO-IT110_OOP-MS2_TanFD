package motorph.ui;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import motorph.SalaryComputationModule;

public class SalaryComputationFrame extends JFrame {

    private static final Logger logger = Logger.getLogger(SalaryComputationFrame.class.getName());

    private static final int FRAME_WIDTH = 1200; // widened to fit extra columns

    private transient List<String[]> employeeData;
    private transient List<String[]> attendanceData;
    private transient List<Object[]> computedResults;
    private transient java.util.Map<String, String[]> employeeIndex;
    private transient java.util.Map<String, List<String[]>> attendanceIndex;

    private boolean singleEmployeeMode;
    private javax.swing.JTextField employeeIdField; // only shown in single-employee mode

    // UI components
    private javax.swing.JComboBox<String> monthComboBox;
    private JButton computeButton;
    private JButton summaryButton;
    private JButton saveButton;
    private JButton backButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;

    public SalaryComputationFrame() {
        this.singleEmployeeMode = false;
        initComponents();
        setResizable(false);
        setSize(FRAME_WIDTH, 600);
        setupLayout();
        applyFrameStyling();
        setLocationRelativeTo(null);
        loadEmployeeData();
        loadAttendanceData();
    }

    /**
     * Creates the frame in either single-employee or all-employees mode.
     *
     * @param singleEmployeeMode true to show an employee-number field and compute
     *                           payroll for just that one employee; false for the
     *                           existing all-employees batch behavior
     */
    public SalaryComputationFrame(boolean singleEmployeeMode) {
        this.singleEmployeeMode = singleEmployeeMode;
        initComponents();
        setResizable(false);
        setSize(FRAME_WIDTH, 600);
        setupLayout();
        applyFrameStyling();
        setLocationRelativeTo(null);
        loadEmployeeData();
        loadAttendanceData();
    }

    // === UI SETUP ===
    private void initComponents() {
        setTitle("Salary Computation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        monthComboBox  = new javax.swing.JComboBox<>();
        employeeIdField = new javax.swing.JTextField();

        computeButton = new JButton("Compute Salaries");
        computeButton.setMnemonic('M');
        computeButton.addActionListener(this::computeButtonActionPerformed);

        summaryButton = new JButton("Generate Summary");
        summaryButton.setMnemonic('G');
        summaryButton.addActionListener(this::summaryButtonActionPerformed);

        saveButton = new JButton("Save Results");
        saveButton.setMnemonic('R');
        saveButton.addActionListener(this::saveButtonActionPerformed);

        backButton = new JButton("Back");
        backButton.setMnemonic('B');
        backButton.addActionListener(this::backButtonActionPerformed);

        // Added "Hourly Rate" and "Hours Worked" columns after Name
        String[] cols = new String[] {
            "Employee ID", "Name",
            "Hourly Rate", "Hours Worked",
            "Gross Pay", "SSS", "PhilHealth", "Pag-IBIG",
            "Withholding Tax", "Total Deductions", "Net Pay"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setAutoCreateRowSorter(true);
        resultsTable.setRowHeight(24);
    }

    /**
     * Lays out components and sets bounds. Called after initComponents().
     */
    private void setupLayout() {
        getContentPane().setLayout(null);

        // Header panel
        javax.swing.JPanel headerPanel = new javax.swing.JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBackground(new java.awt.Color(28, 57, 110));
        headerPanel.setBounds(0, 0, FRAME_WIDTH, 70);
        javax.swing.JLabel headerTitle = new javax.swing.JLabel("Salary Computation");
        headerTitle.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.BOLD, 22));
        headerTitle.setForeground(new java.awt.Color(255, 255, 255));
        headerTitle.setBounds(16, 12, 400, 30);
        headerPanel.add(headerTitle);
        javax.swing.JLabel headerSubtitle = new javax.swing.JLabel("MotorPH Payroll System");
        headerSubtitle.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.PLAIN, 12));
        headerSubtitle.setForeground(new java.awt.Color(210, 225, 245));
        headerSubtitle.setBounds(16, 40, 300, 16);
        headerPanel.add(headerSubtitle);
        getContentPane().add(headerPanel);

        // Separator
        javax.swing.JLabel separator = new javax.swing.JLabel();
        separator.setOpaque(true);
        separator.setBackground(new java.awt.Color(44, 90, 160));
        separator.setBounds(0, 70, FRAME_WIDTH, 2);
        getContentPane().add(separator);

        // Month / Employee controls
        java.awt.Color fg = new java.awt.Color(30, 30, 50);
        java.awt.Font labelFont = new java.awt.Font("Helvetica Neue", java.awt.Font.PLAIN, 13);

        JLabel monthLabel = new JLabel("Select Cutoff Period:");
        monthLabel.setBounds(20, 80, 140, 25);
        monthLabel.setFont(labelFont);
        monthLabel.setForeground(fg);
        getContentPane().add(monthLabel);

        if (singleEmployeeMode) {
            JLabel empIdLabel = new JLabel("Employee No:");
            empIdLabel.setBounds(20, 80, 100, 25);
            empIdLabel.setFont(labelFont);
            empIdLabel.setForeground(fg);
            getContentPane().remove(monthLabel);
            getContentPane().add(empIdLabel);

            employeeIdField.setBounds(120, 80, 130, 25);
            employeeIdField.setToolTipText("Enter employee number");
            employeeIdField.setVisible(true);
            getContentPane().add(employeeIdField);

            monthComboBox.setBounds(260, 80, 150, 25);
            getContentPane().add(monthComboBox);
        } else {
            employeeIdField.setVisible(false);
            monthComboBox.setBounds(170, 80, 150, 25);
            getContentPane().add(monthComboBox);
        }

        // Toolbar buttons — shifted right to match wider frame
        computeButton.setBounds(440, 80, 160, 28);
        summaryButton.setBounds(610, 80, 150, 28);
        saveButton.setBounds(770, 80, 130, 28);
        backButton.setBounds(910, 80, 100, 28);

        getContentPane().add(computeButton);
        getContentPane().add(summaryButton);
        getContentPane().add(saveButton);
        getContentPane().add(backButton);

        // Results table — uses full width of wider frame
        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.setBounds(20, 115, FRAME_WIDTH - 40, 440);
        getContentPane().add(scroll);
    }

    /**
     * Applies all visual styling to the frame components after initComponents().
     */
    private void applyFrameStyling() {
        getContentPane().setBackground(new java.awt.Color(245, 245, 248));

        if (monthComboBox != null) {
            monthComboBox.setBackground(new java.awt.Color(255, 255, 255));
            monthComboBox.setForeground(new java.awt.Color(30, 30, 50));
            monthComboBox.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.PLAIN, 13));
        }

        applyFieldStyle(employeeIdField);
        applyButtonStyle(computeButton);
        applyButtonStyle(summaryButton);
        applyButtonStyle(saveButton);
        applySecondaryButtonStyle(backButton);

        resultsTable.setRowHeight(28);
        resultsTable.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.PLAIN, 12));
        resultsTable.setGridColor(new java.awt.Color(220, 220, 230));
        resultsTable.setSelectionBackground(new java.awt.Color(44, 90, 160));
        resultsTable.setSelectionForeground(new java.awt.Color(255, 255, 255));
        resultsTable.setShowHorizontalLines(true);
        resultsTable.setShowVerticalLines(false);
        resultsTable.getTableHeader().setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.BOLD, 12));
        resultsTable.getTableHeader().setBackground(new java.awt.Color(28, 57, 110));
        resultsTable.getTableHeader().setForeground(new java.awt.Color(255, 255, 255));
        resultsTable.getTableHeader().setReorderingAllowed(false);
        resultsTable.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 30));
    }

    // === DATA LOADING ===

    /**
     * Loads employee CSV data into memory and performs basic cleaning.
     */
    private void loadEmployeeData() {
        employeeData = SalaryComputationModule.readCSV(SalaryComputationModule.EMPLOYEE_CSV_PATH);
        if (employeeData == null) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load employee data. Please check that the file exists at: " + SalaryComputationModule.EMPLOYEE_CSV_PATH,
                    "File Load Error",
                    JOptionPane.ERROR_MESSAGE);
            employeeData = new ArrayList<>();
            employeeIndex = new java.util.HashMap<>();
            return;
        }
        List<String[]> cleaned = new ArrayList<>();
        for (String[] row : employeeData) {
            if (row == null) continue;
            if (row.length < 1) continue;
            if (row[0] == null || row[0].trim().isEmpty()) continue;
            cleaned.add(row);
        }
        employeeData = cleaned;
        employeeIndex = SalaryComputationModule.buildEmployeeIndex(employeeData);
    }

    /**
     * Loads attendance CSV from ATTENDANCE_CSV_PATH into the attendanceData field.
     */
    private void loadAttendanceData() {
        attendanceData = SalaryComputationModule.loadAttendanceData(SalaryComputationModule.ATTENDANCE_CSV_PATH);
        if (attendanceData == null) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load attendance data. Please check that the file exists at: " + SalaryComputationModule.ATTENDANCE_CSV_PATH,
                    "File Load Error",
                    JOptionPane.ERROR_MESSAGE);
            attendanceData = new ArrayList<>();
        }
        attendanceIndex = SalaryComputationModule.buildAttendanceIndex(attendanceData);
        String[] cutoffPeriods = SalaryComputationModule.getAvailableCutoffPeriods(attendanceData);
        monthComboBox.removeAllItems();
        for (String cutoff : cutoffPeriods) {
            monthComboBox.addItem(cutoff);
        }
    }

    /**
     * Validates that a month has been selected in the month combo box.
     *
     * @return the selected cutoff label, or null to indicate validation failure
     */
    private String validateMonthSelection() {
        Object selected = monthComboBox.getSelectedItem();
        if (selected == null || selected.toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a month before computing salaries.",
                    "Missing Selection",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return selected.toString();
    }

    // === COMPUTATION ===

    private void computeSalaries() {
        String selectedMonth = validateMonthSelection();
        if (selectedMonth == null) return;

        if (singleEmployeeMode) {
            computeSingleEmployeeSalary(selectedMonth);
        } else {
            computeAllEmployeesSalary(selectedMonth);
        }
    }

    /**
     * Handles the all-employees batch computation path.
     */
    private void computeAllEmployeesSalary(String selectedMonth) {
        List<String> noAttendance = SalaryComputationModule.findEmployeesWithNoAttendance(
                employeeData, attendanceIndex, selectedMonth);
        if (!noAttendance.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "The following employees have no attendance records for " + selectedMonth
                            + " and will be computed with 0 hours:\n" + String.join("\n", noAttendance),
                    "No Attendance Records Found",
                    JOptionPane.WARNING_MESSAGE);
        }

        computedResults = SalaryComputationModule.computeAllEmployees(employeeData, attendanceIndex, selectedMonth);
        populateResultsTable(computedResults, selectedMonth);
        JOptionPane.showMessageDialog(this, "Salary computation complete.", "Computation Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Handles the single-employee computation path.
     */
    private void computeSingleEmployeeSalary(String selectedMonth) {
        String employeeId = employeeIdField.getText().trim();
        if (employeeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an employee number.", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object[] singleResult = SalaryComputationModule.computeSingleEmployee(
                employeeData, attendanceIndex, employeeId, selectedMonth);
        if (singleResult == null) {
            JOptionPane.showMessageDialog(this, "Employee number does not exist.", "Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        computedResults = new ArrayList<>();
        computedResults.add(singleResult);
        populateResultsTable(computedResults, selectedMonth);
        JOptionPane.showMessageDialog(this, "Salary computation complete.", "Computation Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    // === RESULTS DISPLAY ===

    /**
     * Clears and repopulates the results table with computed payroll data.
     * Uses the pre-built employee index for O(1) lookups instead of linear scans.
     * Looks up each employee's hourly rate from employeeData (col 18) and their
     * hours worked from attendanceData for the selected cutoff period.
     * No salary calculation logic is changed — only display is extended.
     *
     * Result array order from SalaryComputationModule:
     *   [0]=employeeId, [1]=fullName, [2]=grossPay, [3]=totalDeductions,
     *   [4]=netPay, [5]=sss, [6]=philHealth, [7]=pagIbig, [8]=withholdingTax
     *
     * Table column order:
     *   Employee ID | Name | Hourly Rate | Hours Worked |
     *   Gross Pay | SSS | PhilHealth | Pag-IBIG | Withholding Tax | Total Deductions | Net Pay
     *
     * @param results      the list of computed result rows from SalaryComputationModule
     * @param cutoffLabel  the selected cutoff period label, used to look up hours worked
     */
    private void populateResultsTable(List<Object[]> results, String cutoffLabel) {
        tableModel.setRowCount(0);
        if (results == null) return;

        for (Object[] r : results) {
            if (r == null || r.length < 9) continue;

            String id          = (String) r[0];
            String name        = (String) r[1];
            double gross       = (double) r[2];
            double totalDeduct = (double) r[3];
            double net         = (double) r[4];
            double sss         = (double) r[5];
            double phil        = (double) r[6];
            double pag         = (double) r[7];
            double wtax        = (double) r[8];

            // Look up hourly rate from employeeIndex (O(1) lookup)
            double hourlyRate = 0;
            String[] empRecord = SalaryComputationModule.findEmployeeById(employeeIndex, id);
            if (empRecord != null && empRecord.length > SalaryComputationModule.COL_HOURLY_RATE) {
                try {
                    hourlyRate = Double.parseDouble(empRecord[SalaryComputationModule.COL_HOURLY_RATE].trim());
                } catch (NumberFormatException e) {
                    hourlyRate = 0;
                }
            }

            // Look up hours worked from attendanceIndex for this cutoff period
            double hoursWorked = SalaryComputationModule.computeCutoffHoursForEmployee(
                    attendanceIndex, id, cutoffLabel);

            tableModel.addRow(new Object[] {
                id,
                name,
                String.format("₱%,.2f", hourlyRate),
                String.format("%.2f hrs", hoursWorked),
                String.format("₱%,.2f", gross),
                String.format("₱%,.2f", sss),
                String.format("₱%,.2f", phil),
                String.format("₱%,.2f", pag),
                String.format("₱%,.2f", wtax),
                String.format("₱%,.2f", totalDeduct),
                String.format("₱%,.2f", net)
            });
        }
    }

    // === FILE OUTPUT ===

    /**
     * Writes the computed payroll results to a CSV file, including the new
     * Hourly Rate and Hours Worked columns.
     *
     * @param selectedMonth the cutoff label to include in the success message
     * @return true if saved successfully, false otherwise
     */
    private boolean saveResultsToCsv(String selectedMonth) {
        if (computedResults == null) return false;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SalaryComputationModule.RESULTS_CSV_PATH, false))) {
            // Header now includes the two new columns
            bw.write("Employee ID,Name,Hourly Rate,Hours Worked,Gross Pay,SSS,PhilHealth,Pag-IBIG,Withholding Tax,Total Deductions,Net Pay");
            bw.newLine();

            for (Object[] r : computedResults) {
                if (r == null || r.length < 9) continue;

                String id          = (String) r[0];
                String name        = (String) r[1];
                double gross       = (double) r[2];
                double totalDeduct = (double) r[3];
                double net         = (double) r[4];
                double sss         = (double) r[5];
                double phil        = (double) r[6];
                double pag         = (double) r[7];
                double wtax        = (double) r[8];

                // Look up hourly rate from employeeIndex (O(1) lookup)
                double hourlyRate = 0;
                String[] empRecord = SalaryComputationModule.findEmployeeById(employeeIndex, id);
                if (empRecord != null && empRecord.length > SalaryComputationModule.COL_HOURLY_RATE) {
                    try {
                        hourlyRate = Double.parseDouble(empRecord[SalaryComputationModule.COL_HOURLY_RATE].trim());
                    } catch (NumberFormatException e) {
                        hourlyRate = 0;
                    }
                }

                // Look up hours worked for the selected cutoff period
                double hoursWorked = SalaryComputationModule.computeCutoffHoursForEmployee(
                        attendanceIndex, id, selectedMonth);

                String line = String.format("%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
                        SalaryComputationModule.escapeCsvField(id), 
                        SalaryComputationModule.escapeCsvField(name),
                        hourlyRate, hoursWorked,
                        gross, sss, phil, pag, wtax, totalDeduct, net);
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save payroll results", e);
            JOptionPane.showMessageDialog(this, "Failed to save results: " + e.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (selectedMonth == null) selectedMonth = "";
        JOptionPane.showMessageDialog(this, "Results saved for " + selectedMonth, "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    // === NAVIGATION ===

    /**
     * Opens the main dashboard frame and disposes this frame.
     */
    private void openDashboard() {
        try {
            new DashboardFrame().setVisible(true);
            this.dispose();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to open dashboard", e);
        }
    }

    // === EVENT HANDLERS ===

    private void computeButtonActionPerformed(ActionEvent evt) { computeSalaries(); }
    private void summaryButtonActionPerformed(ActionEvent evt) { showPayrollSummary(); }
    private void saveButtonActionPerformed(ActionEvent evt) {
        if (computedResults == null || computedResults.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No computed results to save. Please compute salaries first.",
                    "No Data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String selectedMonth = (String) monthComboBox.getSelectedItem();
        saveResultsToCsv(selectedMonth);
    }
    private void backButtonActionPerformed(ActionEvent evt) { openDashboard(); }

    /**
     * Displays aggregate payroll totals for ALL employees in the selected cutoff period,
     * per requirement MPHCR05. Independent of which computation mode the frame is in,
     * always reflects the full employee list summary.
     */
    private void showPayrollSummary() {
        String selectedMonth = validateMonthSelection();
        if (selectedMonth == null) return;

        if (employeeData == null || employeeData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employee data loaded. Please check that the CSV file exists and try again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double[] summary = SalaryComputationModule.computeFullPayrollSummary(employeeData, attendanceIndex, selectedMonth);
        int    totalEmployees = (int) summary[0];
        double totalGross     = summary[1];
        double totalDeductions = summary[2];
        double totalNet       = summary[3];
        double avgNet         = summary[4];

        String msg = String.format(
                "Cutoff: %s\n\nTotal Employees: %d\nTotal Gross Pay: ₱%,.2f\nTotal Deductions: ₱%,.2f\nTotal Net Pay: ₱%,.2f\nAverage Net Pay: ₱%,.2f",
                selectedMonth, totalEmployees, totalGross, totalDeductions, totalNet, avgNet);
        JOptionPane.showMessageDialog(this, msg, "Payroll Summary (All Employees)", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Applies standard styling to a JButton.
     */
    private void applyButtonStyle(javax.swing.JButton button) {
        UIStyleHelper.applyButtonStyle(button);
    }

    /**
     * Applies secondary button styling (gray).
     */
    private void applySecondaryButtonStyle(javax.swing.JButton button) {
        UIStyleHelper.applySecondaryButtonStyle(button);
    }

    /**
     * Applies standard styling to a JTextField.
     */
    private void applyFieldStyle(javax.swing.JTextField field) {
        UIStyleHelper.applyFieldStyle(field);
    }

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Nimbus LAF not available", ex);
        }
        java.awt.EventQueue.invokeLater(() -> new SalaryComputationFrame().setVisible(true));
    }
}