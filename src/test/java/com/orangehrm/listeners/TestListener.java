package com.orangehrm.listeners;

import com.orangehrm.managers.DriverManager;

import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.ReportManager;
import com.orangehrm.utils.LoggerUtil;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestListener implements ITestListener {
	private static ExtentReports extent = ReportManager.getInstance();
	private static ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
	private static final Logger log = LoggerUtil.getLogger(TestListener.class);

	@Override
	public void onTestStart(ITestResult result) {
		ExtentTest test = extent.createTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
		testThread.set(test);
		log.info("STARTING TEST: " + result.getMethod().getMethodName());
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		testThread.get().log(Status.PASS, "Test Passed");
		log.info("PASSED TEST: " + result.getMethod().getMethodName());
	}

	@Override
	public void onTestFailure(ITestResult result) {
		WebDriver driver = DriverManager.getDriver();
		String testName = result.getName();
		String screenshotPath = "";
		try {
			screenshotPath = takeScreenshot(driver, testName);
			testThread.get().addScreenCaptureFromPath(screenshotPath);
			log.error("FAILED TEST: " + testName + " | Screenshot saved at: " + screenshotPath, result.getThrowable());
		} catch (IOException e) {
			testThread.get().log(Status.WARNING, "Screenshot could not be captured: " + e.getMessage());
			log.error("Could not capture screenshot for failed test: " + testName, e);
		}
		testThread.get().log(Status.FAIL, "Test Failed: " + result.getThrowable());
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		testThread.get().log(Status.SKIP, "Test Skipped: " + result.getThrowable());
		log.warn("SKIPPED TEST: " + result.getMethod().getMethodName());
	}

	@Override
	public void onFinish(ITestContext context) {
		extent.flush(); 
		log.info("TEST EXECUTION FINISHED. Extent report generated.");
	}


	private String takeScreenshot(WebDriver driver, String testName) throws IOException {
		String base = ConfigReader.getProperty("screenshot.path");
		File dir = new File(base);
		if (!dir.exists()) dir.mkdirs();

		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		String screenshotName = testName + "_" + timestamp + ".png";
		String sep = base.endsWith("/") || base.endsWith("\\") ? "" : File.separator;
		String path = base + sep + screenshotName;

		File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(screenshot, new File(path));
		return path;
	}
}
