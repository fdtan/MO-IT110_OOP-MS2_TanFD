package motorph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SalaryComputationModule {

    // === FILE PATHS (centralized) ===
    public static final String EMPLOYEE_CSV_PATH   = "data/employees.csv";
    public static final String ATTENDANCE_CSV_PATH = "data/attendance.csv";
    public static final String RESULTS_CSV_PATH    = "data/payroll_results.csv";

    // === COLUMN INDEXES (employees.csv) ===
    // Single source of truth for employee column positions. All frames reference
    // these constants instead of declaring their own, to avoid duplication.
    public static final int MIN_EMPLOYEE_COLUMNS        = 19;
    public static final int COL_EMPLOYEE_ID             = 0;
    public static final int COL_LAST_NAME               = 1;
    public static final int COL_FIRST_NAME              = 2;
    public static final int COL_BIRTHDAY                = 3;
    public static final int COL_ADDRESS                 = 4;
    public static final int COL_PHONE_NUMBER            = 5;
    public static final int COL_SSS_NUMBER              = 6;
    public static final int COL_PHILHEALTH_NUMBER       = 7;
    public static final int COL_TIN                     = 8;
    public static final int COL_PAGIBIG_NUMBER          = 9;
    public static final int COL_STATUS                  = 10;
    public static final int COL_POSITION                = 11;
    public static final int COL_SUPERVISOR              = 12;
    public static final int COL_BASIC_SALARY            = 13;
    public static final int COL_RICE_SUBSIDY            = 14;
    public static final int COL_PHONE_ALLOWANCE         = 15;
    public static final int COL_CLOTHING_ALLOWANCE      = 16;
    public static final int COL_GROSS_SEMI_MONTHLY_RATE = 17;
    public static final int COL_HOURLY_RATE             = 18;

    // === ATTENDANCE CSV COLUMN INDEXES ===
    private static final int MIN_ATTENDANCE_COLUMNS = 6;
    private static final int COL_ATT_EMPLOYEE_ID     = 0;
    private static final int COL_ATT_LAST_NAME       = 1;
    private static final int COL_ATT_FIRST_NAME      = 2;
    private static final int COL_ATT_DATE            = 3;
    private static final int COL_ATT_LOG_IN          = 4;
    private static final int COL_ATT_LOG_OUT         = 5;

    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    // === COMPUTATION CONSTANTS ===
    private static final int    STANDARD_WORK_DAYS    = 22;   // standard working days/month
    private static final double PHILHEALTH_RATE        = 0.03; // 3% monthly premium
    private static final double PHILHEALTH_MIN         = 150;  // employee share minimum
    private static final double PHILHEALTH_MAX         = 900;  // employee share maximum
    private static final double PAGIBIG_RATE_LOW       = 0.01; // 1% for salary ≤ 1,500
    private static final double PAGIBIG_RATE_HIGH      = 0.02; // 2% for salary > 1,500
    private static final double PAGIBIG_LOW_THRESHOLD  = 1500; // salary threshold for 1% rate
    private static final double PAGIBIG_MAX            = 100;  // maximum Pag-IBIG contribution

    // === RESULT ARRAY INDEXES (for computeDeductions return value) ===
    public static final int IDX_SSS              = 0;
    public static final int IDX_PHILHEALTH       = 1;
    public static final int IDX_PAGIBIG          = 2;
    public static final int IDX_WITHHOLDING_TAX  = 3;
    public static final int IDX_TOTAL_DEDUCTIONS = 4;

    // === RESULT ARRAY INDEXES (for computePayrollResult return value) ===
    public static final int IDX_GROSS_PAY    = 0;
    public static final int IDX_TOTAL_ALLOW  = 1;
    public static final int IDX_NET_PAY      = 2;
    public static final int IDX_TOTAL_DEDUCT = 3;

    // === CSV OPERATIONS ===
    public static List<String[]> readCSV(String filePath) {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] row = parseCSVLine(line);
                data.add(row);
            }
        } catch (IOException e) {
            return null;
        }
        return data;
    }

    public static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

    // === VALIDATION ===
    public static boolean validateNumeric(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        try {
            Double.parseDouble(input.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // === TIME COMPUTATION ===
    public static double computeHoursWorked(String timeIn, String timeOut) {
        try {
            String[] inParts = timeIn.split(":");
            String[] outParts = timeOut.split(":");
            int inHour = Integer.parseInt(inParts[0]);
            int inMin = Integer.parseInt(inParts[1]);
            int outHour = Integer.parseInt(outParts[0]);
            int outMin = Integer.parseInt(outParts[1]);

            //Cap login at 8:00 if within grace period
            if (inHour == 8 && inMin <= 10) {
                inMin = 0; // keep inHour as-is (already 8)
            }

            //Cap logout at 17:00
            if (outHour > 17 || (outHour == 17 && outMin > 0)) {
                outHour = 17;
                outMin = 0;
            }

            double hours = (outHour - inHour) + (outMin - inMin) / 60.0;
            return Math.max(0, hours);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            return 0;
        }
    }

    // === SALARY COMPUTATION ===
    private static final double[] SSS_THRESHOLDS = {
        3250, 3750, 4250, 4750, 5250, 5750, 6250, 6750, 7250, 7750,
        8250, 8750, 9250, 9750, 10250, 10750, 11250, 11750, 12250, 12750,
        13250, 13750, 14250, 14750, 15250, 15750, 16250, 16750, 17250, 17750,
        18250, 18750, 19250, 19750, 20250, 20750, 21250, 21750, 22250, 22750,
        23250, 23750, 24250, 24750
    };

    private static final double[] SSS_AMOUNTS = {
        135, 157.5, 180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5,
        360, 382.5, 405, 427.5, 450, 472.5, 495, 517.5, 540, 562.5,
        585, 607.5, 630, 652.5, 675, 697.5, 720, 742.5, 765, 787.5,
        810, 832.5, 855, 877.5, 900, 922.5, 945, 967.5, 990, 1012.5,
        1035, 1057.5, 1080, 1102.5
    };

    private static double computeSSS(double monthlyGross) {
        for (int i = 0; i < SSS_THRESHOLDS.length; i++) {
            if (monthlyGross < SSS_THRESHOLDS[i]) return SSS_AMOUNTS[i];
        }
        return 1125;
    }

    /**
     * Computes all government-mandated deductions from monthly gross salary.
     * Returns [sss, philHealth, pagIbig, withholdingTax, totalDeductions].
     */
    public static double[] computeDeductions(double monthlyGross) {
        // PhilHealth: salary × 3% ÷ 2 (employee share), min ₱150, max ₱900
        double philHealth = (monthlyGross * PHILHEALTH_RATE) / 2;
        if (philHealth < PHILHEALTH_MIN) philHealth = PHILHEALTH_MIN;
        if (philHealth > PHILHEALTH_MAX) philHealth = PHILHEALTH_MAX;

        // Pag-IBIG
        double pagIbig;
        if (monthlyGross <= PAGIBIG_LOW_THRESHOLD) {
            pagIbig = monthlyGross * PAGIBIG_RATE_LOW;
        } else {
            pagIbig = monthlyGross * PAGIBIG_RATE_HIGH;
        }
        if (pagIbig > PAGIBIG_MAX) pagIbig = PAGIBIG_MAX;

        // SSS: lookup table
        double sss = computeSSS(monthlyGross);

        // Withholding Tax: applied to taxable income after SSS + PhilHealth + Pag-IBIG
        double taxableIncome = monthlyGross - sss - philHealth - pagIbig;
        double withholdingTax;
        if (taxableIncome <= 20832) {
            withholdingTax = 0;
        } else if (taxableIncome <= 33332) {
            withholdingTax = (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome <= 66666) {
            withholdingTax = 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome <= 166666) {
            withholdingTax = 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome <= 666666) {
            withholdingTax = 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            withholdingTax = 200833.33 + (taxableIncome - 666667) * 0.35;
        }

        double totalDeductions = sss + philHealth + pagIbig + withholdingTax;
        return new double[]{sss, philHealth, pagIbig, withholdingTax, totalDeductions};
    }

    // Individual deduction methods
    public static double computeSSSDeduction(double monthlyGross) {
        return computeDeductions(monthlyGross)[IDX_SSS];
    }

    public static double computePhilHealthDeduction(double monthlyGross) {
        return computeDeductions(monthlyGross)[IDX_PHILHEALTH];
    }

    public static double computePagIBIGDeduction(double monthlyGross) {
        return computeDeductions(monthlyGross)[IDX_PAGIBIG];
    }

    public static double computeWithholdingTax(double monthlyGross) {
        return computeDeductions(monthlyGross)[IDX_WITHHOLDING_TAX];
    }

    /**
     * Computes gross pay from the employee's hourly rate and hours worked.
     */
    public static double computeGrossPay(String[] record, double hoursWorked) {
        if (record == null || record.length <= COL_HOURLY_RATE) return 0;
        try {
            double hourlyRate = Double.parseDouble(record[COL_HOURLY_RATE].trim());
            return hourlyRate * hoursWorked;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Computes gross pay directly from basic monthly salary and days worked.
     * Alternative to computeGrossPay() when daily rate data is available
     * instead of hourly rate. Gross Pay = (Basic Salary ÷ STANDARD_WORK_DAYS) × Days Worked.
     * Uses STANDARD_WORK_DAYS as the standard working days per month.
     *
     * @param record the employee CSV row (must have at least COL_BASIC_SALARY+1 columns)
     * @param daysWorked number of days worked in the pay period
     * @return gross pay as a double, or 0 if basic salary is invalid
     */
    public static double computeGrossPayFromDays(String[] record, double daysWorked) {
        if (record == null || record.length <= COL_BASIC_SALARY) return 0;
        try {
            double basicSalary = Double.parseDouble(record[COL_BASIC_SALARY].trim());
            double dailyRate   = basicSalary / (double) STANDARD_WORK_DAYS;
            return dailyRate * daysWorked;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Computes net pay by subtracting total deductions from gross pay.
     */
    public static double computeNetPay(double grossPay, double totalDeductions) {
        return Math.max(0, grossPay - totalDeductions);
    }

    /**
     * Master payroll computation for a single employee.
     * Returns [grossPay, totalAllowances, netPay, totalDeductions]
     */
    public static double[] computePayrollResult(String[] record, double hoursWorked) {
        if (record == null || record.length < MIN_EMPLOYEE_COLUMNS) {
            return new double[]{0, 0, 0, 0};
        }
        double grossPay     = computeGrossPay(record, hoursWorked);
        double[] deductions = computeDeductions(grossPay);
        double totalDeduct  = deductions[IDX_TOTAL_DEDUCTIONS];
        double netPay       = computeNetPay(grossPay, totalDeduct);

        double totalAllow = 0;
        try {
            double rice     = Double.parseDouble(record[COL_RICE_SUBSIDY].trim());
            double phone    = Double.parseDouble(record[COL_PHONE_ALLOWANCE].trim());
            double clothing = Double.parseDouble(record[COL_CLOTHING_ALLOWANCE].trim());
            totalAllow = rice + phone + clothing;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            totalAllow = 0;
        }

        return new double[]{grossPay, totalAllow, netPay, totalDeduct};
    }

    /**
     * Loads and lightly cleans attendance CSV data from the given file path.
     * <p>
     * Reuses the existing readCSV()/parseCSVLine() pair — attendance.csv uses the
     * same simple comma-separated format as employees.csv. Rows that are null or
     * have fewer than MIN_ATTENDANCE_COLUMNS columns are skipped. The header row
     * ("Employee #,Last Name,...") is naturally excluded because its Date column
     * ("Date") fails numeric parsing wherever dates are later processed.
     * </p>
     *
     * @param filePath path to the attendance CSV file
     * @return a cleaned list of attendance rows, or null if the file could not be read
     */
    public static List<String[]> loadAttendanceData(String filePath) {
        List<String[]> raw = readCSV(filePath);
        if (raw == null) return null;

        List<String[]> cleaned = new ArrayList<>();
        for (String[] row : raw) {
            if (row == null || row.length < MIN_ATTENDANCE_COLUMNS) continue;
            if (row[COL_ATT_EMPLOYEE_ID] == null || row[COL_ATT_EMPLOYEE_ID].trim().isEmpty()) continue;
            cleaned.add(row);
        }
        return cleaned;
    }

    private static final int CUTOFF_1_START_DAY = 1;
    private static final int CUTOFF_1_END_DAY   = 15;
    private static final int CUTOFF_2_START_DAY = 16;

    private static final int[] DAYS_IN_MONTH = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    /**
     * Returns the last calendar day of the given month, accounting for leap years
     * in February.
     *
     * @param month month number (1-12)
     * @param year the year (e.g. 2024)
     * @return the last day of that month (28-31)
     */
    private static int lastDayOfMonth(int month, int year) {
        if (month == 2 && (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))) {
            return 29;
        }
        return DAYS_IN_MONTH[month - 1];
    }

    /**
     * Builds a cutoff-period display label from a date string in MM/dd/yyyy format,
     * e.g. "June 1-15, 2024" or "June 16-30, 2024" depending on which half of the
     * month the date falls in.
     *
     * @param dateStr the date string to parse (e.g. "06/03/2024")
     * @return the formatted cutoff label, or null if unparseable
     */
    private static String buildCutoffLabel(String dateStr) {
        try {
            String[] parts = dateStr.split("/");
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            if (month < 1 || month > 12) return null;

            String monthName = MONTH_NAMES[month - 1];
            if (day >= CUTOFF_1_START_DAY && day <= CUTOFF_1_END_DAY) {
                return monthName + " " + CUTOFF_1_START_DAY + "-" + CUTOFF_1_END_DAY + ", " + year;
            } else {
                int lastDay = lastDayOfMonth(month, year);
                return monthName + " " + CUTOFF_2_START_DAY + "-" + lastDay + ", " + year;
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Scans attendance records and returns the distinct cutoff periods present, in
     * chronological order, formatted as "Month D-D, YYYY" (e.g. "June 1-15, 2024").
     * Used to populate the cutoff-selection combo box dynamically.
     *
     * @param attendanceData attendance rows as returned by loadAttendanceData()
     * @return a chronologically ordered array of unique cutoff-period labels
     */
    public static String[] getAvailableCutoffPeriods(List<String[]> attendanceData) {
        List<String> labels = new ArrayList<>();
        List<Integer> sortKeys = new ArrayList<>();
        if (attendanceData == null) return new String[0];

        for (String[] row : attendanceData) {
            if (row == null || row.length < MIN_ATTENDANCE_COLUMNS) continue;
            String label = buildCutoffLabel(row[COL_ATT_DATE]);
            if (label == null || labels.contains(label)) continue;

            String[] parts = row[COL_ATT_DATE].split("/");
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            int cutoffHalf = (day <= CUTOFF_1_END_DAY) ? 1 : 2;
            int sortKey = (year * 100 + month) * 10 + cutoffHalf;

            labels.add(label);
            sortKeys.add(sortKey);
        }

        for (int i = 1; i < sortKeys.size(); i++) {
            int keyToInsert = sortKeys.get(i);
            String labelToInsert = labels.get(i);
            int j = i - 1;
            while (j >= 0 && sortKeys.get(j) > keyToInsert) {
                sortKeys.set(j + 1, sortKeys.get(j));
                labels.set(j + 1, labels.get(j));
                j--;
            }
            sortKeys.set(j + 1, keyToInsert);
            labels.set(j + 1, labelToInsert);
        }
        return labels.toArray(new String[0]);
    }

    /**
     * Computes an employee's total hours worked within a given cutoff period by
     * summing computeHoursWorked() across every matching attendance row for that
     * employee and cutoff period.
     *
     * @param attendanceData attendance rows as returned by loadAttendanceData()
     * @param employeeId the employee number to match (e.g. "10001")
     * @param cutoffLabel the target cutoff label, as returned by getAvailableCutoffPeriods()
     * @return total hours worked, or 0.0 if no matching attendance rows are found
     */
    public static double computeCutoffHoursForEmployee(List<String[]> attendanceData, String employeeId, String cutoffLabel) {
        double totalHours = 0.0;
        if (attendanceData == null || employeeId == null || cutoffLabel == null) return totalHours;

        for (String[] row : attendanceData) {
            if (row == null || row.length < MIN_ATTENDANCE_COLUMNS) continue;
            if (!employeeId.trim().equals(row[COL_ATT_EMPLOYEE_ID].trim())) continue;
            if (!cutoffLabel.equals(buildCutoffLabel(row[COL_ATT_DATE]))) continue;
            totalHours += computeHoursWorked(row[COL_ATT_LOG_IN], row[COL_ATT_LOG_OUT]);
        }
        return totalHours;
    }

    /**
     * Computes total hours worked by an employee for a specific cutoff period using a pre-built attendance index.
     * This is the O(1) indexed version, preferred when the same attendance index is used multiple times.
     *
     * @param attendanceIndex the Map of employee ID to attendance records as returned by buildAttendanceIndex()
     * @param employeeId the employee ID to look up (trimmed before lookup)
     * @param cutoffLabel the target cutoff period label
     * @return the total hours worked for that employee in that cutoff period
     */
    public static double computeCutoffHoursForEmployee(java.util.Map<String, List<String[]>> attendanceIndex, String employeeId, String cutoffLabel) {
        double totalHours = 0.0;
        if (attendanceIndex == null || employeeId == null || cutoffLabel == null) return totalHours;

        List<String[]> employeeAttendance = attendanceIndex.get(employeeId.trim());
        if (employeeAttendance == null) return totalHours;

        for (String[] row : employeeAttendance) {
            if (row == null || row.length < MIN_ATTENDANCE_COLUMNS) continue;
            if (!cutoffLabel.equals(buildCutoffLabel(row[COL_ATT_DATE]))) continue;
            totalHours += computeHoursWorked(row[COL_ATT_LOG_IN], row[COL_ATT_LOG_OUT]);
        }
        return totalHours;
    }

    /**
     * Identifies employees with no attendance records for the given cutoff period, so the
     * GUI can warn the user once before computing salaries with 0 hours for them.
     *
     * @param employeeData employee rows as returned by readCSV()
     * @param attendanceData attendance rows as returned by loadAttendanceData()
     * @param cutoffLabel the target cutoff period label
     * @return a list of "FullName (EmployeeId)" strings for employees with 0 hours
     */
    public static List<String> findEmployeesWithNoAttendance(List<String[]> employeeData, List<String[]> attendanceData, String cutoffLabel) {
        List<String> result = new ArrayList<>();
        if (employeeData == null) return result;

        for (String[] record : employeeData) {
            if (record == null || record.length < MIN_EMPLOYEE_COLUMNS) continue;
            if (record[COL_EMPLOYEE_ID] == null || record[COL_EMPLOYEE_ID].trim().isEmpty()) continue;

            String employeeId = record[COL_EMPLOYEE_ID].trim();
            double hours = computeCutoffHoursForEmployee(attendanceData, employeeId, cutoffLabel);
            if (hours <= 0) {
                String fullName = (record[COL_FIRST_NAME] == null ? "" : record[COL_FIRST_NAME].trim())
                        + " " + (record[COL_LAST_NAME] == null ? "" : record[COL_LAST_NAME].trim());
                result.add(fullName.trim() + " (" + employeeId + ")");
            }
        }
        return result;
    }

    /**
     * Identifies employees with no attendance records for a given cutoff period using a pre-built attendance index.
     * This is the O(1) indexed version, preferred when the same attendance index is used multiple times.
     *
     * @param employeeData employee rows as returned by readCSV()
     * @param attendanceIndex the Map of employee ID to attendance records as returned by buildAttendanceIndex()
     * @param cutoffLabel the target cutoff period label
     * @return a list of "FullName (EmployeeId)" strings for employees with 0 hours
     */
    public static List<String> findEmployeesWithNoAttendance(List<String[]> employeeData, java.util.Map<String, List<String[]>> attendanceIndex, String cutoffLabel) {
        List<String> result = new ArrayList<>();
        if (employeeData == null) return result;

        for (String[] record : employeeData) {
            if (record == null || record.length < MIN_EMPLOYEE_COLUMNS) continue;
            if (record[COL_EMPLOYEE_ID] == null || record[COL_EMPLOYEE_ID].trim().isEmpty()) continue;

            String employeeId = record[COL_EMPLOYEE_ID].trim();
            double hours = computeCutoffHoursForEmployee(attendanceIndex, employeeId, cutoffLabel);
            if (hours <= 0) {
                String fullName = (record[COL_FIRST_NAME] == null ? "" : record[COL_FIRST_NAME].trim())
                        + " " + (record[COL_LAST_NAME] == null ? "" : record[COL_LAST_NAME].trim());
                result.add(fullName.trim() + " (" + employeeId + ")");
            }
        }
        return result;
    }

    /**
     * Determines whether a cutoff label represents a 2nd-half cutoff period
     * (e.g. "June 16-30, 2024") as opposed to a 1st-half cutoff ("June 1-15, 2024").
     * Works by checking whether the label contains the CUTOFF_2_START_DAY value
     * immediately after the month name.
     *
     * @param cutoffLabel a label as returned by getAvailableCutoffPeriods()
     * @return true if this is a 2nd-half cutoff label, false otherwise (including null)
     */
    public static boolean isSecondCutoff(String cutoffLabel) {
        if (cutoffLabel == null) return false;
        return cutoffLabel.contains(" " + CUTOFF_2_START_DAY + "-");
    }

    /**
     * Given a 2nd-half cutoff label (e.g. "June 16-30, 2024"), builds the
     * corresponding 1st-half cutoff label for the same month ("June 1-15, 2024").
     * Returns null if the input is not a 2nd-half cutoff label.
     *
     * @param secondCutoffLabel a 2nd-half cutoff label
     * @return the matching 1st-half cutoff label, or null if input isn't a 2nd-half label
     */
    public static String buildFirstCutoffLabelForSameMonth(String secondCutoffLabel) {
        if (!isSecondCutoff(secondCutoffLabel)) return null;
        int dashIndex = secondCutoffLabel.indexOf(" " + CUTOFF_2_START_DAY + "-");
        if (dashIndex < 0) return null;
        String monthPart = secondCutoffLabel.substring(0, dashIndex);
        int commaIndex = secondCutoffLabel.indexOf(",", dashIndex);
        if (commaIndex < 0) return null;
        String yearPart = secondCutoffLabel.substring(commaIndex);
        return monthPart + " " + CUTOFF_1_START_DAY + "-" + CUTOFF_1_END_DAY + yearPart;
    }

    /**
     * Computes payroll for ALL employees in a single pass, for the given cutoff
     * period. Behavior depends on which half of the month is selected:
     * <ul>
     *   <li>1st-half cutoff: gross pay only, zero deductions, net pay equals gross
     *       pay. Deductions for the month are deferred to the 2nd cutoff.</li>
     *   <li>2nd-half cutoff: gross pay for this cutoff, but deductions are computed
     *       against the COMBINED gross of this cutoff plus the matching 1st-half
     *       cutoff in the same month, per the spec rule "add the 1st and 2nd cutoff
     *       amounts first before computing the deductions." Net pay is this
     *       cutoff's own gross pay minus the full combined-month deductions.</li>
     * </ul>
     * Each result row: [employeeId, fullName, grossPay, totalDeductions,
     *                   netPay, sss, philHealth, pagIbig, withholdingTax]
     *
     * @param employeeData employee rows as returned by readCSV()
     * @param attendanceData attendance rows as returned by loadAttendanceData()
     * @param cutoffLabel the selected cutoff period label from getAvailableCutoffPeriods()
     */
    public static List<Object[]> computeAllEmployees(List<String[]> employeeData, List<String[]> attendanceData, String cutoffLabel) {
        List<Object[]> results = new ArrayList<>();
        if (employeeData == null) return results;

        boolean secondHalf = isSecondCutoff(cutoffLabel);
        String firstHalfLabel = secondHalf ? buildFirstCutoffLabelForSameMonth(cutoffLabel) : null;

        for (String[] record : employeeData) {
            if (record == null || record.length < MIN_EMPLOYEE_COLUMNS) continue;
            if (record[COL_EMPLOYEE_ID] == null || record[COL_EMPLOYEE_ID].trim().isEmpty()) continue;

            String employeeId = record[COL_EMPLOYEE_ID].trim();
            String fullName = (record[COL_FIRST_NAME] == null ? "" : record[COL_FIRST_NAME].trim())
                    + " " + (record[COL_LAST_NAME] == null ? "" : record[COL_LAST_NAME].trim());

            double thisHoursWorked = computeCutoffHoursForEmployee(attendanceData, employeeId, cutoffLabel);
            double thisGrossPay    = computeGrossPay(record, thisHoursWorked);

            double grossPay;
            double[] deductions;
            double netPay;

            if (!secondHalf) {
                // 1st cutoff: no deductions yet, net pay equals gross pay for this cutoff
                grossPay    = thisGrossPay;
                deductions  = new double[]{0, 0, 0, 0, 0};
                netPay      = grossPay;
            } else {
                // 2nd cutoff: combine with 1st cutoff's gross for the same month before
                // computing deductions, per the spec's combined-cutoff rule
                double firstHalfHours = computeCutoffHoursForEmployee(attendanceData, employeeId, firstHalfLabel);
                double firstHalfGross = computeGrossPay(record, firstHalfHours);
                double combinedMonthlyGross = firstHalfGross + thisGrossPay;

                grossPay    = thisGrossPay;
                deductions  = computeDeductions(combinedMonthlyGross);
                netPay      = computeNetPay(thisGrossPay, deductions[IDX_TOTAL_DEDUCTIONS]);
            }

            results.add(new Object[] {
                employeeId,
                fullName,
                grossPay,
                deductions[IDX_TOTAL_DEDUCTIONS],
                netPay,
                deductions[IDX_SSS],
                deductions[IDX_PHILHEALTH],
                deductions[IDX_PAGIBIG],
                deductions[IDX_WITHHOLDING_TAX]
            });
        }
        return results;
    }

    /**
     * Indexed version of computeAllEmployees() — uses a pre-built attendance index
     * for O(1) per-employee lookups instead of scanning the full attendance list.
     * Identical logic and output to the List-based version.
     */
    public static List<Object[]> computeAllEmployees(List<String[]> employeeData, java.util.Map<String, List<String[]>> attendanceIndex, String cutoffLabel) {
        List<Object[]> results = new ArrayList<>();
        if (employeeData == null) return results;

        boolean secondHalf = isSecondCutoff(cutoffLabel);
        String firstHalfLabel = secondHalf ? buildFirstCutoffLabelForSameMonth(cutoffLabel) : null;

        for (String[] record : employeeData) {
            if (record == null || record.length < MIN_EMPLOYEE_COLUMNS) continue;
            if (record[COL_EMPLOYEE_ID] == null || record[COL_EMPLOYEE_ID].trim().isEmpty()) continue;

            String employeeId = record[COL_EMPLOYEE_ID].trim();
            String fullName = (record[COL_FIRST_NAME] == null ? "" : record[COL_FIRST_NAME].trim())
                    + " " + (record[COL_LAST_NAME] == null ? "" : record[COL_LAST_NAME].trim());

            double thisHoursWorked = computeCutoffHoursForEmployee(attendanceIndex, employeeId, cutoffLabel);
            double thisGrossPay    = computeGrossPay(record, thisHoursWorked);

            double grossPay;
            double[] deductions;
            double netPay;

            if (!secondHalf) {
                grossPay    = thisGrossPay;
                deductions  = new double[]{0, 0, 0, 0, 0};
                netPay      = grossPay;
            } else {
                double firstHalfHours = computeCutoffHoursForEmployee(attendanceIndex, employeeId, firstHalfLabel);
                double firstHalfGross = computeGrossPay(record, firstHalfHours);
                double combinedMonthlyGross = firstHalfGross + thisGrossPay;

                grossPay    = thisGrossPay;
                deductions  = computeDeductions(combinedMonthlyGross);
                netPay      = computeNetPay(thisGrossPay, deductions[IDX_TOTAL_DEDUCTIONS]);
            }

            results.add(new Object[] {
                employeeId,
                fullName,
                grossPay,
                deductions[IDX_TOTAL_DEDUCTIONS],
                netPay,
                deductions[IDX_SSS],
                deductions[IDX_PHILHEALTH],
                deductions[IDX_PAGIBIG],
                deductions[IDX_WITHHOLDING_TAX]
            });
        }
        return results;
    }

    /**
     * Computes payroll for a SINGLE employee for the given cutoff period, using the
     * same combined-cutoff deduction logic as computeAllEmployees (see that method's
     * Javadoc for the 1st-half vs 2nd-half behavior). Returns null if the employee
     * is not found in employeeData.
     *
     * @param employeeData employee rows as returned by readCSV()
     * @param attendanceData attendance rows as returned by loadAttendanceData()
     * @param employeeId the specific employee number to compute payroll for
     * @param cutoffLabel the selected cutoff period label from getAvailableCutoffPeriods()
     * @return a single result row [employeeId, fullName, grossPay, totalDeductions,
     *         netPay, sss, philHealth, pagIbig, withholdingTax], or null if not found
     */
    public static Object[] computeSingleEmployee(List<String[]> employeeData, List<String[]> attendanceData, String employeeId, String cutoffLabel) {
        if (employeeData == null || employeeId == null) return null;

        String[] record = findEmployeeById(employeeData, employeeId);
        if (record == null || record.length < MIN_EMPLOYEE_COLUMNS) return null;

        String fullName = (record[COL_FIRST_NAME] == null ? "" : record[COL_FIRST_NAME].trim())
                + " " + (record[COL_LAST_NAME] == null ? "" : record[COL_LAST_NAME].trim());

        boolean secondHalf = isSecondCutoff(cutoffLabel);
        String firstHalfLabel = secondHalf ? buildFirstCutoffLabelForSameMonth(cutoffLabel) : null;

        double thisHoursWorked = computeCutoffHoursForEmployee(attendanceData, employeeId, cutoffLabel);
        double thisGrossPay    = computeGrossPay(record, thisHoursWorked);

        double grossPay;
        double[] deductions;
        double netPay;

        if (!secondHalf) {
            grossPay   = thisGrossPay;
            deductions = new double[]{0, 0, 0, 0, 0};
            netPay     = grossPay;
        } else {
            double firstHalfHours = computeCutoffHoursForEmployee(attendanceData, employeeId, firstHalfLabel);
            double firstHalfGross = computeGrossPay(record, firstHalfHours);
            double combinedMonthlyGross = firstHalfGross + thisGrossPay;

            grossPay   = thisGrossPay;
            deductions = computeDeductions(combinedMonthlyGross);
            netPay     = computeNetPay(thisGrossPay, deductions[IDX_TOTAL_DEDUCTIONS]);
        }

        return new Object[] {
            employeeId.trim(),
            fullName,
            grossPay,
            deductions[IDX_TOTAL_DEDUCTIONS],
            netPay,
            deductions[IDX_SSS],
            deductions[IDX_PHILHEALTH],
            deductions[IDX_PAGIBIG],
            deductions[IDX_WITHHOLDING_TAX]
        };
    }

    /**
     * Indexed version of computeSingleEmployee() — uses a pre-built attendance index
     * for O(1) lookup instead of scanning the full attendance list.
     * Identical logic and output to the List-based version.
     */
    public static Object[] computeSingleEmployee(List<String[]> employeeData, java.util.Map<String, List<String[]>> attendanceIndex, String employeeId, String cutoffLabel) {
        if (employeeData == null || employeeId == null) return null;

        String[] record = findEmployeeById(employeeData, employeeId);
        if (record == null || record.length < MIN_EMPLOYEE_COLUMNS) return null;

        String fullName = (record[COL_FIRST_NAME] == null ? "" : record[COL_FIRST_NAME].trim())
                + " " + (record[COL_LAST_NAME] == null ? "" : record[COL_LAST_NAME].trim());

        boolean secondHalf = isSecondCutoff(cutoffLabel);
        String firstHalfLabel = secondHalf ? buildFirstCutoffLabelForSameMonth(cutoffLabel) : null;

        double thisHoursWorked = computeCutoffHoursForEmployee(attendanceIndex, employeeId, cutoffLabel);
        double thisGrossPay    = computeGrossPay(record, thisHoursWorked);

        double grossPay;
        double[] deductions;
        double netPay;

        if (!secondHalf) {
            grossPay   = thisGrossPay;
            deductions = new double[]{0, 0, 0, 0, 0};
            netPay     = grossPay;
        } else {
            double firstHalfHours = computeCutoffHoursForEmployee(attendanceIndex, employeeId, firstHalfLabel);
            double firstHalfGross = computeGrossPay(record, firstHalfHours);
            double combinedMonthlyGross = firstHalfGross + thisGrossPay;

            grossPay   = thisGrossPay;
            deductions = computeDeductions(combinedMonthlyGross);
            netPay     = computeNetPay(thisGrossPay, deductions[IDX_TOTAL_DEDUCTIONS]);
        }

        return new Object[] {
            employeeId.trim(),
            fullName,
            grossPay,
            deductions[IDX_TOTAL_DEDUCTIONS],
            netPay,
            deductions[IDX_SSS],
            deductions[IDX_PHILHEALTH],
            deductions[IDX_PAGIBIG],
            deductions[IDX_WITHHOLDING_TAX]
        };
    }

    /**
     * Computes aggregate payroll summary across all computed results.
     * Returns [totalEmployees, totalGrossPay, totalDeductions, totalNetPay, averageNetPay]
     */
    public static double[] computePayrollSummary(List<Object[]> computedResults) {
        if (computedResults == null || computedResults.isEmpty()) {
            return new double[]{0, 0, 0, 0, 0};
        }
        int total = computedResults.size();
        double grossTotal = 0;
        double deductTotal = 0;
        double netTotal = 0;

        for (Object[] row : computedResults) {
            grossTotal += (double) row[2];
            deductTotal += (double) row[3];
            netTotal += (double) row[4];
        }
        return new double[]{total, grossTotal, deductTotal, netTotal, netTotal / total};
    }

    // === EMPLOYEE LOOKUP ===
    public static String[] findEmployeeById(List<String[]> data, String employeeId) {
        if (data == null) return null;
        for (String[] row : data) {
            if (row != null && row.length >= MIN_EMPLOYEE_COLUMNS) {
                if (row[COL_EMPLOYEE_ID].trim().equals(employeeId.trim())) return row;
            }
        }
        return null;
    }

    // === EMPLOYEE INDEX (HashMap for O(1) lookups) ===

    /**
     * Builds a HashMap index from employee data for fast O(1) lookups by employee ID.
     * Trims all employee IDs to ensure consistent matching.
     *
     * @param employeeData the full employee list as returned by readCSV()
     * @return a HashMap<String, String[]> keyed by trimmed employee ID, or empty map if data is null
     */
    public static java.util.Map<String, String[]> buildEmployeeIndex(List<String[]> employeeData) {
        java.util.Map<String, String[]> index = new java.util.HashMap<>();
        if (employeeData == null) return index;
        for (String[] row : employeeData) {
            if (row != null && row.length >= MIN_EMPLOYEE_COLUMNS) {
                String employeeId = row[COL_EMPLOYEE_ID].trim();
                if (!employeeId.isEmpty()) {
                    index.put(employeeId, row);
                }
            }
        }
        return index;
    }

    /**
     * Pre-groups attendance records by employee ID for O(1) lookup.
     * Groups all attendance rows into a Map where each key is an employee ID
     * and the value is a list of that employee's attendance records.
     *
     * @param attendanceData attendance rows as returned by loadAttendanceData()
     * @return a Map of employee ID to list of attendance records
     */
    public static java.util.Map<String, List<String[]>> buildAttendanceIndex(List<String[]> attendanceData) {
        java.util.Map<String, List<String[]>> index = new java.util.HashMap<>();
        if (attendanceData == null) return index;
        for (String[] row : attendanceData) {
            if (row != null && row.length >= MIN_ATTENDANCE_COLUMNS) {
                String employeeId = row[COL_ATT_EMPLOYEE_ID].trim();
                if (!employeeId.isEmpty()) {
                    index.computeIfAbsent(employeeId, k -> new ArrayList<>()).add(row);
                }
            }
        }
        return index;
    }

    /**
     * Looks up an employee by ID using a pre-built HashMap index for O(1) performance.
     * Preferred in loops where the same index is used multiple times.
     *
     * @param index the HashMap index as returned by buildEmployeeIndex()
     * @param employeeId the employee ID to look up (trimmed before lookup)
     * @return the employee record array, or null if not found
     */
    public static String[] findEmployeeById(java.util.Map<String, String[]> index, String employeeId) {
        if (index == null || employeeId == null) return null;
        return index.get(employeeId.trim());
    }

    // === FILE I/O OPERATIONS ===

    /**
     * Escapes CSV field values to handle commas, quotes, and special characters.
     * If the field contains commas, quotes, or newlines, or starts/ends with spaces,
     * wraps it in quotes and escapes internal quotes by doubling them.
     *
     * @param value the field value to escape (null becomes empty string)
     * @return the escaped field value suitable for CSV output
     */
    public static String escapeCsvField(String value) {
        if (value == null) return "";
        String v = value;
        boolean needQuote = v.contains(",") || v.contains("\"") || v.contains("\n") || v.startsWith(" ") || v.endsWith(" ");
        v = v.replace("\"", "\"\"");
        if (needQuote) {
            return "\"" + v + "\"";
        }
        return v;
    }

    /**
     * Appends a single CSV row to the specified file. Creates the file if it does not exist.
     * Handles proper CSV escaping and adds a newline after each row.
     *
     * @param filePath the file path to append to
     * @param values the string array of field values for this row
     * @return true if the operation succeeded, false on I/O error
     */
    public static boolean appendRowToCsv(String filePath, String[] values) {
        if (values == null || values.length == 0) return false;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(escapeCsvField(values[i]));
            if (i < values.length - 1) sb.append(",");
        }

        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(filePath, true))) {
            bw.write(sb.toString());
            bw.newLine();
            bw.flush();
            return true;
        } catch (java.io.IOException e) {
            java.util.logging.Logger.getLogger(SalaryComputationModule.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Failed to append row to CSV: " + filePath, e);
            return false;
        }
    }

    /**
     * Overwrites an entire CSV file with the given data. Each row is properly escaped.
     * Suitable for updating a file after editing or deleting rows.
     *
     * @param filePath the file path to overwrite
     * @param data list of string arrays representing CSV rows
     * @return true if the operation succeeded, false on I/O error
     */
    public static boolean rewriteCsvFile(String filePath, List<String[]> data) {
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(filePath, false))) {
            if (data != null) {
                for (String[] row : data) {
                    if (row == null) continue;
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < row.length; j++) {
                        sb.append(escapeCsvField(row[j]));
                        if (j < row.length - 1) sb.append(",");
                    }
                    bw.write(sb.toString());
                    bw.newLine();
                }
            }
            bw.flush();
            return true;
        } catch (java.io.IOException e) {
            java.util.logging.Logger.getLogger(SalaryComputationModule.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Failed to rewrite CSV: " + filePath, e);
            return false;
        }
    }

    // === PAYROLL SUMMARY ===

    /**
     * Computes a full payroll summary for ALL employees for a given cutoff period,
     * independent of the current application state. This method internally calls
     * computeAllEmployees() and then computePayrollSummary() to produce aggregate
     * statistics, ensuring that the summary always reflects the full employee list
     * regardless of which mode the frame is operating in (single or batch).
     *
     * This is the "correct" summary method per requirement MPHCR05.
     *
     * @param employeeData the full employee list as returned by readCSV()
     * @param attendanceData the full attendance list as returned by loadAttendanceData()
     * @param cutoffLabel the selected cutoff period label
     * @return a double array [totalEmployees, totalGrossPay, totalDeductions, totalNetPay, averageNetPay]
     */
    public static double[] computeFullPayrollSummary(List<String[]> employeeData, List<String[]> attendanceData, String cutoffLabel) {
        if (employeeData == null || attendanceData == null || cutoffLabel == null) {
            return new double[]{0, 0, 0, 0, 0};
        }
        List<Object[]> allResults = computeAllEmployees(employeeData, attendanceData, cutoffLabel);
        return computePayrollSummary(allResults);
    }

    /**
     * Indexed version of computeFullPayrollSummary() — uses a pre-built attendance
     * index for O(1) per-employee lookups instead of scanning the full attendance list.
     */
    public static double[] computeFullPayrollSummary(List<String[]> employeeData, java.util.Map<String, List<String[]>> attendanceIndex, String cutoffLabel) {
        if (employeeData == null || attendanceIndex == null || cutoffLabel == null) {
            return new double[]{0, 0, 0, 0, 0};
        }
        List<Object[]> allResults = computeAllEmployees(employeeData, attendanceIndex, cutoffLabel);
        return computePayrollSummary(allResults);
    }

}
