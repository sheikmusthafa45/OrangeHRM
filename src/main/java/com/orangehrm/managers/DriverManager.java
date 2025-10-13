package com.orangehrm.managers;

import com.orangehrm.utils.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


public class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER_TL  = new ThreadLocal<>();
    private static final ThreadLocal<String>    BROWSER_TL = new ThreadLocal<>();


    public static void setBrowser(String browser) {
        BROWSER_TL.set(browser);
    }

   
    public static WebDriver getDriver() {
        if (DRIVER_TL.get() == null) {
            setDriver();
        }
        return DRIVER_TL.get();
    }

    private static void setDriver() {
        // Resolve browser
        String browser = BROWSER_TL.get();
        if (browser == null || browser.isBlank()) browser = System.getProperty("browser");
        if (browser == null || browser.isBlank()) browser = ConfigReader.getProperty("browser");
        if (browser == null || browser.isBlank()) browser = "chrome";

       
        String acceptLang = ConfigReader.getProperty("browser.lang");
        if (acceptLang == null || acceptLang.isBlank()) acceptLang = "en-US,en;q=0.9";
        String primaryLang = acceptLang.split(",")[0].trim(); 

        WebDriver driver;

        switch (browser.toLowerCase()) {
            case "firefox": {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ff = new FirefoxOptions();

               
                try {
                    Path profile = Files.createTempDirectory("ff-profile-");
                    ff.addArguments("-profile", profile.toAbsolutePath().toString());
                } catch (Exception ignored) {}

            
                ff.addPreference("intl.accept_languages", acceptLang);      
                ff.addPreference("intl.locale.requested", primaryLang);     
              
                ff.addPreference("browser.translations.automaticallyPopup", false);
                ff.addPreference("browser.translations.enable", false);

                driver = new FirefoxDriver(ff);
                break;
            }
            case "chrome":
            default: {
                WebDriverManager.chromedriver().setup();

                ChromeOptions ch = new ChromeOptions();

              
                try {
                    Path profile = Files.createTempDirectory("chrome-profile-");
                    ch.addArguments("--user-data-dir=" + profile.toAbsolutePath());
                    ch.addArguments("--profile-directory=Default");
                } catch (Exception ignored) {}

               
                ch.addArguments("--lang=" + primaryLang);
                ch.addArguments("--disable-features=Translate");
                ch.addArguments("--disable-features=LanguageSettings");

              
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("intl.accept_languages", acceptLang);
                prefs.put("translate.enabled", false);
                prefs.put("translate_enabled", false);
                ch.setExperimentalOption("prefs", prefs);

                ChromeDriver cd = new ChromeDriver(ch);

          
                try {
                    cd.executeCdpCommand("Network.enable", new HashMap<>()); 
                    Map<String, Object> headers = new HashMap<>();
                    headers.put("Accept-Language", acceptLang);
                    Map<String, Object> params = new HashMap<>();
                    params.put("headers", headers);
                    cd.executeCdpCommand("Network.setExtraHTTPHeaders", params);
                } catch (Exception ignored) {
                    
                }

                driver = cd;
                break;
            }
        }

  
        driver.manage().window().maximize();
        int implicitWait = 0;
        try {
            String iw = ConfigReader.getProperty("implicit.wait");
            implicitWait = (iw == null || iw.isBlank()) ? 0 : Integer.parseInt(iw.trim());
        } catch (Exception ignored) {}
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

        try { driver.manage().deleteAllCookies(); } catch (Exception ignored) {}

        DRIVER_TL.set(driver);
    }

    
    public static void clearCookies() {
        WebDriver d = DRIVER_TL.get();
        if (d != null) {
            try { d.manage().deleteAllCookies(); } catch (Exception ignored) {}
        }
    }

 
    public static void quitDriver() {
        WebDriver d = DRIVER_TL.get();
        if (d != null) {
            try { d.quit(); } finally { DRIVER_TL.remove(); }
        }
        BROWSER_TL.remove();
    }
}
