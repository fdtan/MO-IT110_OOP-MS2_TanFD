/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorph.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import motorph.SalaryComputationModule;


public class EmployeeFrame extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(EmployeeFrame.class.getName());

    private static final long serialVersionUID = 1L;

    private static final int MIN_COLUMNS_FOR_TABLE   = 12;
    private static final int FRAME_WIDTH = 900;

   // === COLUMN INDEXES ===
    // Sourced from SalaryComputationModule to keep a single source of truth and
    // avoid duplicating column-position assumptions across frames.
    private static final int COL_EMPLOYEE_ID = SalaryComputationModule.COL_EMPLOYEE_ID;
    private static final int COL_LAST_NAME = SalaryComputationModule.COL_LAST_NAME;
    private static final int COL_FIRST_NAME = SalaryComputationModule.COL_FIRST_NAME;
    private static final int COL_BIRTHDAY = SalaryComputationModule.COL_BIRTHDAY;
    private static final int COL_ADDRESS = SalaryComputationModule.COL_ADDRESS;
    private static final int COL_PHONE_NUMBER = SalaryComputationModule.COL_PHONE_NUMBER;
    private static final int COL_SSS_NUMBER = SalaryComputationModule.COL_SSS_NUMBER;
    private static final int COL_PHILHEALTH_NUMBER = SalaryComputationModule.COL_PHILHEALTH_NUMBER;
    private static final int COL_TIN = SalaryComputationModule.COL_TIN;
    private static final int COL_PAGIBIG_NUMBER = SalaryComputationModule.COL_PAGIBIG_NUMBER;
    private static final int COL_STATUS = SalaryComputationModule.COL_STATUS;
    private static final int COL_POSITION = SalaryComputationModule.COL_POSITION;
    private static final int COL_SUPERVISOR = SalaryComputationModule.COL_SUPERVISOR;
    private static final int COL_BASIC_SALARY = SalaryComputationModule.COL_BASIC_SALARY;
    private static final int COL_RICE_SUBSIDY = SalaryComputationModule.COL_RICE_SUBSIDY;
    private static final int COL_PHONE_ALLOWANCE = SalaryComputationModule.COL_PHONE_ALLOWANCE;
    private static final int COL_CLOTHING_ALLOWANCE = SalaryComputationModule.COL_CLOTHING_ALLOWANCE;
    private static final int COL_GROSS_SEMI_MONTHLY_RATE = SalaryComputationModule.COL_GROSS_SEMI_MONTHLY_RATE;
    private static final int COL_HOURLY_RATE = SalaryComputationModule.COL_HOURLY_RATE;

    private transient List<String[]> employeeData;
    private transient java.util.Map<String, String[]> employeeIndex;

    // Employee field names (CSV columns)
    private static final String[] EMPLOYEE_FIELD_NAMES = {
        "Employee ID", "Last Name", "First Name", "Birthday", "Address", "Phone Number",
        "SSS Number", "PhilHealth Number", "TIN", "Pag-IBIG Number", "Status", "Position", "Supervisor",
        "Basic Salary", "Rice Subsidy", "Phone Allowance", "Clothing Allowance", "Gross Semi-Monthly Rate", "Hourly Rate"
    };

    // Numeric field indexes for validation
    private static final int[] NUMERIC_FIELD_INDEXES = {COL_BASIC_SALARY, COL_RICE_SUBSIDY, COL_PHONE_ALLOWANCE, COL_CLOTHING_ALLOWANCE, COL_GROSS_SEMI_MONTHLY_RATE, COL_HOURLY_RATE};

    // Text-only field indexes for validation (no digits allowed)
    private static final int[] TEXT_ONLY_FIELD_INDEXES = {COL_LAST_NAME, COL_FIRST_NAME, COL_POSITION, COL_SUPERVISOR};

    //Generate EmployeeID
    private String generateNextEmployeeId() {
    int highestId = 0;

    if (employeeData != null) {
        for (String[] row : employeeData) {
            if (row == null || row.length == 0) continue;

            try {
                int currentId = Integer.parseInt(row[0].trim());
                if (currentId > highestId) {
                    highestId = currentId;
                }
            } catch (NumberFormatException e) {
                // Skip non-numeric rows
            }
        }
    }

    return String.valueOf(highestId + 1);
    }

    @SuppressWarnings("this-escape")
    public EmployeeFrame() {
        initComponents();
        setResizable(false);
        setSize(FRAME_WIDTH, 600);
        setupLayout();
        loadEmployees();
        setLocationRelativeTo(null);
    }

    /**
     * Initializes component instances and registers listeners only. No bounds or layout.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        employeeLabel = new javax.swing.JLabel();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        showAllButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        employeeTable = new javax.swing.JTable();
        viewButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        employeeLabel.setText("Employee Management");

        searchLabel.setText("Search:");

        searchField.addActionListener(this::searchFieldActionPerformed);

        searchButton.setText("Search");
        searchButton.setMnemonic('S');
        searchButton.addActionListener(this::searchButtonActionPerformed);

        showAllButton.setText("Show All");
        showAllButton.setMnemonic('A');
        showAllButton.addActionListener(this::showAllButtonActionPerformed);

        employeeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Employee ID", "Last Name", "First Name", "Position", "Status"
            }
        ) {
            Class<?>[] types = new Class<?>[] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class<?> getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
                public boolean isCellEditable(int row, int column) {
                return false;}
        });
        jScrollPane1.setViewportView(employeeTable);

        viewButton.setText("View Details");
        viewButton.setMnemonic('V');
        viewButton.addActionListener(this::viewButtonActionPerformed);

        addButton.setText("Add Employee");
        addButton.setMnemonic('D');
        addButton.addActionListener(this::addButtonActionPerformed);

        editButton.setText("Edit");
        editButton.setMnemonic('E');
        editButton.addActionListener(this::editButtonActionPerformed);

        deleteButton.setText("Delete");
        deleteButton.setMnemonic('L');
        deleteButton.addActionListener(this::deleteButtonActionPerformed);

        backButton.setText("Back");
        backButton.setMnemonic('B');
        backButton.addActionListener(this::backButtonActionPerformed);
    }

    /**
     * Performs layout and styling for all components. Called after initComponents().
     */
    private void setupLayout() {
        getContentPane().setLayout(null);
        getContentPane().setBackground(new java.awt.Color(245, 245, 248));

        // Header panel
        javax.swing.JPanel headerPanel = new javax.swing.JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBackground(new java.awt.Color(28, 57, 110));
        headerPanel.setBounds(0, 0, FRAME_WIDTH, 70);

        // Move title into the header
        employeeLabel.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.BOLD, 22));
        employeeLabel.setForeground(new java.awt.Color(255, 255, 255));
        employeeLabel.setBounds(16, 12, 300, 30);
        headerPanel.add(employeeLabel);

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

        // Search controls
        searchLabel.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.BOLD, 13));
        searchLabel.setForeground(new java.awt.Color(30, 30, 50));
        searchLabel.setBounds(20, 80, 50, 18);
        getContentPane().add(searchLabel);

        searchField.setBounds(90, 80, 200, 25);
        getContentPane().add(searchField);

        searchButton.setBounds(300, 80, 100, 25);
        getContentPane().add(searchButton);

        showAllButton.setBounds(410, 80, 110, 25);
        getContentPane().add(showAllButton);

        // Table and scroll
        jScrollPane1.setBounds(20, 110, 860, 380);
        getContentPane().add(jScrollPane1);

        // Bottom action buttons
        viewButton.setBounds(20, 510, 120, 30);
        addButton.setBounds(150, 510, 140, 30);
        editButton.setBounds(300, 510, 100, 30);
        deleteButton.setBounds(410, 510, 100, 30);
        backButton.setBounds(780, 510, 100, 30);

        getContentPane().add(viewButton);
        getContentPane().add(addButton);
        getContentPane().add(editButton);
        getContentPane().add(deleteButton);
        getContentPane().add(backButton);

        applyFieldStyle(searchField);
        applyButtonStyle(searchButton);
        applySecondaryButtonStyle(showAllButton);

        // Bottom buttons
        applyButtonStyle(viewButton);
        applyButtonStyle(addButton);
        applyButtonStyle(editButton);
        applyDangerButtonStyle(deleteButton);
        applySecondaryButtonStyle(backButton);

        // Table styling
        employeeTable.setRowHeight(28);
        employeeTable.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.PLAIN, 12));
        employeeTable.setGridColor(new java.awt.Color(220, 220, 230));
        employeeTable.setSelectionBackground(new java.awt.Color(44, 90, 160));
        employeeTable.setSelectionForeground(new java.awt.Color(255, 255, 255));
        employeeTable.setShowHorizontalLines(true);
        employeeTable.setShowVerticalLines(false);
        employeeTable.getTableHeader().setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.BOLD, 12));
        employeeTable.getTableHeader().setBackground(new java.awt.Color(28, 57, 110));
        employeeTable.getTableHeader().setForeground(new java.awt.Color(255, 255, 255));
        employeeTable.getTableHeader().setReorderingAllowed(false);
        employeeTable.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 30));
    }

    // DATA FUNCTIONS
    /**
     * Loads employees from CSV into memory and repopulates the table.
     * Skips blank rows and ensures the in-memory list is fully replaced so searches
     * and table refreshes reflect the latest CSV contents. Rebuilds the employee
     * index for fast O(1) lookups.
     */
    private void loadEmployees() {
        setTitle("Employee Management — Loading...");
        List<String[]> rawData = SalaryComputationModule.readCSV(SalaryComputationModule.EMPLOYEE_CSV_PATH);

        if (rawData == null) {
            JOptionPane.showMessageDialog(this,
                "Error loading employee data. Please check the file path.",
                "Load Error",
                JOptionPane.ERROR_MESSAGE);
            employeeData = new ArrayList<>();
            employeeIndex = new java.util.HashMap<>();
            populateEmployeeTable(employeeData);
            setTitle("Employee Management");
            return;
        }

        List<String[]> cleaned = new ArrayList<>();
        for (String[] row : rawData) {
            if (row == null) continue;
            if (row.length < MIN_COLUMNS_FOR_TABLE) continue;
            if (row[0] == null || row[0].trim().isEmpty()) continue; // skip empty/blank rows
            cleaned.add(row);
        }

        // Replace in-memory data entirely with the cleaned list
        employeeData = cleaned;
        employeeIndex = SalaryComputationModule.buildEmployeeIndex(employeeData);
        populateEmployeeTable(employeeData);
        setTitle("Employee Management");
    }

    private void populateEmployeeTable(List<String[]> data) {
        DefaultTableModel model = (DefaultTableModel) employeeTable.getModel();
        model.setRowCount(0);

        for (String[] row : data) {
            if (row != null && row.length >= MIN_COLUMNS_FOR_TABLE) {
                model.addRow(new Object[]{
                    row[COL_EMPLOYEE_ID],
                    row[COL_LAST_NAME],
                    row[COL_FIRST_NAME],
                    row[COL_POSITION],
                    row[COL_STATUS]
                });
            }
        }
    }

    // SEARCH FUNCTIONS
    private void searchEmployee() {
        String searchQuery = searchField.getText().trim().toLowerCase();

        // If search is empty, reset to full list instead of showing warning
        if (searchQuery.isEmpty()) {
            populateEmployeeTable(employeeData);
            return;
        }

        List<String[]> results = findMatchingEmployees(searchQuery);

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No employees found for: " + searchQuery,
                "No Results",
                JOptionPane.INFORMATION_MESSAGE);
            populateEmployeeTable(employeeData);
        } else {
            populateEmployeeTable(results);
        }
    }

    private List<String[]> findMatchingEmployees(String query) {
        List<String[]> results = new ArrayList<>();

        for (String[] row : employeeData) {
            if (row != null && row.length >= MIN_COLUMNS_FOR_TABLE) {
                boolean matchesId        = row[0].toLowerCase().contains(query);
                boolean matchesLastName  = row[1].toLowerCase().contains(query);
                boolean matchesFirstName = row[2].toLowerCase().contains(query);

                if (matchesId || matchesLastName || matchesFirstName) {
                    results.add(row);
                }
            }
        }

        return results;
    }

    // VIEW EMPLOYEE FUNCTIONS
    private void viewEmployee() {
        int selectedRow = employeeTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee to view.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String employeeId       = (String) employeeTable.getValueAt(selectedRow, 0);
        String[] employeeRecord = SalaryComputationModule.findEmployeeById(employeeIndex, employeeId);

        if (employeeRecord == null || employeeRecord.length < SalaryComputationModule.MIN_EMPLOYEE_COLUMNS) {
            JOptionPane.showMessageDialog(this,
                "Error retrieving employee details. Record may be incomplete.",
                "Data Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        displayEmployeeDetails(employeeRecord);
    }


    private String[][] buildDetailData(String[] record) {
        return new String[][] {
            { "Employee ID",     record[COL_EMPLOYEE_ID]  },
            { "Last Name",       record[COL_LAST_NAME]  },
            { "First Name",      record[COL_FIRST_NAME]  },
            { "Birthday",        record[COL_BIRTHDAY]  },
            { "Address",         record[COL_ADDRESS]  },
            { "Phone",           record[COL_PHONE_NUMBER]  },
            { "SSS No.",         record[COL_SSS_NUMBER]  },
            { "PhilHealth No.",  record[COL_PHILHEALTH_NUMBER]  },
            { "TIN",             record[COL_TIN]  },
            { "Pag-ibig No.",    record[COL_PAGIBIG_NUMBER]  },
            { "Status",          record[COL_STATUS] },
            { "Position",        record[COL_POSITION] },
            { "Supervisor",      record[COL_SUPERVISOR] },
            { "Basic Salary",    record[COL_BASIC_SALARY] },
            { "Rice Subsidy",    record[COL_RICE_SUBSIDY] },
            { "Phone Allowance", record[COL_PHONE_ALLOWANCE] },
            { "Clothing Allow.", record[COL_CLOTHING_ALLOWANCE] },
            { "Gross Semi-mo.",  record[COL_GROSS_SEMI_MONTHLY_RATE] },
            { "Hourly Rate",     record[COL_HOURLY_RATE] }
        };
    }

    private void displayEmployeeDetails(String[] record) {
        String[][] detailData  = buildDetailData(record);
        String[]   columnNames = { "Field", "Value" };

        javax.swing.JTable detailTable = new javax.swing.JTable(detailData, columnNames);
        detailTable.setEnabled(false);
        detailTable.setRowHeight(24);
        detailTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        detailTable.getColumnModel().getColumn(1).setPreferredWidth(280);

        javax.swing.JScrollPane detailScrollPane = new javax.swing.JScrollPane(detailTable);
        detailScrollPane.setPreferredSize(new java.awt.Dimension(440, 420));

        JOptionPane.showMessageDialog(
            this,
            detailScrollPane,
            "Employee Details - " + record[2] + " " + record[1],
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // NAVIGATION FUNCTIONS
    private void openDashboard() {
        new DashboardFrame().setVisible(true);
        this.dispose();
    }

    // EVENT HANDLERS
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) { searchEmployee(); }
    private void viewButtonActionPerformed(java.awt.event.ActionEvent evt) { viewEmployee(); }
    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) { openDashboard(); }

    /**
     * Action handler for the Add Employee button.
     *
     * @param evt the action event
     */
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {
        showAddEmployeeDialog();
    }

    /**
     * Shows a modal dialog that collects all employee fields, validates input,
     * prevents duplicate IDs, appends the new record to the CSV file, and
     * refreshes the table on success.
     */
    private void showAddEmployeeDialog() {
        Object[] parts = buildEmployeeFormDialog("Add Employee", null);
        JDialog dialog = (JDialog) parts[0];
        JTextField[] inputs = (JTextField[]) parts[1];
        JButton saveBtn = (JButton) parts[2];
        JButton cancelBtn = (JButton) parts[3];

        inputs[0].setText(generateNextEmployeeId());
        inputs[0].setEditable(false);

        saveBtn.addActionListener((ActionEvent e) -> {
            String[] values = new String[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                values[i] = inputs[i].getText().trim();
            }

            String validationError = validateNewEmployeeInputs(values);
            if (validationError != null) {
                JOptionPane.showMessageDialog(dialog, validationError, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isDuplicateEmployeeId(values[0])) {
                JOptionPane.showMessageDialog(dialog, "An employee with ID " + values[0] + " already exists.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to add this employee?", "Confirm Add", JOptionPane.YES_NO_OPTION);
            if (confirm != javax.swing.JOptionPane.YES_OPTION) {
                // keep dialog open for user to make changes
                return;
            }

            if (!appendEmployeeToCsv(values)) {
                JOptionPane.showMessageDialog(dialog, "Error saving employee record. Please try again.", "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (employeeData == null) {
                employeeData = new ArrayList<>();
            }
            employeeData.add(values);
            employeeIndex = SalaryComputationModule.buildEmployeeIndex(employeeData);

            JOptionPane.showMessageDialog(this, "Employee added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            populateEmployeeTable(employeeData);
            dialog.dispose();
        });

        cancelBtn.addActionListener((ActionEvent e) -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * Builds and returns a modal employee form dialog with pre-filled values.
     * Used by both the Add and Edit flows to avoid code duplication.
     *
     * @param title   the dialog window title
     * @param prefill array of pre-filled values; null or empty strings show blank fields
     * @return array where [0] = JDialog, [1] = JTextField[] inputs, [2] = JButton saveBtn, [3] = JButton cancelBtn
     *         Caller adds ActionListeners to the Save and Cancel buttons via getComponent()
     */
    private Object[] buildEmployeeFormDialog(String title, String[] prefill) {
        JTextField[] inputs = new JTextField[EMPLOYEE_FIELD_NAMES.length];
        JPanel formPanel = new JPanel(new GridLayout(EMPLOYEE_FIELD_NAMES.length, 2, 6, 6));
        formPanel.setPreferredSize(new Dimension(400, EMPLOYEE_FIELD_NAMES.length * 32));
        for (int i = 0; i < EMPLOYEE_FIELD_NAMES.length; i++) {
            JLabel fieldLabel = new JLabel(EMPLOYEE_FIELD_NAMES[i] + ":");
            fieldLabel.setFont(new java.awt.Font("Helvetica Neue", java.awt.Font.BOLD, 12));
            fieldLabel.setForeground(new java.awt.Color(30, 30, 50));
            fieldLabel.setHorizontalAlignment(JLabel.RIGHT);
            formPanel.add(fieldLabel);
            String val = (prefill != null && i < prefill.length && prefill[i] != null) ? prefill[i] : "";
            inputs[i] = new JTextField(val);
            applyFieldStyle(inputs[i]);
            formPanel.add(inputs[i]);
        }

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setPreferredSize(new Dimension(420, Math.min(EMPLOYEE_FIELD_NAMES.length * 32, 520)));

        JPanel buttonsPanel = new JPanel();
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        saveBtn.setMnemonic('S');
        cancelBtn.setMnemonic('C');
        applyButtonStyle(saveBtn);
        applyButtonStyle(cancelBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(cancelBtn);

        JDialog dialog = new JDialog(this, title, true);
        dialog.getContentPane().setLayout(new java.awt.BorderLayout(8, 8));
        dialog.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
        dialog.getContentPane().add(buttonsPanel, java.awt.BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Add Enter key handler to the last text field to trigger Save
        if (inputs.length > 0) {
            final JTextField lastField = inputs[inputs.length - 1];
            lastField.addActionListener(e -> saveBtn.doClick());
        }

        // Add Escape key handler to the dialog's root pane to trigger Cancel
        javax.swing.KeyStroke escapeKeyStroke = javax.swing.KeyStroke.getKeyStroke("ESCAPE");
        dialog.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(escapeKeyStroke, "closeDialog");
        dialog.getRootPane().getActionMap().put("closeDialog", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cancelBtn.doClick();
            }
        });

        return new Object[] { dialog, inputs, saveBtn, cancelBtn };
    }

    /**
     * Validates new employee input fields.
     *
     * @param values array of input strings
     * @return null if valid, otherwise an error message
     */
    private String validateNewEmployeeInputs(String[] values) {
        if (values == null || values.length < SalaryComputationModule.MIN_EMPLOYEE_COLUMNS) {
            return "Invalid input data.";
        }

        for (int i = 0; i < SalaryComputationModule.MIN_EMPLOYEE_COLUMNS; i++) {
            if (values[i] == null || values[i].isEmpty()) {
                return EMPLOYEE_FIELD_NAMES[i] + " is required.";
            }
        }

        // Text-only fields must not contain digits
        for (int idx : TEXT_ONLY_FIELD_INDEXES) {
            if (values[idx].matches(".*\\d.*")) {
                return EMPLOYEE_FIELD_NAMES[idx] + " must not contain numbers.";
            }
        }

        // Employee ID must be a positive whole number
        try {
            long id = Long.parseLong(values[0]);
            if (id <= 0) {
                return "Employee ID must be a positive whole number.";
            }
        } catch (NumberFormatException ex) {
            return "Employee ID must be a positive whole number.";
        }

        // Numeric fields must be valid numbers and greater than zero
        for (int idx : NUMERIC_FIELD_INDEXES) {
            if (!SalaryComputationModule.validateNumeric(values[idx])) {
                return EMPLOYEE_FIELD_NAMES[idx] + " must be a valid number.";
            }
            try {
                double val = Double.parseDouble(values[idx]);
                if (val <= 0) {
                    return EMPLOYEE_FIELD_NAMES[idx] + " must be greater than zero.";
                }
            } catch (NumberFormatException ex) {
                return EMPLOYEE_FIELD_NAMES[idx] + " must be a valid number.";
            }
        }

        return null;
    }

    /**
     * Checks whether an employee ID already exists in the current data list.
     * Uses the pre-built index for O(1) lookup.
     *
     * Important: Do not re-read the CSV here if employeeData is not loaded; that
     * would be inefficient. If data is not loaded in memory, return false so that
     * callers can decide whether to load data first.
     *
     * @param employeeId the employee ID to check
     * @return true if duplicate exists, false otherwise
     */
    private boolean isDuplicateEmployeeId(String employeeId) {
        if (employeeIndex == null) {
            // If we don't have index loaded in memory, avoid re-reading the CSV here.
            return false;
        }
        return SalaryComputationModule.findEmployeeById(employeeIndex, employeeId) != null;
    }

    /**
     * Appends a new employee record to the CSV file via SalaryComputationModule.
     * Delegates to the centralized file I/O method.
     *
     * @param values the employee field values (length must be MIN_EMPLOYEE_COLUMNS)
     * @return true on success, false on IO error
     */
    private boolean appendEmployeeToCsv(String[] values) {
        return SalaryComputationModule.appendRowToCsv(SalaryComputationModule.EMPLOYEE_CSV_PATH, values);
    }

    /**
     * Rewrites the entire CSV file with the provided data rows via SalaryComputationModule.
     * Delegates to the centralized file I/O method.
     *
     * @param data list of string arrays representing CSV rows
     * @return true on success, false on IO error
     */
    private boolean rewriteCsv(List<String[]> data) {
        return SalaryComputationModule.rewriteCsvFile(SalaryComputationModule.EMPLOYEE_CSV_PATH, data);
    }

    /**
     * Called when Enter is pressed in the search field to trigger the search.
     *
     * @param evt the action event
     */
    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {
        searchEmployee();
    }

    private void showAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
        searchField.setText("");
        populateEmployeeTable(employeeData);
    }

    /**
     * Action handler for the Edit button. Opens an edit dialog, validates input,
     * confirms the change, rewrites the CSV and refreshes the table on success.
     *
     * @param evt the action event
     */
    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String employeeId = (String) employeeTable.getValueAt(selectedRow, 0);
        String[] record = SalaryComputationModule.findEmployeeById(employeeIndex, employeeId);
        if (record == null || record.length < SalaryComputationModule.MIN_EMPLOYEE_COLUMNS) {
            JOptionPane.showMessageDialog(this,
                "Error retrieving employee details. Record may be incomplete.",
                "Data Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] parts = buildEmployeeFormDialog("Edit Employee", record);
        JDialog dialog = (JDialog) parts[0];
        JTextField[] inputs = (JTextField[]) parts[1];
        JButton saveBtn = (JButton) parts[2];
        JButton cancelBtn = (JButton) parts[3];

        final String originalId = employeeId;

        saveBtn.addActionListener((ActionEvent e) -> {
            String[] values = new String[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                values[i] = inputs[i].getText().trim();
            }

            String validationError = validateNewEmployeeInputs(values);
            if (validationError != null) {
                JOptionPane.showMessageDialog(dialog, validationError, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!values[0].equals(originalId) && isDuplicateEmployeeId(values[0])) {
                JOptionPane.showMessageDialog(dialog, "An employee with ID " + values[0] + " already exists.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to save changes?", "Confirm Save", JOptionPane.YES_NO_OPTION);
            if (confirm != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }

            boolean updated = false;
            for (int i = 0; i < employeeData.size(); i++) {
                String[] row = employeeData.get(i);
                if (row != null && row[0].trim().equals(originalId.trim())) {
                    String[] newRow = new String[values.length];
                    System.arraycopy(values, 0, newRow, 0, values.length);
                    employeeData.set(i, newRow);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                JOptionPane.showMessageDialog(dialog, "Failed to locate original employee record.", "Update Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!rewriteCsv(employeeData)) {
                JOptionPane.showMessageDialog(dialog, "Error saving changes. Please try again.", "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            employeeIndex = SalaryComputationModule.buildEmployeeIndex(employeeData);

            JOptionPane.showMessageDialog(this, "Employee updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            populateEmployeeTable(employeeData);
            dialog.dispose();
        });

        cancelBtn.addActionListener((ActionEvent e) -> dialog.dispose());
        dialog.setVisible(true);
    }

    /**
     * Action handler for the Delete button. Prompts for confirmation and removes
     * the selected record from memory and the CSV file.
     *
     * @param evt the action event
     */
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String employeeId = (String) employeeTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this employee? This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        boolean removed = false;
        for (int i = 0; i < employeeData.size(); i++) {
            String[] row = employeeData.get(i);
            if (row != null && row[0].trim().equals(employeeId.trim())) {
                employeeData.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            JOptionPane.showMessageDialog(this, "Failed to locate employee to delete.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!rewriteCsv(employeeData)) {
            JOptionPane.showMessageDialog(this, "Error deleting employee. Please try again.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        employeeIndex = SalaryComputationModule.buildEmployeeIndex(employeeData);

        JOptionPane.showMessageDialog(this, "Employee deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        populateEmployeeTable(employeeData);
    }

    private String escapeCsvField(String value) {
        return SalaryComputationModule.escapeCsvField(value);
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
     * Applies danger button styling (red).
     *
     * @param button the JButton to style
     */
    private void applyDangerButtonStyle(javax.swing.JButton button) {
        UIStyleHelper.applyDangerButtonStyle(button);
    }

    /**
     * Applies standard styling to a text input field (JTextField or subclasses).
     *
     * @param field the JTextField to style
     */
    private void applyFieldStyle(javax.swing.JTextField field) {
        UIStyleHelper.applyFieldStyle(field);
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(Level.WARNING, "Nimbus look-and-feel not available. Using default.", ex);
        }

        java.awt.EventQueue.invokeLater(() -> new EmployeeFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel employeeLabel;
    private javax.swing.JTable employeeTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton searchButton;
    private javax.swing.JButton showAllButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables
}
