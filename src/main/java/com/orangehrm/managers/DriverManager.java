package com.orangehrm.managers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import com.orangehrm.utils.ConfigReader;

public class DriverManager {
	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

	public static WebDriver getDriver() {
		if (driver.get() == null) {
			setDriver();
		}
		return driver.get();
	}

	private static void setDriver() {

		String browser = System.getProperty("browser");
		if (browser == null || browser.isBlank()) {
			browser = ConfigReader.getProperty("browser");
		}

		WebDriver webDriver;
		switch (browser.toLowerCase()) {
		case "firefox":
			WebDriverManager.firefoxdriver().setup();
			webDriver = new FirefoxDriver();
			break;
		case "chrome":
		default:
			WebDriverManager.chromedriver().setup();
			webDriver = new ChromeDriver();
			break;
		}

		webDriver.manage().window().maximize();
		int implicitWait = Integer.parseInt(ConfigReader.getProperty("implicit.wait"));
		webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
		driver.set(webDriver);
	}

	public static void quitDriver() {
		if (driver.get() != null) {
			driver.get().quit();
			driver.remove();
		}
	}
}
