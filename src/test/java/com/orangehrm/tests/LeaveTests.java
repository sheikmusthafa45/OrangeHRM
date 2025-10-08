package com.orangehrm.tests;

import com.orangehrm.managers.DriverManager;
import com.orangehrm.pages.LeavePage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.ExcelReader;
import com.orangehrm.utils.LoggerUtil;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;


public class LeaveTests {

    private static final Logger log = LoggerUtil.getLogger(LeaveTests.class);
    private WebDriver driver;
    private LoginPage loginPage;
    private LeavePage leavePage;

   
    private static final String DEF_EMP     = "Linda Anderson";
    private static final String DEF_TYPE    = "CAN - Personal";
    private static final String DEF_FROM    = "2025-10-08";
    private static final String DEF_TO      = "2025-10-09";
    private static final String DEF_COMMENT = "UI automation leave test";

    @Parameters({"browser"})
    @BeforeClass(alwaysRun = true)
    public void setup(@Optional String browser) {
        if (browser != null && !browser.isBlank()) {
            System.setProperty("browser", browser);
        }
        driver = DriverManager.getDriver();
        driver.get(ConfigReader.getProperty("url"));
        loginPage = new LoginPage(driver);
        leavePage = new LeavePage(driver);

        loginPage.login(
                ConfigReader.getProperty("default.username"),
                ConfigReader.getProperty("default.password")
        );
        log.info("Logged in for Leave module tests.");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }

    

    @Test(priority = 1, description = "Apply Leave: select type, set dates, click Apply (mirrors Assign flow).")
    public void step1_applyLeave() {
     
        String leaveType = "CAN - Bereavement"; 
        String from = DEF_FROM;
        String to = DEF_TO;
        String comment = DEF_COMMENT;

       
        Object[][] rows = ExcelReader.getTestData("LeaveApply");
        if (rows.length > 0) {
            Object[] r = rows[0];
            if (r.length > 0 && r[0] != null && !String.valueOf(r[0]).isBlank()) leaveType = String.valueOf(r[0]).trim();
            if (r.length > 1 && r[1] != null && !String.valueOf(r[1]).isBlank()) from = String.valueOf(r[1]).trim();
            if (r.length > 2 && r[2] != null && !String.valueOf(r[2]).isBlank()) to   = String.valueOf(r[2]).trim();
            if (r.length > 3 && r[3] != null && !String.valueOf(r[3]).isBlank()) comment = String.valueOf(r[3]).trim();
        }

        boolean ok = leavePage.applyLeaveSmart(leaveType, from, to, comment);
        Assert.assertTrue(ok, "Apply leave did not show success toast or the info banner.");
    }

    @Test(priority = 2, description = "Assign Leave to an employee (robust autocomplete selection).")
    public void step2_assignLeave() {
      
        String empName = DEF_EMP;
        String leaveType = DEF_TYPE;
        String from = DEF_FROM;
        String to = DEF_TO;
        String comment = DEF_COMMENT;

       
        Object[][] rows = ExcelReader.getTestData("LeaveAssign");
        if (rows.length > 0) {
            Object[] r = rows[0];
            if (r.length > 0 && r[0] != null && !String.valueOf(r[0]).isBlank()) empName = String.valueOf(r[0]).trim();
            if (r.length > 1 && r[1] != null && !String.valueOf(r[1]).isBlank()) leaveType = String.valueOf(r[1]).trim();
            if (r.length > 2 && r[2] != null && !String.valueOf(r[2]).isBlank()) from = String.valueOf(r[2]).trim();
            if (r.length > 3 && r[3] != null && !String.valueOf(r[3]).isBlank()) to = String.valueOf(r[3]).trim();
            if (r.length > 4 && r[4] != null && !String.valueOf(r[4]).isBlank()) comment = String.valueOf(r[4]).trim();
        }

        boolean ok = leavePage.assignLeave(empName, leaveType, from, to, comment);
        Assert.assertTrue(ok, "Assign leave failed.");
    }

    @Test(priority = 3, description = "My Leave page: table renders for given date range (and optional Status if provided in Excel).")
    public void step3_myLeaveTable() {
        String from = DEF_FROM, to = DEF_TO, status = null;
        Object[][] data = ExcelReader.getTestData("MyLeave");
        if (data.length > 0 && data[0].length >= 5) {
            String sFrom = String.valueOf(data[0][2]);
            String sTo   = String.valueOf(data[0][3]);
            String sStat = String.valueOf(data[0][4]);
            if (sFrom != null && !sFrom.isBlank()) from = sFrom;
            if (sTo   != null && !sTo.isBlank())   to   = sTo;
            if (sStat != null && !sStat.isBlank()) status = sStat;
        }
        boolean ok = leavePage.myLeaveHasRows(from, to, status);
        Assert.assertTrue(ok, "My Leave table did not load for range " + from + " - " + to + (status != null ? (" with status " + status) : ""));
    }

    @Test(priority = 4, description = "Entitlements → Add Entitlements: verify Leave Type dropdown values (optional compare with sheet LeaveTypes).")
    public void step4_entitlementsList() {
        List<String> actual = leavePage.listEntitlementLeaveTypes();
        Reporter.log("Entitlements Leave Types (actual): " + actual, true);

        Object[][] data = ExcelReader.getTestData("LeaveTypes");
        List<String> expected = new ArrayList<>();
        for (Object[] row : data) {
            if (row.length > 0) {
                String val = String.valueOf(row[0]).trim();
                if (!val.isEmpty() && !"LeaveType".equalsIgnoreCase(val)) {
                    expected.add(val);
                }
            }
        }

        if (expected.isEmpty()) {
            Assert.assertTrue(actual != null && !actual.isEmpty(), "No leave types found in Entitlements dropdown.");
        } else {
            Reporter.log("Entitlements Leave Types (expected from sheet): " + expected, true);
            for (String e : expected) {
                Assert.assertTrue(actual.contains(e),
                        "Missing leave type in Entitlements dropdown: " + e + " | Actual: " + actual);
            }
        }
    }

    @Test(priority = 5, description = "Reports → Leave Balance: verify rows render (>= 0).")
    public void step5_leaveBalanceReport() {
        String reportLeaveType = "CAN - Bereavement"; 
        Object[][] ent = ExcelReader.getTestData("LeaveEntitlements");
        if (ent.length > 0 && ent[0].length >= 2) {
            String lt = String.valueOf(ent[0][1]);
            if (lt != null && !lt.isBlank()) reportLeaveType = lt;
        }
        int rows = leavePage.openLeaveBalanceReportAndCount(reportLeaveType);
        Assert.assertTrue(rows >= 0, "Leave Balance report did not render.");
        Reporter.log("Leave Balance rows: " + rows, true);
    }
}
