package com.orangehrm.tests;

import com.orangehrm.data.TestDataProvider;
import com.orangehrm.listeners.TestListener;
import com.orangehrm.managers.DriverManager;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.util.List;

@Listeners(TestListener.class)
public class DashboardTests {

	private static final Logger log = LoggerUtil.getLogger(DashboardTests.class);

	private WebDriver driver;
	private LoginPage loginPage;
	private DashboardPage dashboardPage;

	@BeforeMethod(alwaysRun = true)
	@Parameters("browser")
	public void loginFirst(@Optional String browser) {
		if (browser != null && !browser.isBlank()) {
			System.setProperty("browser", browser);
			log.info("Launching tests on browser: {}", browser);
		}

		driver = DriverManager.getDriver();
		driver.get(ConfigReader.getProperty("url"));
		loginPage = new LoginPage(driver);
		dashboardPage = new DashboardPage(driver);

		loginPage.login(ConfigReader.getProperty("default.username"),
				ConfigReader.getProperty("default.password"));
		Assert.assertTrue(dashboardPage.isDashboardDisplayed(), "Dashboard should be visible after login.");
		log.info("Successfully logged in and Dashboard loaded.");
	}

	@AfterMethod(alwaysRun = true)
	public void cleanup(ITestResult result) {
		log.info("=== Test '{}' finished with status: {} ===",
				result.getName(),
				result.getStatus() == 1 ? "PASS" : result.getStatus() == 2 ? "FAIL" : "SKIP");
		DriverManager.quitDriver();
	}


	@Test(priority = 1, dataProvider = "dashboardMenus", dataProviderClass = TestDataProvider.class,
			description = "Verify Dashboard header and main menus are visible")
	public void verifyDashboardElementsAfterLogin(List<String> menus) {
		log.info("Running verifyDashboardElementsAfterLogin with menus: {}", menus);
		Assert.assertTrue(dashboardPage.isDashboardDisplayed(), "Dashboard header not visible.");
		for (String menu : menus) {
			Assert.assertTrue(dashboardPage.isMenuVisible(menu), "Menu not visible: " + menu);
		}
	}


	@Test(priority = 2, dataProvider = "dashboardQuickLaunchTiles", dataProviderClass = TestDataProvider.class,
			description = "Verify Quick Launch widget and tiles are displayed")
	public void verifyQuickLaunchWidgets(List<String> tiles) {
		log.info("Running verifyQuickLaunchWidgets with tiles: {}", tiles);
		Assert.assertTrue(dashboardPage.isQuickLaunchVisible(), "Quick Launch should be visible.");

		int visible = 0;
		for (String tile : tiles) {
			if (dashboardPage.isQuickLaunchTileVisible(tile)) visible++;
		}
		Assert.assertTrue(visible >= Math.min(2, tiles.size()),
				"Expected at least 2 visible Quick Launch tiles from " + tiles + " but found " + visible);
	}


	@Test(priority = 3, description = "Verify Employee Distribution chart and title are visible")
	public void verifyEmployeeDistributionChart() {
		log.info("Running verifyEmployeeDistributionChart");
		Assert.assertTrue(dashboardPage.isEmployeeDistributionTitleVisible(), "Employee Distribution title missing.");
		Assert.assertTrue(dashboardPage.isEmployeeDistributionChartVisible(), "Employee Distribution chart not visible.");
	}


	@Test(priority = 4, description = "Verify Pending Leave Requests widget title when visible")
	public void verifyPendingLeaveRequests() {
		log.info("Running verifyPendingLeaveRequests");
		if (!dashboardPage.hasPendingLeaveWidget()) {
			throw new SkipException("No pending leave requests; widget not shown in demo.");
		}
		Assert.assertTrue(dashboardPage.isPendingLeaveTitleVisible(), "Pending Leave title should be visible.");
	}


	@Test(priority = 5, dataProvider = "dashboardBaselineWidgets", dataProviderClass = TestDataProvider.class,
			description = "Verify baseline widgets exist in dashboard layout")
	public void verifyDashboardCustomization(List<String> widgets) {
		log.info("Running verifyDashboardCustomization with widgets: {}", widgets);
		for (String w : widgets) {
			Assert.assertTrue(dashboardPage.isWidgetTitleVisible(w), "Widget not visible: " + w);
		}
	}
}
