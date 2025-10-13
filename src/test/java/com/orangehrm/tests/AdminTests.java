package com.orangehrm.tests;

import com.orangehrm.pages.AdminPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.data.TestDataProvider;
import com.orangehrm.managers.DriverManager;
import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

public class AdminTests {

    private static final Logger log = LoggerUtil.getLogger(AdminTests.class);

    private WebDriver driver;
    private LoginPage loginPage;
    private AdminPage adminPage;

    @BeforeClass(alwaysRun = true)
    @Parameters("browser")
    public void setup(@Optional String browser) {
        if (browser != null && !browser.isBlank()) {
            DriverManager.setBrowser(browser);
            log.info("Launching tests on browser: {}", browser);
        }

        driver = DriverManager.getDriver();
        driver.get(ConfigReader.getProperty("url"));
        loginPage = new LoginPage(driver);
        adminPage = new AdminPage(driver);

        loginPage.login(
                ConfigReader.getProperty("default.username"),
                ConfigReader.getProperty("default.password")
        );
        adminPage.openAdminModule();
        log.info("Logged in successfully and opened Admin module (once for AdminTests class).");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
        log.info("Browser closed after AdminTests class.");
    }

    @Test(priority = 1, dataProvider = "adminDataBundle", dataProviderClass = TestDataProvider.class)
    public void verifyUserManagementFunctionality(Object[][] rows) {
        for (Object[] r : rows) {
            String empName          = (String) r[0];
            String username         = (String) r[1];
            String password         = (String) r[2];
            String jobTitle         = (String) r[3];
            String jobDescription   = (String) r[4];
            String payGrade         = (String) r[5];
            String employmentStatus = (String) r[6];
            String currency         = (String) r[7];
            String minSalary        = (String) r[8];
            String maxSalary        = (String) r[9];
            String orgUnit          = (String) r[10];
            String subUnit          = (String) r[11];

            log.info("Running verifyUserManagementFunctionality for username: {}", username);
            adminPage.navigateToUserManagement();
            Assert.assertTrue(
                    adminPage.addUser("ESS", empName, "Enabled", username, password),
                    "Failed to add user"
            );

            String actualUsername = adminPage.getLastCreatedUsername();
            if (actualUsername == null || actualUsername.isBlank()) {
                actualUsername = username;
            }

            Assert.assertTrue(
                    adminPage.searchUser(actualUsername),
                    "User not found in search: " + actualUsername
            );
        }
    }

    @Test(priority = 2, dataProvider = "adminDataBundle", dataProviderClass = TestDataProvider.class)
    public void verifyJobTitlesManagement(Object[][] rows) {
        for (Object[] r : rows) {
            String jobTitle       = (String) r[3];
            String jobDescription = (String) r[4];

            log.info("Running verifyJobTitlesManagement for job title: {}", jobTitle);
            Assert.assertTrue(
                    adminPage.addJobTitle(jobTitle, jobDescription),
                    "Failed to add job title"
            );
            Assert.assertTrue(
                    adminPage.verifyJobTitle(jobTitle),
                    "Job title not listed after creation"
            );
        }
    }

    @Test(priority = 3, dataProvider = "adminDataBundle", dataProviderClass = TestDataProvider.class)
    public void verifyPayGradesManagement(Object[][] rows) {
        for (Object[] r : rows) {
            String payGrade  = (String) r[5];
            String currency  = (String) r[7];
            String minSalary = (String) r[8];
            String maxSalary = (String) r[9];

            log.info("Running verifyPayGradesManagement for pay grade: {}", payGrade);
            Assert.assertTrue(
                    adminPage.addPayGradeWithSalary(payGrade, currency, minSalary, maxSalary),
                    "Failed to add pay grade with currency and salary range"
            );
            Assert.assertTrue(
                    adminPage.verifyPayGrade(payGrade),
                    "Pay grade not listed"
            );
        }
    }

    @Test(priority = 4, dataProvider = "adminDataBundle", dataProviderClass = TestDataProvider.class)
    public void verifyEmploymentStatusManagement(Object[][] rows) {
        for (Object[] r : rows) {
            String employmentStatus = (String) r[6];

            log.info("Running verifyEmploymentStatusManagement for status: {}", employmentStatus);
            Assert.assertTrue(
                    adminPage.addEmploymentStatus(employmentStatus),
                    "Failed to add employment status"
            );
            Assert.assertTrue(
                    adminPage.verifyEmploymentStatus(employmentStatus),
                    "Employment status not listed"
            );
        }
    }

    @Test(priority = 5, description = "Verify navigation to Organization Structure page")
    public void verifyOrganizationStructure() {
        log.info("Running verifyOrganizationStructure");
        Assert.assertTrue(
                adminPage.navigateToOrganizationStructure(),
                "Failed to open Organization Structure page"
        );
    }
}
