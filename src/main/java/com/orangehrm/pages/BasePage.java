package com.orangehrm.pages;

import com.orangehrm.utils.ConfigReader;

import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
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

    
    private final long afterClickPauseMs;
    private final long afterTypePauseMs;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        int explicitWait = parseIntSafe(ConfigReader.getProperty("explicit.wait"), 20);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
        this.afterClickPauseMs = parseIntSafe(ConfigReader.getProperty("after.click.pause.ms"), 150);
        this.afterTypePauseMs  = parseIntSafe(ConfigReader.getProperty("after.type.pause.ms"), 0);
    }

    protected void type(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        log.info("Typing text: {}", text);
        try { element.clear(); } catch (Exception ignored) {}
        if (text != null) element.sendKeys(text);
        pause(afterTypePauseMs);
    }

    protected void click(WebElement element) throws ElementClickInterceptedException {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
            log.info("Clicked element: {}", element);
            pause(afterClickPauseMs);
            return;
        } catch (ElementNotInteractableException e) {
            log.debug("Click intercepted/not interactable, scrolling + retry: {}", e.getMessage());
            try {
                scrollIntoViewCenter(element);
                wait.until(ExpectedConditions.elementToBeClickable(element)).click();
                log.info("Clicked element after scroll: {}", element);
                pause(afterClickPauseMs);
                return;
            } catch (ElementNotInteractableException e2) {
                log.debug("Retry still intercepted, JS click fallback: {}", e2.getMessage());
                jsClick(element);
                log.info("Clicked element via JS: {}", element);
                pause(afterClickPauseMs);
                return;
            }
        } catch (StaleElementReferenceException e) {
            log.debug("Element stale; re-waiting and retrying click.");
            wait.until(driver1 -> {
                try {
                    element.isEnabled();
                    return ExpectedConditions.elementToBeClickable(element).apply(driver1);
                } catch (StaleElementReferenceException se) {
                    return null;
                }
            }).click();
            log.info("Clicked after staleness recovery: {}", element);
            pause(afterClickPauseMs);
            return;
        } catch (TimeoutException e) {
            log.debug("Timeout waiting clickable; JS click fallback: {}", e.getMessage());
            jsClick(element);
            log.info("Clicked element via JS after timeout: {}", element);
            pause(afterClickPauseMs);
            return;
        } catch (WebDriverException e) {
            log.debug("Native click failed ({}). JS fallback.", e.getMessage());
            jsClick(element);
            log.info("Clicked element via JS (fallback): {}", element);
            pause(afterClickPauseMs);
        }
    }

    public boolean isElementDisplayed(WebElement element) {
        try {
            boolean displayed = element.isDisplayed();
            log.info("Element displayed: {}", displayed);
            return displayed;
        } catch (Exception e) {
            log.error("Element not visible: {}", e.getMessage());
            return false;
        }
    }

    protected void waitForElementVisible(WebElement element) {
        log.info("Waiting for element visibility: {}", element);
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected boolean isToastDisplayed(String expectedText) {
        try {
            wait.until(ExpectedConditions.visibilityOf(toastMessage));
            String actual = toastMessage.getText();
            log.info("Toast displayed with text: {}", actual);
            return actual != null && actual.contains(expectedText);
        } catch (Exception e) {
            log.info("Toast not displayed (expected='{}')", expectedText);
            return false;
        }
    }

    
    private void scrollIntoViewCenter(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'center', behavior:'instant'});",
                element
            );
        } catch (Exception ignored) {}
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private void pause(long ms) {
        if (ms <= 0) return;
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    private int parseIntSafe(String v, int def) {
        if (v == null) return def;
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return def; }
    }
}
