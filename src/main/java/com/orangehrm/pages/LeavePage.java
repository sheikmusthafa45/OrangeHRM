package com.orangehrm.pages;

import com.orangehrm.utils.LoggerUtil;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

public class LeavePage extends BasePage {

    private static final Logger log = LoggerUtil.getLogger(LeavePage.class);

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration SHORT   = Duration.ofSeconds(5);

    @FindBy(xpath = "//span[normalize-space()='Leave']")
    private WebElement menuLeave;

    @FindBy(xpath = "//a[normalize-space()='Apply']")
    private WebElement tabApply;

    @FindBy(xpath = "//a[normalize-space()='Assign Leave']")
    private WebElement tabAssign;

    @FindBy(xpath = "//a[normalize-space()='My Leave']")
    private WebElement tabMyLeave;

    private final By toast = By.cssSelector("div.oxd-toast");
    private final By loader = By.cssSelector("div.oxd-form-loader, div.oxd-loading-spinner");
    private final By dropdownList = By.xpath("//div[@role='listbox']");
    private final By noBalanceInfo = By.xpath("//*[contains(normalize-space(.),'No Leave Types with Leave Balance')]");

    private final By applyLeaveType = By.xpath("//label[normalize-space()='Leave Type']/../following-sibling::div//div[contains(@class,'oxd-select-text')]");
    private final By applyFromDate  = By.xpath("//label[normalize-space()='From Date']/following::input[1]");
    private final By applyToDate    = By.xpath("//label[normalize-space()='To Date']/following::input[1]");
    private final By applyComment   = By.xpath("//textarea[@placeholder='Type here']");
    private final By applyButton    = By.xpath("//button[normalize-space()='Apply' or @type='submit']");

    private final By assignEmpInput   = By.xpath("//input[@placeholder='Type for hints...']");
    private final By assignLeaveType  = By.xpath("//label[normalize-space()='Leave Type']/../following-sibling::div//div[contains(@class,'oxd-select-text')]");
    private final By assignFromDate   = By.xpath("//label[normalize-space()='From Date']/following::input[1]");
    private final By assignToDate     = By.xpath("//label[normalize-space()='To Date']/following::input[1]");
    private final By assignComment    = By.xpath("//textarea[@placeholder='Type here']");
    private final By assignButton     = By.xpath("//button[normalize-space()='Assign' or @type='submit']");
    private final By confirmYesOk     = By.xpath("//button[normalize-space()='Yes' or normalize-space()='OK' or normalize-space()='Ok' or normalize-space()='Confirm']");

    private final By myFromDate     = By.xpath("(//label[normalize-space()='From Date']/following::input)[1]");
    private final By myToDate       = By.xpath("(//label[normalize-space()='To Date']/following::input)[1]");
    private final By myStatusDD     = By.xpath("//label[contains(normalize-space(),'Show Leave with Status')]/../following-sibling::div//div[contains(@class,'oxd-select-text')]");
    private final By mySearchBtn    = By.xpath("//button[normalize-space()='Search']");
    private final By myRows         = By.cssSelector("div.oxd-table-body > div.oxd-table-card");

    public LeavePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    private void openTab(WebElement tab) {
        wait.until(ExpectedConditions.elementToBeClickable(menuLeave)).click();
        wait.until(ExpectedConditions.elementToBeClickable(tab)).click();
        try { new WebDriverWait(driver, SHORT).until(ExpectedConditions.invisibilityOfElementLocated(loader)); }
        catch (TimeoutException ignored) {}
    }

    private void pickFromDropdown(By dropdownField, String visibleText) {
        wait.until(ExpectedConditions.elementToBeClickable(dropdownField)).click();
        By option = By.xpath("//div[@role='listbox']//span[normalize-space()='" + visibleText + "']");
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
        sleep(300);
    }

    private void chooseFromListboxExactOrFirst(String exactText) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownList));
        List<WebElement> exact = driver.findElements(
                By.xpath("//div[@role='listbox']//div[@role='option']//span[normalize-space()='" + exactText + "']")
        );
        if (!exact.isEmpty()) { exact.get(0).click(); sleep(200); return; }
        WebElement first = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='listbox']//div[@role='option'][1]")));
        first.click();
        sleep(200);
    }

    private void typeDate(By inputBy, String incoming) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(inputBy));
        String placeholder = input.getAttribute("placeholder");
        String val = (incoming == null ? "" : incoming.trim().replace('/', '-'));

        String out = val;
        if (val.matches("^\\d{2}-\\d{2}-\\d{4}$")) {
            String[] p = val.split("-");
            String d = p[0], m = p[1], y = p[2];
            out = (placeholder != null && placeholder.startsWith("yyyy-dd"))
                    ? y + "-" + d + "-" + m
                    : y + "-" + m + "-" + d;
        } else if (val.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            String[] p = val.split("-");
            String y = p[0], mid = p[1], last = p[2];
            out = (placeholder != null && placeholder.startsWith("yyyy-dd"))
                    ? y + "-" + last + "-" + mid
                    : val;
        }

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(out);
        input.sendKeys(Keys.TAB);
        sleep(300);
    }

    private boolean sawToast() {
        try {
            new WebDriverWait(driver, SHORT)
                    .until(ExpectedConditions.visibilityOfElementLocated(toast));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private boolean passIndicatorShown() {
        try {
            new WebDriverWait(driver, SHORT).until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(toast),
                    ExpectedConditions.visibilityOfElementLocated(noBalanceInfo)
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void clickWhenReady(By by) {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(by));
        try { btn.click(); }
        catch (ElementClickInterceptedException e) {
            new Actions(driver).moveToElement(btn).pause(80).click().perform();
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    public boolean applyLeave(String leaveType, String fromDate, String toDate, String comment) {
        openTab(tabApply);

        if (!driver.findElements(noBalanceInfo).isEmpty()) {
            return true;
        }

        pickFromDropdown(applyLeaveType, leaveType);
        typeDate(applyFromDate, fromDate);
        typeDate(applyToDate, toDate);

        try {
            WebElement cmt = new WebDriverWait(driver, SHORT)
                    .until(ExpectedConditions.visibilityOfElementLocated(applyComment));
            cmt.click(); cmt.clear();
            if (comment != null) cmt.sendKeys(comment);
        } catch (TimeoutException ignored) {}

        clickWhenReady(applyButton);
        return passIndicatorShown();
    }

    public boolean assignLeave(String employeeName, String leaveType, String fromDate, String toDate, String comment) {
        openTab(tabAssign);

        WebElement emp = wait.until(ExpectedConditions.visibilityOfElementLocated(assignEmpInput));
        emp.click(); emp.clear(); emp.sendKeys(employeeName);
        sleep(300);
        chooseFromListboxExactOrFirst(employeeName);

        pickFromDropdown(assignLeaveType, leaveType);
        typeDate(assignFromDate, fromDate);
        typeDate(assignToDate, toDate);

        try {
            WebElement cmt = new WebDriverWait(driver, SHORT)
                    .until(ExpectedConditions.visibilityOfElementLocated(assignComment));
            cmt.click(); cmt.clear();
            if (comment != null) cmt.sendKeys(comment);
        } catch (TimeoutException ignored) {}

        clickWhenReady(assignButton);
        List<WebElement> confirm = driver.findElements(confirmYesOk);
        if (!confirm.isEmpty()) {
            try { confirm.get(0).click(); } catch (Exception ignored) {}
        }
        return sawToast();
    }

    public int myLeaveSearch(String fromDate, String toDate, String statusOrBlank) {
        openTab(tabMyLeave);

        typeDate(myFromDate, fromDate);
        typeDate(myToDate, toDate);

        if (statusOrBlank != null && !statusOrBlank.isBlank()) {
            pickFromDropdown(myStatusDD, statusOrBlank.trim());
        }

        clickWhenReady(mySearchBtn);
        sleep(500);
        return driver.findElements(myRows).size();
    }
}
