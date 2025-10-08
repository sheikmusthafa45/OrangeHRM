package com.orangehrm.tests;

import com.orangehrm.data.TestDataProvider;

import com.orangehrm.managers.DriverManager;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;


public class LoginTests {

	private static final Logger log = LoggerUtil.getLogger(LoginTests.class);

	private WebDriver driver;
	private LoginPage loginPage;
	private DashboardPage dashboardPage;

	@BeforeMethod(alwaysRun = true)
	@Parameters("browser")
	public void setUp(@Optional String browser) {
		if (browser != null && !browser.isBlank()) {
			System.setProperty("browser", browser);
			log.info("Launching tests on browser: {}", browser);
		}

		driver = DriverManager.getDriver();
		driver.get(ConfigReader.getProperty("url"));
		loginPage = new LoginPage(driver);
		dashboardPage = new DashboardPage(driver);
		log.info("=== Starting new test session ===");
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestResult result) {
		log.info("=== Test '{}' completed with status: {} ===",
				result.getName(),
				result.getStatus() == 1 ? "PASS" : result.getStatus() == 2 ? "FAIL" : "SKIP");
		DriverManager.quitDriver();
	}


	@Test(priority = 1, description = "Verify successful login with valid credentials")
	public void testValidLogin() {
		log.info("Running: testValidLogin");
		loginPage.login(ConfigReader.getProperty("default.username"), ConfigReader.getProperty("default.password"));

		Assert.assertTrue(dashboardPage.isDashboardDisplayed(), "Dashboard should be visible after successful login.");
		loginPage.logout();
		Assert.assertTrue(loginPage.isLoginPageVisible(), "Login page should be visible after logout.");
	}


	@Test(priority = 2, dataProvider = "loginInvalidUser", dataProviderClass = TestDataProvider.class,
			description = "Verify login with invalid username")
	public void testInvalidUsername(String username, String password) {
		log.info("Running: testInvalidUsername with username='{}'", username);
		loginPage.login(username, password);

		Assert.assertTrue(loginPage.isLoginPageVisible(), "Should remain on login page after invalid username.");
		Assert.assertTrue(loginPage.getErrorText().contains("Invalid credentials"),
				"Error message should indicate invalid credentials.");
	}


	@Test(priority = 3, dataProvider = "loginInvalidPassword", dataProviderClass = TestDataProvider.class,
			description = "Verify login with invalid password")
	public void testInvalidPassword(String username, String password) {
		log.info("Running: testInvalidPassword with password='{}'", password);
		loginPage.login(username, password);

		Assert.assertTrue(loginPage.isLoginPageVisible(), "Should remain on login page after invalid password.");
		Assert.assertTrue(loginPage.getErrorText().contains("Invalid credentials"),
				"Error message should indicate invalid credentials.");
	}


	@Test(priority = 4, description = "Verify login with empty credentials")
	public void testEmptyCredentials() {
		log.info("Running: testEmptyCredentials");
		loginPage.login("", "");

		Assert.assertTrue(loginPage.isLoginPageVisible(), "Login page should remain visible.");
		Assert.assertTrue(loginPage.hasRequiredValidation(), "Expected 'Required' validation message.");
	}


	@Test(priority = 5, description = "Verify logout functionality")
	public void testLogoutFunctionality() {
		log.info("Running: testLogoutFunctionality");
		loginPage.login(ConfigReader.getProperty("default.username"), ConfigReader.getProperty("default.password"));
		Assert.assertTrue(dashboardPage.isDashboardDisplayed(), "Precondition failed: dashboard not visible after login.");
		loginPage.logout();
		Assert.assertTrue(loginPage.isLoginPageVisible(), "Login page should be visible after logout.");
	}
}
