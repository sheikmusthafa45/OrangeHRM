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

import java.util.List;

public class PIMTests {

    private static final Logger log = LoggerUtil.getLogger(PIMTests.class);

    private WebDriver driver;
    private LoginPage loginPage;
    private PIMPage pimPage;

    
    @BeforeClass(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional String browser) {
        if (browser != null && !browser.isBlank()) {
            System.setProperty("browser", browser);
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
        DriverManager.quitDriver();
        log.info("Browser closed after PIMTests class.");
    }

    @Test(priority = 1, dataProvider = "pimData", dataProviderClass = TestDataProvider.class,
            description = "Add a new employee with login (data from Excel sheet: PIM)")
    public void testAddEmployee(String firstName,
                                String middleName,
                                String lastName,
                                String userName,
                                String password,
                                String employeeId) {

        log.info("Running testAddEmployee for employee: {} {}", firstName, lastName);
        pimPage.openPIM();
        boolean saved = pimPage.addEmployeeWithLogin(firstName, middleName, lastName, userName, password, employeeId);
        Assert.assertTrue(saved, "Employee was not saved successfully.");
    }

    @Test(priority = 2, description = "Employee List tab should be visible and load without errors")
    public void testEmployeeListView() {
        log.info("Running testEmployeeListView");
        pimPage.openPIM();
        pimPage.goToEmployeeList();

        Assert.assertTrue(pimPage.isEmployeeListVisible(), "Employee List view did not load properly.");
        Assert.assertTrue(pimPage.isElementVisible("Employee Id"), "Employee ID search field is missing");

        List<String> buttons = pimPage.getVisibleButtonLabels();
        log.info("Buttons available: {}", buttons);

        List<String> headers = pimPage.getTableHeaders();
        log.info("Table Headers: {}", headers);

        int count = pimPage.getVisibleEmployeeCount();
        log.info("Total employee records found: {}", count);
    }

    @Test(priority = 3, dataProvider = "pimData", dataProviderClass = TestDataProvider.class,
            description = "Search newly added employee by employee ID")
    public void testEmployeeSearchFunctionality(String firstName,
                                                String middleName,
                                                String lastName,
                                                String userName,
                                                String password,
                                                String employeeId) {

        log.info("Running testEmployeeSearchFunctionality for employee ID: {}", employeeId);
        pimPage.goToEmployeeList();
        pimPage.searchEmployeeById(employeeId);
        Assert.assertTrue(pimPage.isEmployeeFoundInTable(employeeId),
                "Employee not found in search results: " + employeeId);
    }
}
