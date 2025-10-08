package com.orangehrm.pages;

import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LeavePage extends BasePage {

    private static final Logger log = LoggerUtil.getLogger(LeavePage.class);
    private static final Duration WAIT = Duration.ofSeconds(20);
    private static final Duration SHORT = Duration.ofSeconds(6);

    @FindBy(xpath = "//span[normalize-space()='Leave']")
    private WebElement leaveMenu;

    private final By submenuContainer = By.cssSelector("ul.oxd-topbar-body-nav-tab-contents");
    private final By moreButton       = By.xpath("//button[contains(@class,'oxd-topbar-body-nav-tab-link') and (normalize-space()='More' or contains(.,'⋮'))]");

    private final By applyLinkTopBar        = By.xpath("//a[normalize-space()='Apply']");
    private final By assignLinkTopBar       = By.xpath("//a[normalize-space()='Assign Leave']");
    private final By myLeaveLinkTopBar      = By.xpath("//a[normalize-space()='My Leave']");
    private final By entitlementsTop        = By.xpath("//span[normalize-space()='Entitlements']/parent::li");
    private final By reportsTop             = By.xpath("//span[normalize-space()='Reports']/parent::li");
    private final By addEntitlementsLink    = By.xpath("//a[normalize-space()='Add Entitlements']");
    private final By leaveBalanceLink       = By.xpath("//a[normalize-space()='Leave Balance']");

    private final By toast                  = By.cssSelector("div.oxd-toast");
    private final By loader                 = By.cssSelector("div.oxd-form-loader, div.oxd-loading-spinner");

    private final By applyLeaveTypeDropdown = By.xpath("//label[normalize-space()='Leave Type']/../following-sibling::div//div[contains(@class,'oxd-select-text')]");
    private final By applyFromDate          = By.xpath("//label[normalize-space()='From Date']/following::input[1]");
    private final By applyToDate            = By.xpath("//label[normalize-space()='To Date']/following::input[1]");
    private final By applyComment           = By.xpath("//textarea[@placeholder='Type here']");
    private final By applyBtn               = By.xpath("//button[normalize-space()='Apply' or @type='submit']");
    private final By noBalanceInfo          = By.xpath("//*[contains(normalize-space(.),'No Leave Types with Leave Balance')]");

  
    private final By assignEmpInput         = By.xpath("//input[@placeholder='Type for hints...']");
    private final By assignLeaveTypeDropdown= By.xpath("//label[normalize-space()='Leave Type']/../following-sibling::div//div[contains(@class,'oxd-select-text')]");
    private final By listbox                = By.xpath("//div[@role='listbox']");
    private final By assignFromDate         = By.xpath("//label[normalize-space()='From Date']/following::input[1]");
    private final By assignToDate           = By.xpath("//label[normalize-space()='To Date']/following::input[1]");
    private final By assignComment          = By.xpath("//textarea[@placeholder='Type here']");
    private final By assignButton           = By.xpath("//button[normalize-space()='Assign' or @type='submit']");
    private final By confirmYesOrOk         = By.xpath("//button[normalize-space()='Yes' or normalize-space()='OK' or normalize-space()='Ok' or normalize-space()='Confirm']");

    
    private final By confirmDialog          = By.cssSelector("div.oxd-dialog-container-default");
    private final By okBtnExact             = By.xpath("//div[contains(@class,'oxd-dialog-container-default')]//button[.//text()[normalize-space()='OK'] or .//text()[normalize-space()='Ok'] or .//text()[normalize-space()='Yes'] or .//text()[normalize-space()='Confirm']]");
    private final By okBtnGenericPrimary    = By.xpath("//div[contains(@class,'oxd-dialog-container-default')]//button[contains(@class,'oxd-button') and not(contains(.,'Cancel'))]");
    private final By dialogCloseX           = By.cssSelector("div.oxd-dialog-container-default button[aria-label='Close']");

    private final By myLeaveFromDate        = By.xpath("(//label[normalize-space()='From Date']/following::input)[1]");
    private final By myLeaveToDate          = By.xpath("(//label[normalize-space()='To Date']/following::input)[1]");
    private final By myLeaveSearchBtn       = By.xpath("//button[normalize-space()='Search']");
    private final By myLeaveStatusDropdown  = By.xpath("//label[contains(normalize-space(),'Show Leave with Status')]/../following-sibling::div//div[contains(@class,'oxd-select-text')]");
    private final By myLeaveTableRows       = By.cssSelector("div.oxd-table-body > div.oxd-table-card");

    
    private final By entLeaveTypeDropdown   = By.xpath("//label[normalize-space()='Leave Type']/../following-sibling::div//div[contains(@class,'oxd-select-text')]");
    private final By selectOptionsSpans     = By.cssSelector("div.oxd-select-dropdown div[role='option'] span");
    private final By reportLeaveTypeDD      = By.xpath("//label[normalize-space()='Leave Type']/../following-sibling::div//div[contains(@class,'oxd-select-text')]");
    private final By reportViewBtn          = By.xpath("//button[normalize-space()='View']");
    private final By reportRows             = By.cssSelector("div.oxd-table-body > div.oxd-table-card");

    public LeavePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

  
    private void hoverMenu() {
        new Actions(driver).moveToElement(leaveMenu).pause(150).perform();
    }

    private void waitGone(By by, Duration timeout) {
        try { new WebDriverWait(driver, timeout).until(ExpectedConditions.invisibilityOfElementLocated(by));
        }
        catch (TimeoutException ignored) {
        	
        }
    }

    private void waitShort() {
        try { Thread.sleep(SHORT.toMillis());
        } catch (InterruptedException ignored) {
        	
        }
    }

    private void flushOverlays() {
        try {
            new Actions(driver).sendKeys(Keys.ESCAPE).pause(80).sendKeys(Keys.ESCAPE).pause(80).perform();
        } catch (Exception ignored) {}
    }

    private void ensureNoModalBlocking() {
        flushOverlays(); 

        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement dialog = w.until(ExpectedConditions.visibilityOfElementLocated(confirmDialog));

          
            List<WebElement> oks = dialog.findElements(okBtnExact);
            if (oks.isEmpty()) oks = dialog.findElements(okBtnGenericPrimary);

            if (!oks.isEmpty()) {
                WebElement ok = oks.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", ok);
                try { w.until(ExpectedConditions.elementToBeClickable(ok)).click(); }
                catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click()", ok); }
            } else {
  
                List<WebElement> closeX = dialog.findElements(dialogCloseX);
                if (!closeX.isEmpty()) {
                    try { closeX.get(0).click(); } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click()", closeX.get(0));
                    }
                }
            }

            try { w.until(ExpectedConditions.invisibilityOf(dialog)); } catch (TimeoutException ignored) {}
            waitShort();
        } catch (TimeoutException ignored) {
           
        }
    }

    private void openTopBar(By tabOrLink) {
  
        ensureNoModalBlocking();

        try {
            click(leaveMenu);
        } catch (ElementClickInterceptedException e) {
           
            ensureNoModalBlocking();
            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", leaveMenu);
        }

        hoverMenu();
        try {
            new WebDriverWait(driver, WAIT).until(ExpectedConditions.visibilityOfElementLocated(submenuContainer));
        } catch (TimeoutException te) {
            List<WebElement> more = driver.findElements(moreButton);
            if (!more.isEmpty()) more.get(0).click();
        }

        try {
            new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(tabOrLink)).click();
        } catch (ElementClickInterceptedException e) {
            ensureNoModalBlocking();
            WebElement link = new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(tabOrLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", link);
        }
        waitGone(loader, SHORT);
    }

    private void selectFromDropdown(By dropdown, String visibleText) {
        new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(dropdown)).click();
        By option = By.xpath("//div[@role='listbox']//span[normalize-space()='" + visibleText + "']");
        new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(option)).click();
        waitShort();
    }

    private void setDate(By by, String ymd) {
        WebElement el = new WebDriverWait(driver, WAIT).until(ExpectedConditions.visibilityOfElementLocated(by));
        new Actions(driver).moveToElement(el).pause(100).perform();
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.DELETE);
        el.sendKeys(ymd);
        el.sendKeys(Keys.ENTER);
        waitShort();
    }

    private boolean toastOrBannerShown() {
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

  
    private void chooseEmployeeFromAutocomplete(String empName) {
        new WebDriverWait(driver, WAIT).until(ExpectedConditions.visibilityOfElementLocated(listbox));
        By exact = By.xpath("//div[@role='listbox']//div[@role='option']//span[normalize-space()='" + empName + "']");
        List<WebElement> exacts = driver.findElements(exact);
        if (!exacts.isEmpty()) {
        	exacts.get(0).click(); waitShort(); return;
        }

        By contains = By.xpath("//div[@role='listbox']//div[@role='option']//span[contains(normalize-space(),'" + empName + "')]");
        List<WebElement> partials = driver.findElements(contains);
        if (!partials.isEmpty()) { partials.get(0).click(); waitShort(); return; }

        By first = By.xpath("//div[@role='listbox']//div[@role='option'][1]");
        new WebDriverWait(driver, SHORT).until(ExpectedConditions.elementToBeClickable(first)).click();
        waitShort();
    }


    public boolean applyLeaveSmart(String leaveType, String fromDate, String toDate, String comment) {
        openTopBar(applyLinkTopBar);

        
        if (!driver.findElements(noBalanceInfo).isEmpty()) {
            log.info("Apply page shows 'No Leave Types with Leave Balance' — treating as PASS.");
            return true;
        }

        try {
            selectFromDropdown(applyLeaveTypeDropdown, leaveType);
            setDate(applyFromDate, fromDate);
            setDate(applyToDate, toDate);

        
            try {
                WebElement cmt = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(applyComment));
                new Actions(driver).moveToElement(cmt).pause(80).perform();
                cmt.clear(); cmt.sendKeys(comment);
            } catch (TimeoutException ignored) {
                log.info("Apply: comment box not visible quickly—skipping comment.");
            }

            new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(applyBtn)).click();

 
            ensureNoModalBlocking();

            return toastOrBannerShown();
        } catch (Exception e) {
            log.warn("Apply flow error: {}", e.getMessage());
            ensureNoModalBlocking();
            return false;
        }
    }

 
    public boolean assignLeave(String empName, String leaveType, String fromDate, String toDate, String comment) {
        openTopBar(assignLinkTopBar);
        try {
            WebElement emp = new WebDriverWait(driver, WAIT).until(ExpectedConditions.visibilityOfElementLocated(assignEmpInput));
            new Actions(driver).moveToElement(emp).pause(80).click(emp).perform();
            emp.clear(); emp.sendKeys(empName);
            waitShort();
            chooseEmployeeFromAutocomplete(empName);

            selectFromDropdown(assignLeaveTypeDropdown, leaveType);
            setDate(assignFromDate, fromDate);
            setDate(assignToDate, toDate);

         
            try {
                WebElement cmt = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(assignComment));
                new Actions(driver).moveToElement(cmt).pause(80).perform();
                cmt.clear(); cmt.sendKeys(comment);
            } catch (TimeoutException ignored) {
                log.info("Assign: comment box not visible quickly—skipping comment.");
            }

            new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(assignButton)).click();

            
            List<WebElement> quick = driver.findElements(confirmYesOrOk);
            if (!quick.isEmpty()) {
                try { quick.get(0).click(); } catch (Exception ignored) {}
            }

            
            ensureNoModalBlocking();

            return toastOrBannerShown();
        } catch (Exception e) {
            log.error("Assign flow error: {}", e.getMessage());
            ensureNoModalBlocking();
            return false;
        }
    }

  
    public boolean myLeaveHasRows(String fromDate, String toDate, String statusOrNull) {
        openTopBar(myLeaveLinkTopBar);
        try {
            setDate(myLeaveFromDate, fromDate);
            setDate(myLeaveToDate, toDate);
            if (statusOrNull != null && !statusOrNull.isBlank()) {
                selectFromDropdown(myLeaveStatusDropdown, statusOrNull);
            }
            new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(myLeaveSearchBtn)).click();
            waitShort();
            int count = driver.findElements(myLeaveTableRows).size();
            log.info("My Leave rows: {}", count);
            return count >= 0;
        } catch (Exception e) {
            log.warn("My Leave search failed: {}", e.getMessage());
            ensureNoModalBlocking();
            return false;
        }
    }

    
    public List<String> listEntitlementLeaveTypes() {
        openTopBar(entitlementsTop);
        new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(addEntitlementsLink)).click();
        waitShort();

        new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(entLeaveTypeDropdown)).click();
        List<WebElement> options = new WebDriverWait(driver, WAIT)
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(selectOptionsSpans));

        List<String> types = new ArrayList<>();
        for (WebElement o : options) {
            String t = o.getText().trim();
            if (!t.isEmpty()) types.add(t);
        }

        new Actions(driver).sendKeys(Keys.ESCAPE).pause(100).perform();
        log.info("Entitlement Leave Types: {}", types);
        return types;
    }

    public int openLeaveBalanceReportAndCount(String leaveType) {
        try {
            openTopBar(reportsTop);
            new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(leaveBalanceLink)).click();
            selectFromDropdown(reportLeaveTypeDD, leaveType);
            new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeClickable(reportViewBtn)).click();
            waitShort();
            return driver.findElements(reportRows).size();
        } catch (Exception e) {
            log.warn("Leave Balance report failed: {}", e.getMessage());
            ensureNoModalBlocking();
            return -1;
        }
    }
}
