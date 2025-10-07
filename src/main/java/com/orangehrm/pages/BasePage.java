package com.orangehrm.pages;

import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;


public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    private static final Logger log = LoggerUtil.getLogger(BasePage.class);

    @FindBy(css = "div.oxd-toast")
    private WebElement toastMessage;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        int explicitWait = Integer.parseInt(ConfigReader.getProperty("explicit.wait"));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
    }

   
    protected void type(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        log.info("Typing text: " + text);
        element.clear();
        element.sendKeys(text);
    }

   
    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        log.info("Clicked element: " + element);
    }

    
    public boolean isElementDisplayed(WebElement element) {
        try {
            boolean displayed = element.isDisplayed();
            log.info("Element displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            log.error("Element not found or not visible: " + e.getMessage());
            return false;
        }
    }

    
    protected void waitForElementVisible(WebElement element) {
        log.info("Waiting for element visibility: " + element);
        wait.until(ExpectedConditions.visibilityOf(element));
    }

  
    protected boolean isToastDisplayed(String expectedText) {
        try {
            wait.until(ExpectedConditions.visibilityOf(toastMessage));
            String actual = toastMessage.getText();
            log.info("Toast displayed with text: " + actual);
            return actual.contains(expectedText);
        } catch (Exception e) {
            log.error("Toast not displayed: " + e.getMessage());
            return false;
        }
    }
}
