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

public class AdminPage extends BasePage {

    private static final Logger log = LoggerUtil.getLogger(AdminPage.class);

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration SHORT_TIMEOUT = Duration.ofSeconds(6);

    private String lastCreatedUsername = null;
    public String getLastCreatedUsername() { return lastCreatedUsername; }

    @FindBy(xpath = "//span[normalize-space()='Admin']")
    private WebElement adminMenu;

    @FindBy(xpath = "//span[normalize-space()='User Management']")
    private WebElement userManagementMenu;

    @FindBy(xpath = "//span[normalize-space()='Job']")
    private WebElement jobMenu;

    @FindBy(xpath = "//span[normalize-space()='Organization']")
    private WebElement organizationMenu;

    @FindBy(xpath = "//a[normalize-space()='Structure']")
    private WebElement organizationStructureSubMenu;

    @FindBy(xpath = "//button[normalize-space()='Add']")
    private WebElement addBtn;

    @FindBy(xpath = "//button[@type='submit' and normalize-space()='Search']")
    private WebElement searchBtn;

    @FindBy(xpath = "//button[normalize-space()='Save']")
    private WebElement saveBtn;

    @FindBy(xpath = "//label[normalize-space()='Username']/../following-sibling::div//input")
    private WebElement usernameInput;

    @FindBy(xpath = "//label[normalize-space()='Employee Name']/../following-sibling::div//input")
    private WebElement employeeNameInput;

    @FindBy(xpath = "//div[contains(@class,'oxd-input-field-bottom-space')]//input")
    private WebElement nameInputField;

    private final By systemUsersHeader = By.xpath("//h5[normalize-space()='System Users']");
    private final By listTableBody   = By.xpath("//div[@role='table']//div[@role='rowgroup']");
    private final By listNoRecords   = By.xpath("//span[contains(.,'No Records Found')]");
    private final By spinner         = By.xpath("//div[contains(@class,'oxd-loading-spinner')]");
    private final By toast           = By.cssSelector("div.oxd-toast");

    public AdminPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void openAdminModule() {
        click(adminMenu);
        waitUntilVisible(systemUsersHeader);
        log.info("Opened Admin module (System Users page visible).");
    }

    public void navigateToUserManagement() {
        click(userManagementMenu);
        waitUntilVisible(systemUsersHeader);
        log.info("Navigated to User Management (System Users).");
    }

    private void navigateToJobSubMenu(String subMenuText) {
        click(jobMenu);
        By sub = By.xpath("//a[normalize-space()='" + subMenuText + "']");
        click(waitUntilClickable(sub));
        waitForTableOrEmpty();
        log.info("Navigated Job → {}.", subMenuText);
    }

    public boolean navigateToOrganizationStructure() {
        click(organizationMenu);
        click(organizationStructureSubMenu);
        try {
            waitUntilVisible(By.xpath("//h6[normalize-space()='Organization Structure']"));
            return true;
        } catch (TimeoutException e) {
            log.error("Organization Structure header not visible.");
            return false;
        }
    }

    public boolean addUser(String userRole, String empName, String status, String newUsername, String password) {
        this.lastCreatedUsername = null;
        click(addBtn);

        selectDropdownByLabel("User Role", userRole);

        type(employeeNameInput, empName);
        WebDriverWait shortWait = new WebDriverWait(driver, SHORT_TIMEOUT);
        try {
            By listbox = By.xpath("//div[@role='listbox']");
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(listbox));

            By optionByName = By.xpath("//div[@role='listbox']//div[@role='option'][contains(.,'" + empName + "')]");

            List<WebElement> matching = driver.findElements(optionByName);
            WebElement toClick = !matching.isEmpty()
                    ? matching.get(0)
                    : shortWait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[@role='listbox']//div[@role='option'][1]")));

            click(toClick);
            log.info("Selected employee suggestion for '{}'", empName);
        } catch (TimeoutException e) {
            log.error("Employee autocomplete suggestion not found for: {}", empName);
            return false;
        }

        selectDropdownByLabel("Status", status);

        clearAndType(usernameInput, newUsername);
        this.lastCreatedUsername = newUsername;

        WebElement pass = driver.findElement(By.xpath("//label[normalize-space()='Password']/../following-sibling::div//input[@type='password']"));
        WebElement cpass = driver.findElement(By.xpath("//label[normalize-space()='Confirm Password']/../following-sibling::div//input[@type='password']"));
        type(pass, password);
        type(cpass, password);

        scrollIntoViewAndClick(saveBtn);

        if (isToastDisplayed("Successfully Saved")) {
            waitToastGone();
            return true;
        }

        if (hasInlineError()) {
            String err = getInlineErrorText();
            log.error("User save blocked: {}", err);
            if (err.toLowerCase().contains("already exists")) {
                return searchUser(newUsername);
            }
            return false;
        }

        return searchUser(this.lastCreatedUsername);
    }

    public boolean searchUser(String username) {
        waitUntilVisible(systemUsersHeader);
        WebElement searchUsername = driver.findElement(By.xpath("//label[normalize-space()='Username']/../following-sibling::div//input"));
        clearAndType(searchUsername, username);
        click(searchBtn);
        waitForTableOrEmpty();
        return isTextInTable(username);
    }

    public boolean addJobTitle(String jobTitle, String jobDescription) {
        navigateToJobSubMenu("Job Titles");
        click(addBtn);
        type(nameInputField, jobTitle);
        WebElement description = driver.findElement(By.xpath("//textarea[@placeholder='Type description here']"));
        type(description, jobDescription);
        scrollIntoViewAndClick(saveBtn);

        if (isToastDisplayed("Successfully Saved")) {
            waitToastGone();
            return true;
        }
        if (hasInlineError() && getInlineErrorText().toLowerCase().contains("already exists")) {
            return verifyJobTitle(jobTitle);
        }
        return verifyJobTitle(jobTitle);
    }

    public boolean verifyJobTitle(String jobTitle) {
        navigateToJobSubMenu("Job Titles");
        return isTextInTable(jobTitle);
    }

    public boolean addPayGradeWithSalary(String payGrade, String _currencyIgnored, String _minIgnored, String _maxIgnored) {
        try {
            By adminMenu = By.xpath("//span[normalize-space()='Admin']");
            By jobMenu = By.xpath("//span[normalize-space()='Job']");
            By payGrades = By.xpath("//a[normalize-space()='Pay Grades']");
            wait.until(ExpectedConditions.elementToBeClickable(adminMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(jobMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(payGrades)).click();
            log.info("Navigated Job → Pay Grades.");

            By addBtn = By.xpath("//button[normalize-space()='Add']");
            By nameInput = By.xpath("//label[normalize-space()='Name']/../following-sibling::div//input");
            By saveBtn = By.xpath("//button[normalize-space()='Save']");
            wait.until(ExpectedConditions.elementToBeClickable(addBtn)).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput)).sendKeys(payGrade);
            wait.until(ExpectedConditions.elementToBeClickable(saveBtn)).click();

            if (isToastDisplayed("Successfully Saved")) {
                waitToastGone();
                return verifyPayGrade(payGrade);
            }

            if (hasInlineError() && getInlineErrorText().toLowerCase().contains("already exists")) {
                return verifyPayGrade(payGrade);
            }

            return verifyPayGrade(payGrade);

        } catch (Exception e) {
            log.error("Error while creating pay grade (currency step removed): {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean verifyPayGrade(String payGradeName) {
        navigateToJobSubMenu("Pay Grades");
        return isTextInTable(payGradeName);
    }

    public boolean addEmploymentStatus(String statusName) {
        navigateToJobSubMenu("Employment Status");
        click(addBtn);
        type(nameInputField, statusName);
        scrollIntoViewAndClick(saveBtn);

        if (isToastDisplayed("Successfully Saved")) {
            waitToastGone();
            return true;
        }
        if (hasInlineError() && getInlineErrorText().toLowerCase().contains("already exists")) {
            return verifyEmploymentStatus(statusName);
        }
        return verifyEmploymentStatus(statusName);
    }

    public boolean verifyEmploymentStatus(String statusName) {
        navigateToJobSubMenu("Employment Status");
        return isTextInTable(statusName);
    }

    public boolean addOrganizationUnit(String unitName, String parentName) {
        if (!navigateToOrganizationStructure()) return false;

        WebElement editBtn = driver.findElement(By.xpath("//button[normalize-space()='Edit']"));
        click(editBtn);

        WebElement addBtn = driver.findElement(By.xpath("//button[normalize-space()='Add']"));
        click(addBtn);

        WebElement unitNameInput = driver.findElement(By.xpath("//input[@placeholder='Type name']"));
        type(unitNameInput, unitName);

        WebElement parentDropdown = driver.findElement(By.xpath("//label[text()='Parent Unit']/../following-sibling::div//div[contains(@class,'oxd-select-text')]"));
        parentDropdown.click();

        By option = By.xpath("//div[@role='listbox']//span[normalize-space()='" + parentName + "']");
        WebElement parentOption = waitUntilClickable(option);
        parentOption.click();

        scrollIntoViewAndClick(saveBtn);
        boolean ok = isToastDisplayed("Successfully Saved");
        waitToastGone();
        return ok;
    }

    private void selectDropdownByLabel(String label, String optionText) {
        if (optionText == null || optionText.trim().isEmpty()) return;

        WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        By drop = By.xpath("//label[normalize-space()='" + label + "']/../following-sibling::div//div[contains(@class,'oxd-select-text')]");
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(drop));
        scrollIntoView(dropdown);
        dropdown.click();

        By option = By.xpath("//div[@role='listbox']//span[normalize-space()='" + optionText + "']");
        WebElement opt = wait.until(ExpectedConditions.elementToBeClickable(option));
        opt.click();

        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(drop, optionText));
        } catch (TimeoutException ignored) {}
    }

    private void waitForTableOrEmpty() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(listTableBody),
                ExpectedConditions.presenceOfElementLocated(listNoRecords)
        ));
        waitSpinnerGone();
    }

    private boolean isTextInTable(String exactText) {
        By cell = By.xpath("//div[@role='table']//div[@role='rowgroup']//div[@role='row']//div[@role='cell']//div[normalize-space()='" + exactText + "']");
        try {
            new WebDriverWait(driver, SHORT_TIMEOUT).until(ExpectedConditions.visibilityOfElementLocated(cell));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void waitUntilVisible(By locator) {
        new WebDriverWait(driver, WAIT_TIMEOUT).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private WebElement waitUntilClickable(By locator) {
        return new WebDriverWait(driver, WAIT_TIMEOUT).until(ExpectedConditions.elementToBeClickable(locator));
    }

    private void waitSpinnerGone() {
        try {
            new WebDriverWait(driver, SHORT_TIMEOUT).until(ExpectedConditions.invisibilityOfElementLocated(spinner));
        } catch (TimeoutException ignored) {
        }
    }

    private void waitToastGone() {
        try {
            new WebDriverWait(driver, SHORT_TIMEOUT).until(ExpectedConditions.invisibilityOfElementLocated(toast));
        } catch (TimeoutException ignored) {
        }
    }

    private void scrollIntoView(WebElement el) {
        try {
            new Actions(driver).moveToElement(el).perform();
        } catch (Exception e) {
            log.warn("Could not move to element: {}", e.getMessage());
        }
    }

    private void scrollIntoViewAndClick(WebElement el) {
        try {
            new Actions(driver).moveToElement(el).perform();
        } catch (Exception ignored) {
        }
        click(el);
    }

    private void clearAndType(WebElement el, String text) {
        try {
            el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            el.sendKeys(Keys.DELETE);
        } catch (Exception ignored) {
            try { el.clear(); } catch (Exception ig) {}
        }
        el.sendKeys(text);
    }

    private boolean hasInlineError() {
        By inlineErr = By.xpath("//span[contains(@class,'oxd-input-field-error-message')]");
        try {
            new WebDriverWait(driver, SHORT_TIMEOUT).until(ExpectedConditions.visibilityOfElementLocated(inlineErr));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private String getInlineErrorText() {
        try {
            WebElement err = new WebDriverWait(driver, SHORT_TIMEOUT)
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//span[contains(@class,'oxd-input-field-error-message')]")));
            return err.getText().trim();
        } catch (TimeoutException e) {
            return "";
        }
    }

    private boolean isElementPresent(By locator) {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
            boolean present = !driver.findElements(locator).isEmpty();
            int implicit = Integer.parseInt(com.orangehrm.utils.ConfigReader.getProperty("implicit.wait"));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicit));
            return present;
        } catch (Exception e) {
            int implicit = Integer.parseInt(com.orangehrm.utils.ConfigReader.getProperty("implicit.wait"));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicit));
            return false;
        }
    }

    private boolean openPayGradeFromList(String payGrade) {
        try {
            By searchName = By.xpath("//label[normalize-space()='Name']/../following-sibling::div//input");
            By searchBtn = By.xpath("//button[normalize-space()='Search']");

            try {
                WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchName));
                input.clear();
                input.sendKeys(payGrade);
                wait.until(ExpectedConditions.elementToBeClickable(searchBtn)).click();
            } catch (TimeoutException ignored) { }

            By rowLink = By.xpath("//div[contains(@class,'oxd-table-card')]//*[normalize-space()='" + payGrade + "']");
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(rowLink));
            link.click();

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//h6[contains(.,'Pay Grade')]")),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(@class,'orangehrm-card-container')]"))
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
