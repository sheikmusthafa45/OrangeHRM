package com.orangehrm.pages;

import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PIMPage extends BasePage {

    private static final Logger log = LoggerUtil.getLogger(PIMPage.class);
    private static final Duration WAIT = Duration.ofSeconds(20);
    private static final Duration SHORT = Duration.ofSeconds(6);

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//span[normalize-space()='PIM']")
    private WebElement pimMenu;

    @FindBy(xpath = "//a[normalize-space()='Employee List']")
    private WebElement employeeListTab;

    @FindBy(xpath = "//button[normalize-space()='Add']")
    private WebElement addEmployeeBtn;

    @FindBy(xpath = "//input[@placeholder='First Name']")
    private WebElement firstNameField;

    @FindBy(xpath = "//input[@placeholder='Middle Name']")
    private WebElement middleNameField;

    @FindBy(xpath = "//input[@placeholder='Last Name']")
    private WebElement lastNameField;

    @FindBy(xpath = "//label[normalize-space()='Employee Id']/../following-sibling::div//input")
    private WebElement employeeIdField;

    @FindBy(xpath = "//button[normalize-space()='Save']")
    private WebElement saveButton;

    @FindBy(xpath = "//span[contains(@class,'oxd-switch-input')]")
    private WebElement createLoginToggle;

    @FindBy(xpath = "//label[normalize-space()='Username']/../following-sibling::div//input")
    private WebElement loginUsernameField;

    @FindBy(xpath = "//label[normalize-space()='Password']/../following-sibling::div//input")
    private WebElement loginPasswordField;

    @FindBy(xpath = "//label[normalize-space()='Confirm Password']/../following-sibling::div//input")
    private WebElement loginConfirmPasswordField;

    @FindBy(xpath = "//label[normalize-space()='Employee Id']/../following-sibling::div//input")
    private WebElement employeeIdSearch;

    @FindBy(xpath = "//button[normalize-space()='Search']")
    private WebElement searchButton;

    private final By successToast = By.xpath("//p[contains(@class,'oxd-text--toast-title')][normalize-space()='Success']");
    private final By tableCards = By.cssSelector("div.oxd-table-body div.oxd-table-card");
    private final By tableRowContainer = By.cssSelector("div[role='table'] div[role='rowgroup']");
    private final By noRecords = By.xpath("//span[contains(.,'No Records Found')]");
    private final By personalDetailsHeader = By.xpath("//h6[normalize-space()='Personal Details']");

    public PIMPage(WebDriver driver) {
        super(driver);
        this.driver = driver;
        this.wait = new WebDriverWait(driver, WAIT);
        PageFactory.initElements(driver, this);
    }

    public void openPIM() {
        log.info("Opening PIM module");
        click(pimMenu);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOf(addEmployeeBtn),
                ExpectedConditions.visibilityOf(employeeListTab)
        ));
    }

 
    public void goToEmployeeList() {
        log.info("Navigating to Employee List");
        By empListTab = By.xpath("//a[normalize-space()='Employee List']");
        By searchBtn = By.xpath("//button[normalize-space()='Search']");
        wait.until(ExpectedConditions.elementToBeClickable(empListTab)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchBtn));
    }

    public void clickAddEmployee() {
        log.info("Clicking Add Employee");
        click(addEmployeeBtn);
        wait.until(ExpectedConditions.visibilityOf(firstNameField));
    }

    public boolean addEmployeeWithLogin(String first, String middle, String last,
                                        String userName, String password, String employeeId) {
        clickAddEmployee();

        clearAndType(firstNameField, first);
        if (middle != null && !middle.isEmpty()) {
            clearAndType(middleNameField, middle);
        }
        clearAndType(lastNameField, last);

        setEmployeeIdIfEditable(employeeId);

        click(createLoginToggle);
        wait.until(ExpectedConditions.visibilityOf(loginUsernameField));
        clearAndType(loginUsernameField, userName);
        clearAndType(loginPasswordField, password);
        clearAndType(loginConfirmPasswordField, password);

        click(saveButton);
        return waitForSaveSuccess();
    }

    public void searchEmployeeById(String employeeId) {
        goToEmployeeList();
        clearAndType(employeeIdSearch, employeeId);
        try { Thread.sleep(250); } catch (InterruptedException ignored) {}
        click(searchButton);
        waitForEmployeeListResults();
    }

    public boolean isEmployeeListVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(tableRowContainer));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isEmployeeFoundInTable(String expected) {
        if (isPresent(noRecords)) {
            log.info("No Records Found visible.");
            return false;
        }
        List<WebElement> rows = driver.findElements(tableCards);
        for (WebElement row : rows) {
            String rowText = row.getText();
            if (rowText != null && rowText.toLowerCase().contains(expected.toLowerCase())) {
                log.info("Found employee '{}' in table.", expected);
                return true;
            }
        }
        log.info("Employee '{}' not found in table.", expected);
        return false;
    }

    public boolean isElementVisible(String labelText) {
        try {
            By locator = By.xpath("//label[normalize-space()='" + labelText + "']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public List<String> getVisibleButtonLabels() {
        List<String> labels = new ArrayList<>();
        List<WebElement> buttons = driver.findElements(By.xpath("//button"));
        for (WebElement btn : buttons) {
            if (btn.isDisplayed()) {
                String text = btn.getText().trim();
                if (!text.isEmpty()) {
                    labels.add(text);
                }
            }
        }
        return labels;
    }

    public List<String> getTableHeaders() {
        List<String> headers = new ArrayList<>();
        List<WebElement> headerEls = driver.findElements(By.cssSelector(".oxd-table-header-cell"));
        for (WebElement el : headerEls) {
            String text = el.getText().trim();
            if (!text.isEmpty()) {
                headers.add(text);
            }
        }
        return headers;
    }

    public int getVisibleEmployeeCount() {
        List<WebElement> rows = driver.findElements(tableCards);
        return rows.size();
    }

    private boolean waitForSaveSuccess() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            log.info("Employee saved (toast visible)");
            return true;
        } catch (TimeoutException te) {
            try {
                new WebDriverWait(driver, SHORT).until(ExpectedConditions.visibilityOfElementLocated(personalDetailsHeader));
                log.info("Employee saved (Personal Details page visible)");
                return true;
            } catch (TimeoutException te2) {
                log.error("Save did not show success indicators.");
                return false;
            }
        }
    }

    private void waitForEmployeeListToLoad() {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(tableRowContainer),
                ExpectedConditions.presenceOfElementLocated(noRecords)
        ));
    }

    private void waitForEmployeeListResults() {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(tableCards),
                ExpectedConditions.presenceOfElementLocated(noRecords)
        ));
    }

    private void setEmployeeIdIfEditable(String employeeId) {
        if (employeeId == null || employeeId.isEmpty()) return;
        try {
            wait.until(ExpectedConditions.visibilityOf(employeeIdField));
            employeeIdField.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            employeeIdField.sendKeys(Keys.DELETE);
            type(employeeIdField, employeeId);
        } catch (Exception e) {
            log.warn("Could not set Employee Id (field may be readonly or delayed): {}", e.getMessage());
        }
    }

    private void clearAndType(WebElement el, String value) {
        wait.until(ExpectedConditions.visibilityOf(el));
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.DELETE);
        type(el, value);
    }

    private boolean isPresent(By locator) {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
            boolean present = !driver.findElements(locator).isEmpty();
            driver.manage().timeouts().implicitlyWait(SHORT);
            return present;
        } catch (Exception e) {
            driver.manage().timeouts().implicitlyWait(SHORT);
            return false;
        }
    }
}
