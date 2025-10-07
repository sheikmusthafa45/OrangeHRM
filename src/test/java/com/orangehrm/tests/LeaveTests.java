package com.orangehrm.tests;

import com.orangehrm.data.TestDataProvider;
import com.orangehrm.listeners.TestListener;
import com.orangehrm.managers.DriverManager;
import com.orangehrm.pages.LeavePage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.ExcelReader;
import com.orangehrm.utils.LoggerUtil;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;
@Listeners(TestListener.class)
public class LeaveTests {
	private static final Logger log = LoggerUtil.getLogger(LeaveTests.class);
    private WebDriver driver;
    private LoginPage loginPage;
    private LeavePage leavePage;

    private static final String DEF_EMP     = "Musthafa Sheik Sheik";
    private static final String DEF_TYPE    = "CAN - Bereavement";
    private static final String DEF_FROM    = "2025-10-08";
    private static final String DEF_TO      = "2025-10-08";
    private static final String DEF_COMMENT = "Bereavement leave - 1 day";

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
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }


    @Test(priority = 1, description = "Verify Apply functionality (passes if 'No Leave Types with Leave Balance' banner shown).")
    public void verifyApply_Smart() {
        boolean ok = leavePage.applyLeaveSmart(DEF_TYPE, DEF_FROM, DEF_TO, DEF_COMMENT);
        Assert.assertTrue(ok, "Apply did not succeed nor show the expected info banner.");
    }

    
    @Test(priority = 2, dataProvider = "leaveApplyData", dataProviderClass = TestDataProvider.class,
          description = "Apply Leave (data-driven) — sheet: LeaveApply")
    public void verifyApply_DataDriven(String empName, String leaveType, String action,
                                       String fromDate, String toDate, String comment, String expected) {
        boolean ok = leavePage.applyLeaveSmart(
                (leaveType == null || leaveType.isBlank()) ? DEF_TYPE : leaveType,
                (fromDate == null || fromDate.isBlank()) ? DEF_FROM : fromDate,
                (toDate == null || toDate.isBlank()) ? DEF_TO : toDate,
                (comment == null || comment.isBlank()) ? DEF_COMMENT : comment
        );
        Assert.assertTrue(ok, "Apply (data-driven) failed. Expected: " + expected);
    }


    @Test(priority = 3, description = "Verify Assign functionality (autocomplete picks suggestion).")
    public void verifyAssign_Basic() {
        boolean ok = leavePage.assignLeave(DEF_EMP, DEF_TYPE, DEF_FROM, DEF_TO, DEF_COMMENT);
        Assert.assertTrue(ok, "Assign leave failed.");
    }


    @Test(priority = 4, dataProvider = "leaveAssignData", dataProviderClass = TestDataProvider.class,
          description = "Assign Leave (data-driven) — sheet: LeaveAssign")
    public void verifyAssign_DataDriven(String empName, String leaveType, String fromDate, String toDate, String comment) {
        boolean ok = leavePage.assignLeave(
                (empName == null || empName.isBlank()) ? DEF_EMP : empName,
                (leaveType == null || leaveType.isBlank()) ? DEF_TYPE : leaveType,
                (fromDate == null || fromDate.isBlank()) ? DEF_FROM : fromDate,
                (toDate == null || toDate.isBlank()) ? DEF_TO : toDate,
                (comment == null || comment.isBlank()) ? DEF_COMMENT : comment
        );
        Assert.assertTrue(ok, String.format("Assign failed for %s (%s, %s-%s)", empName, leaveType, fromDate, toDate));
    }

  
    @Test(priority = 5, description = "Verify My Leave table loads for the date range and optional Status (sheet: MyLeave).")
    public void verifyMyLeave_FromSheet() {
        Object[][] data = ExcelReader.getTestData("MyLeave");
        String from = DEF_FROM, to = DEF_TO, status = null;
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

    
    @Test(priority = 6, description = "Verify Entitlements → Add Entitlements shows correct Leave Types (optional sheet: LeaveTypes).")
    public void verifyEntitlements_LeaveTypesList() {
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


    @Test(priority = 7, description = "Verify Leave Balance report renders rows.")
    public void verifyReports_LeaveBalance() {
        String reportLeaveType = DEF_TYPE;
        Object[][] ent = ExcelReader.getTestData("LeaveEntitlements");
        if (ent.length > 0 && ent[0].length >= 2) {
            String lt = String.valueOf(ent[0][1]);
            if (lt != null && !lt.isBlank()) reportLeaveType = lt;
        }
        int rows = leavePage.openLeaveBalanceReportAndCount(reportLeaveType);
        Assert.assertTrue(rows >= 0, "Report did not render.");
        Reporter.log("Leave Balance rows: " + rows, true);
    }

    @AfterMethod(alwaysRun = true)
    public void afterEach(ITestResult result) {
        
    }
}
