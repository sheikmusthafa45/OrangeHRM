package com.orangehrm.data;

import org.testng.annotations.DataProvider;
import com.orangehrm.utils.ExcelReader;
import com.orangehrm.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import java.util.List;

public class TestDataProvider {

    private static final Logger log = LoggerUtil.getLogger(TestDataProvider.class);

    
    @DataProvider(name = "loginData")
    public Object[][] getLoginData() {
        log.info("Fetching test data from Excel sheet: Login");
        return ExcelReader.getTestData("Login");
    }

    @DataProvider(name = "loginInvalidUser")
    public Object[][] loginInvalidUser() {
        log.info("Providing static data for invalid username login test");
        return new Object[][] {
            {"InvalidUser", "admin123"}
        };
    }

    @DataProvider(name = "loginInvalidPassword")
    public Object[][] loginInvalidPassword() {
        log.info("Providing static data for invalid password login test");
        return new Object[][] {
            {"Admin", "wrongPwd"}
        };
    }

    
    @DataProvider(name = "dashboardData")
    public Object[][] getDashboardData() {
        log.info("Fetching test data from Excel sheet: Dashboard");
        return ExcelReader.getTestData("Dashboard");
    }

    @DataProvider(name = "dashboardMenus")
    public Object[][] getDashboardMenus() {
        log.info("Providing static data for dashboard menus");
        return new Object[][] {
            { List.of("Admin", "PIM", "Leave", "Time") }
        };
    }

    @DataProvider(name = "dashboardQuickLaunchTiles")
    public Object[][] getDashboardQuickLaunchTiles() {
        log.info("Providing static data for dashboard quick launch tiles");
        return new Object[][] {
            { List.of("Assign Leave", "Leave List", "Timesheets", "Apply Leave") }
        };
    }

    @DataProvider(name = "dashboardBaselineWidgets")
    public Object[][] getDashboardBaselineWidgets() {
        log.info("Providing static data for dashboard baseline widgets");
        return new Object[][] {
            {List.of("Quick Launch", "Time at Work", "My Actions", "Employee Distribution by Sub Unit") }
        };
    }

  
    @DataProvider(name = "pimData")
    public Object[][] getPimData() {
        log.info("Fetching test data from Excel sheet: PIM");
        return ExcelReader.getTestData("PIM");
    }

    
    @DataProvider(name = "adminData")
    public Object[][] getAdminData() {
        log.info("Fetching test data from Excel sheet: Admin");
        return ExcelReader.getTestData("Admin");
    }

   
    @DataProvider(name = "leaveData")
    public Object[][] getLeaveData() {
        log.info("Fetching test data from Excel sheet: Leave");
        return ExcelReader.getTestData("Leave");
    }

    @DataProvider(name = "leaveApplyData")
    public Object[][] getLeaveApplyData() {
        log.info("Fetching test data from Excel sheet: LeaveApply");
        return ExcelReader.getTestData("LeaveApply");
    }

    @DataProvider(name = "leaveAssignData")
    public Object[][] getLeaveAssignData() {
        log.info("Fetching test data from Excel sheet: LeaveAssign");
        return ExcelReader.getTestData("LeaveAssign");
    }

}
