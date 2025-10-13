package com.orangehrm.tests;

import com.orangehrm.managers.DriverManager;
import com.orangehrm.pages.LeavePage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.ExcelReader;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

public class LeaveTests {

    private WebDriver driver;
    private LoginPage login;
    private LeavePage leave;

    @Parameters({"browser"})
    @BeforeClass(alwaysRun = true)
    public void setUp(@Optional String browser) {
        if (browser != null && !browser.isBlank()) {
            DriverManager.setBrowser(browser); 
        }
        driver = DriverManager.getDriver();
        driver.get(ConfigReader.getProperty("url"));
        login = new LoginPage(driver);
        leave = new LeavePage(driver);
        login.login(
                ConfigReader.getProperty("default.username"),
                ConfigReader.getProperty("default.password")
        );
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }

    @DataProvider(name = "applyData")
    public Object[][] applyData() {
        Object[][] rows = ExcelReader.getTestData("LeaveApply"); 
        if (rows == null || rows.length == 0) throw new SkipException("LeaveApply is empty.");
        return rows;
    }

    @DataProvider(name = "assignData")
    public Object[][] assignData() {
        Object[][] rows = ExcelReader.getTestData("LeaveAssign"); 
        if (rows == null || rows.length == 0) throw new SkipException("LeaveAssign is empty.");
        return rows;
    }

    @DataProvider(name = "myLeaveData")
    public Object[][] myLeaveData() {
        Object[][] rows = ExcelReader.getTestData("MyLeave"); 
        if (rows == null || rows.length == 0) throw new SkipException("MyLeave is empty.");
        Object[][] norm = new Object[rows.length][5];
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < 5; j++) {
                Object v = (j < rows[i].length) ? rows[i][j] : "";
                norm[i][j] = (v == null) ? "" : String.valueOf(v).trim();
            }
        }
        return norm;
    }

    @Test(priority = 1, dataProvider = "applyData", description = "Apply Leave")
    public void testApplyLeave(String leaveType, String fromDate, String toDate, String comment) {
        boolean ok = leave.applyLeave(leaveType, fromDate, toDate, comment);
        Assert.assertTrue(ok, "Apply Leave did not show a success/info toast.");
    }

    @Test(priority = 2, dataProvider = "assignData", description = "Assign Leave")
    public void testAssignLeave(String empName, String leaveType, String fromDate, String toDate, String comment) {
        boolean ok = leave.assignLeave(empName, leaveType, fromDate, toDate, comment);
        Assert.assertTrue(ok, "Assign Leave failed (no success toast).");
    }

    @Test(priority = 3, dataProvider = "myLeaveData", description = "My Leave search")
    public void testMyLeave(String empName, String leaveType, String fromDate, String toDate, String status) {
        int rows = leave.myLeaveSearch(fromDate, toDate, status);
        Assert.assertTrue(rows >= 0, "My Leave table did not render.");
    }
}
