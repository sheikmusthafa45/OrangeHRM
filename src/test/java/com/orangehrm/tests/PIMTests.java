package com.orangehrm.tests;

import com.orangehrm.data.TestDataProvider;
import com.orangehrm.managers.DriverManager;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.pages.PIMPage;
import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import java.util.List;

public class PIMTests {

    private static final Logger log = LoggerUtil.getLogger(PIMTests.class);

    private WebDriver driver;
    private LoginPage loginPage;
    private PIMPage pimPage;

    private void visualPause(String where) {
        String msStr = ConfigReader.getProperty("visual.pause.ms");
        int ms = 0;
        try { ms = (msStr == null || msStr.isBlank()) ? 0 : Integer.parseInt(msStr.trim()); } catch (Exception ignored) {}
        if (ms > 0) {
            log.info("Visual pause {}ms at: {}", ms, where);
            try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
        }
    }

    @BeforeClass(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional String browser) {
        if (browser != null && !browser.isBlank()) {
            DriverManager.setBrowser(browser); 
            log.info("Launching tests on browser: {}", browser);
        }

        driver = DriverManager.getDriver();
        driver.get(ConfigReader.getProperty("url"));
        loginPage = new LoginPage(driver);
        pimPage = new PIMPage(driver);

        loginPage.login(
                ConfigReader.getProperty("default.username"),
                ConfigReader.getProperty("default.password")
        );
        pimPage.openPIM();
        log.info("Logged in and opened PIM (once for PIMTests class).");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        boolean keepOpen = Boolean.parseBoolean(System.getProperty("keepBrowserOpen", "false"));
        String pauseStr = ConfigReader.getProperty("afterclass.pause.seconds");
        int pauseSec = 0;
        try { pauseSec = (pauseStr == null || pauseStr.isBlank()) ? 0 : Integer.parseInt(pauseStr.trim()); } catch (Exception ignored) {}

        if (keepOpen) {
            log.info("keepBrowserOpen=true -> not quitting the driver.");
            return;
        }
        if (pauseSec > 0) {
            log.info("Pausing {}s before quitting (afterclass.pause.seconds).", pauseSec);
            try { Thread.sleep(pauseSec * 1000L); } catch (InterruptedException ignored) {}
        }
        DriverManager.quitDriver();
        log.info("Browser closed after PIMTests class.");
    }

    @Test(priority = 1,
          dataProvider = "pimDataBundle",
          dataProviderClass = TestDataProvider.class,
          description = "Add new employee(s) with login from Excel (all rows)")
    public void testAddEmployee(Object[][] rows) {
        SoftAssert soft = new SoftAssert();
        for (Object[] r : rows) {
            String firstName  = (String) r[0];
            String middleName = (String) r[1];
            String lastName   = (String) r[2];
            String userName   = (String) r[3];
            String password   = (String) r[4];
            String employeeId = (String) r[5];

            log.info("[PIM] Adding employee: {} {} (empId={})", firstName, lastName, employeeId);
            pimPage.openPIM();
            boolean saved = pimPage.addEmployeeWithLogin(firstName, middleName, lastName, userName, password, employeeId);
            soft.assertTrue(saved, "Employee not saved: " + employeeId + " (" + firstName + " " + lastName + ")");
        }
        soft.assertAll();
    }

    @Test(priority = 2,
          dataProvider = "pimDataBundle",
          dataProviderClass = TestDataProvider.class,
          description = "Employee List tab should load and show basic controls")
    public void testEmployeeListView(Object[][] rows) {
        pimPage.openPIM();
        pimPage.goToEmployeeList();

        Assert.assertTrue(pimPage.isEmployeeListVisible(), "Employee List view did not load properly.");
        Assert.assertTrue(pimPage.isElementVisible("Employee Id"), "Employee ID search field is missing");

        List<String> buttons = pimPage.getVisibleButtonLabels();
        log.info("Buttons available: {}", buttons);

        List<String> headers = pimPage.getTableHeaders();
        log.info("Table Headers: {}", headers);

        int count = pimPage.getVisibleEmployeeCount();
        log.info("Visible employee rows (page slice): {}", count);

        visualPause("Employee List view");
    }

    @Test(priority = 3,
          dataProvider = "pimDataBundle",
          dataProviderClass = TestDataProvider.class,
          description = "Search newly added employee(s) by Employee Id (all rows)")
    public void testEmployeeSearchFunctionality(Object[][] rows) {
        SoftAssert soft = new SoftAssert();
        for (Object[] r : rows) {
            String employeeId = (String) r[5];
            log.info("[PIM] Validating employee search for empId={}", employeeId);

            pimPage.goToEmployeeList();
            pimPage.searchEmployeeById(employeeId);
            soft.assertTrue(pimPage.isEmployeeFoundInTable(employeeId),
                    "Employee not found in search results: " + employeeId);

            visualPause("Search results for empId=" + employeeId);
        }
        soft.assertAll();
    }

    @Test(priority = 4,
          dataProvider = "pimDataBundle",
          dataProviderClass = TestDataProvider.class,
          description = "Open employee record and verify Personal Details labels (values may be empty)")
    public void testEmployeePersonalDetails(Object[][] rows) {
        SoftAssert soft = new SoftAssert();
        for (Object[] r : rows) {
            String employeeId = (String) r[5];
            log.info("[PIM] Validating employee Personal Details for empId={}", employeeId);

            pimPage.goToEmployeeList();
            pimPage.searchEmployeeById(employeeId);

            if (!pimPage.hasAnyRow() && pimPage.isNoRecordsVisible()) {
                soft.fail("Employee not found to open Personal Details: " + employeeId);
                continue;
            }
            pimPage.openEmployeeFromSearchResults();

            var missingPD = pimPage.getMissingPersonalDetailsLabels();
            if (!missingPD.isEmpty()) {
                log.warn("Personal Details missing labels: {}", missingPD);
            }
            soft.assertTrue(missingPD.isEmpty(), "Personal Details page missing labels: " + missingPD);

            visualPause("Personal Details empId=" + employeeId);
        }
        soft.assertAll();
    }

    @Test(priority = 5,
          dataProvider = "pimDataBundle",
          dataProviderClass = TestDataProvider.class,
          description = "Open employee record and verify Job Details labels (values may be empty)")
    public void testEmployeeJobDetails(Object[][] rows) {
        SoftAssert soft = new SoftAssert();
        for (Object[] r : rows) {
            String employeeId = (String) r[5];
            log.info("[PIM] Validating employee Job Details for empId={}", employeeId);

            pimPage.goToEmployeeList();
            pimPage.searchEmployeeById(employeeId);

            if (!pimPage.hasAnyRow() && pimPage.isNoRecordsVisible()) {
                soft.fail("Employee not found to open Job Details: " + employeeId);
                continue;
            }
            pimPage.openEmployeeFromSearchResults();
            pimPage.goToJobTab();
            Assert.assertTrue(pimPage.isOnJobTab(), "Did not land on Job tab");

            var missingJob = pimPage.getMissingJobDetailsLabels();
            if (!missingJob.isEmpty()) {
                log.warn("Job Details missing labels: {}", missingJob);
            }
            soft.assertTrue(missingJob.isEmpty(), "Job Details page missing labels: " + missingJob);

            visualPause("Job Details empId=" + employeeId);
        }
        soft.assertAll();
    }
}
