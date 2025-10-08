package com.orangehrm.pages;

import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;


public class LoginPage extends BasePage {

    private static final Logger log = LoggerUtil.getLogger(LoginPage.class);

    @FindBy(xpath = "//input[@placeholder='Username']")
    private WebElement usernameField;

    @FindBy(xpath = "//input[@placeholder='Password']")
    private WebElement passwordField;

    @FindBy(xpath = "//button[@type='submit']")
    private WebElement loginButton;

    @FindBy(className = "oxd-alert-content-text")
    private WebElement errorMessage;

    @FindBy(xpath = "//span[normalize-space()='Required']")
    private WebElement requiredBadge;

    @FindBy(xpath = "//i[contains(@class,'oxd-userdropdown-icon')]")
    private WebElement userMenu;

    @FindBy(xpath = "//a[normalize-space()='Logout']")
    private WebElement logoutLink;

    public LoginPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }


    public void login(String username, String password) {
        log.info("Attempting login with Username: {}", username);
        type(usernameField, username);
        type(passwordField, password);
        click(loginButton);
    }


    public boolean isLoginPageVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(loginButton)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }


    public String getErrorText() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(errorMessage)).getText();
        } catch (Exception e) {
            return "";
        }
    }


    public boolean hasRequiredValidation() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(requiredBadge)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }


    public void logout() {
        try {
            log.info("Logging out...");
            click(userMenu);
            click(logoutLink);
            wait.until(ExpectedConditions.visibilityOf(loginButton));
            log.info("Successfully logged out.");
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
        }
    }
}
