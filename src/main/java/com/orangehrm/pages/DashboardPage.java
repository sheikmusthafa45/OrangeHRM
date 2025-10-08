package com.orangehrm.pages;

import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;


public class DashboardPage extends BasePage {

    private static final Logger log = LoggerUtil.getLogger(DashboardPage.class);

    @FindBy(xpath = "//h6[normalize-space()='Dashboard']")
    private WebElement dashboardHeader;

    @FindBy(xpath = "//p[normalize-space()='Quick Launch']")
    private WebElement quickLaunchTitle;

    @FindBy(xpath = "//p[normalize-space()='Pending Leave Requests']")
    private WebElement pendingLeaveTitle;

    public DashboardPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public boolean isDashboardDisplayed() {
        try {
            boolean visible = wait.until(ExpectedConditions.visibilityOf(dashboardHeader)).isDisplayed();
            log.info("Dashboard header visible: {}", visible);
            return visible;
        } catch (Exception e) {
            log.error("Dashboard not displayed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isMenuVisible(String menuText) {
        try {
            By menu = By.xpath("//span[normalize-space()='" + menuText + "']");
            boolean visible = wait.until(ExpectedConditions.visibilityOfElementLocated(menu)).isDisplayed();
            log.info("Menu '{}' visible: {}", menuText, visible);
            return visible;
        } catch (Exception e) {
            log.error("Menu '{}' not visible: {}", menuText, e.getMessage());
            return false;
        }
    }

    public boolean isWidgetTitleVisible(String titleText) {
        try {
            By t = By.xpath("//p[normalize-space()='" + titleText + "']");
            boolean visible = wait.until(ExpectedConditions.visibilityOfElementLocated(t)).isDisplayed();
            log.info("Widget '{}' visible: {}", titleText, visible);
            return visible;
        } catch (Exception e) {
            log.error("Widget '{}' not visible: {}", titleText, e.getMessage());
            return false;
        }
    }

    public boolean isQuickLaunchVisible() {
        try {
            boolean visible = wait.until(ExpectedConditions.visibilityOf(quickLaunchTitle)).isDisplayed();
            log.info("Quick Launch widget visible: {}", visible);
            return visible;
        } catch (Exception e) {
            log.error("Quick Launch not visible: {}", e.getMessage());
            return false;
        }
    }

    public boolean isQuickLaunchTileVisible(String tileText) {
        try {
            By widget = By.xpath("//p[normalize-space()='Quick Launch']/ancestor::div[contains(@class,'orangehrm-dashboard-widget')]");
            WebElement container = wait.until(ExpectedConditions.visibilityOfElementLocated(widget));

            By tile = By.xpath(".//*[self::span or self::p or self::a or self::button or self::div][normalize-space()='" + tileText + "']");
            for (WebElement el : container.findElements(tile)) {
                if (el.isDisplayed()) {
                    log.info("Quick Launch tile '{}' is visible.", tileText);
                    return true;
                }
            }
            log.warn("Quick Launch tile '{}' not visible.", tileText);
            return false;
        } catch (Exception e) {
            log.error("Error verifying Quick Launch tile '{}': {}", tileText, e.getMessage());
            return false;
        }
    }

    public boolean isEmployeeDistributionTitleVisible() {
        try {
            By title = By.xpath("//p[contains(.,'Employee Distribution')]");
            boolean visible = wait.until(ExpectedConditions.visibilityOfElementLocated(title)).isDisplayed();
            log.info("Employee Distribution title visible: {}", visible);
            return visible;
        } catch (Exception e) {
            log.error("Employee Distribution title missing: {}", e.getMessage());
            return false;
        }
    }

    public boolean isEmployeeDistributionChartVisible() {
        try {
            By chart = By.xpath("//p[contains(.,'Employee Distribution')]" +
                    "/ancestor::div[contains(@class,'orangehrm-dashboard-widget')]" +
                    "//*[self::canvas or self::div[contains(@class,'chart')]]");
            boolean visible = wait.until(ExpectedConditions.visibilityOfElementLocated(chart)).isDisplayed();
            log.info("Employee Distribution chart visible: {}", visible);
            return visible;
        } catch (Exception e) {
            log.error("Employee Distribution chart not visible: {}", e.getMessage());
            return false;
        }
    }

    public boolean hasPendingLeaveWidget() {
        try {
            By title = By.xpath("//p[normalize-space()='Pending Leave Requests']");
            wait.until(ExpectedConditions.presenceOfElementLocated(title));
            log.info("Pending Leave widget present.");
            return true;
        } catch (Exception e) {
            log.warn("Pending Leave widget not present: {}", e.getMessage());
            return false;
        }
    }

    public boolean isPendingLeaveTitleVisible() {
        try {
            boolean visible = wait.until(ExpectedConditions.visibilityOf(pendingLeaveTitle)).isDisplayed();
            log.info("Pending Leave title visible: {}", visible);
            return visible;
        } catch (Exception e) {
            log.error("Pending Leave title not visible: {}", e.getMessage());
            return false;
        }
    }
}
