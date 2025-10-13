package com.orangehrm.data;

import org.testng.SkipException;
import org.testng.annotations.DataProvider;

import com.orangehrm.utils.ExcelReader;
import com.orangehrm.utils.LoggerUtil;
import com.orangehrm.utils.ConfigReader;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestDataProvider {

    private static final Logger log = LoggerUtil.getLogger(TestDataProvider.class);

    

    private static Object[][] pad(Object[][] src, int minCols) {
        if (src == null) return new Object[][]{};
        Object[][] out = new Object[src.length][minCols];
        for (int i = 0; i < src.length; i++) {
            for (int j = 0; j < minCols; j++) {
                out[i][j] = (src[i] != null && j < src[i].length && src[i][j] != null) ? src[i][j] : "";
            }
        }
        return out;
    }

    private static Object[][] firstNonEmpty(Object[][]... candidates) {
        for (Object[][] c : candidates) {
            if (c != null && c.length > 0) return c;
        }
        return new Object[][]{};
    }



    private static List<String> splitPipeFromConfig(String key) {
        String raw = ConfigReader.getProperty(key);
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

  

    private static List<String> readKeyListFromDashboardSheet(String key) {
        Object[][] rows = ExcelReader.getTestData("Dashboard");
        if (rows == null || rows.length == 0) {
            return List.of(); 
        }

        String found = null;
        for (Object[] r : rows) {
            if (r == null || r.length == 0) continue;
            String k = String.valueOf(r[0]).trim();
            if (k.equalsIgnoreCase("Key")) continue; // header
            if (k.equalsIgnoreCase(key)) {
                if (r.length < 2) break;
                found = String.valueOf(r[1]).trim();
                break;
            }
        }

        if (found == null || found.isBlank()) return List.of();

        List<String> out = new ArrayList<>();
        for (String part : found.split("\\|")) {
            String v = part.trim();
            if (!v.isEmpty()) out.add(v);
        }
        return out;
    }

  
    private static List<String> dashboardList(String key, List<String> hardcodedFallback) {
       
        List<String> fromExcel = readKeyListFromDashboardSheet(key);
        if (!fromExcel.isEmpty()) {
            log.info("Dashboard[" + key + "] from Excel -> {}", fromExcel);
            return fromExcel;
        }

      
        List<String> fromConfig = splitPipeFromConfig(key);
        if (!fromConfig.isEmpty()) {
            log.warn("Dashboard[" + key + "] from config.properties -> {}", fromConfig);
            return fromConfig;
        }

       
        log.warn("Dashboard[" + key + "] not found in Excel/config. Using fallback -> {}", hardcodedFallback);
        return hardcodedFallback;
    }

  

    @DataProvider(name = "loginData", parallel = true)
    public Object[][] loginData() {
        log.info("Reading Excel sheet: Login");
        return ExcelReader.getTestData("Login");
    }

    @DataProvider(name = "loginInvalidUser", parallel = true)
    public Object[][] loginInvalidUser() {
        log.info("Static data: invalid username login");
        return new Object[][]{{"InvalidUser", "admin123"}};
    }

    @DataProvider(name = "loginInvalidPassword", parallel = true)
    public Object[][] loginInvalidPassword() {
        log.info("Static data: invalid password login");
        return new Object[][]{{"Admin", "wrongPwd"}};
    }

   

    @DataProvider(name = "dashboardMenus", parallel = true)
    public Object[][] dashboardMenus() {
        List<String> fallback = Arrays.asList("Admin", "PIM", "Leave", "Time");
        List<String> menus = dashboardList("menus", fallback);
        if (menus.isEmpty()) throw new SkipException("No menus found for Dashboard tests.");
        log.info("Dashboard menus -> {}", menus);
        return new Object[][]{{menus}};
    }

    @DataProvider(name = "dashboardQuickLaunchTiles", parallel = true)
    public Object[][] dashboardQuickLaunchTiles() {
        List<String> fallback = Arrays.asList("Assign Leave", "Leave List", "Timesheets", "Apply Leave");
        List<String> tiles = dashboardList("quickLaunchTiles", fallback);
        if (tiles.isEmpty()) log.warn("No Quick Launch tiles configured.");
        log.info("Quick Launch tiles -> {}", tiles);
        return new Object[][]{{tiles}};
    }

    @DataProvider(name = "dashboardBaselineWidgets", parallel = true)
    public Object[][] dashboardBaselineWidgets() {
        List<String> fallback = Arrays.asList("Quick Launch", "Time at Work", "My Actions", "Employee Distribution by Sub Unit");
        List<String> widgets = dashboardList("baselineWidgets", fallback);
        if (widgets.isEmpty()) throw new SkipException("No baseline widgets found for Dashboard tests.");
        log.info("Baseline widgets -> {}", widgets);
        return new Object[][]{{widgets}};
    }

    

    @DataProvider(name = "pimData", parallel = true)
    public Object[][] pimData() {
        log.info("Reading Excel sheet: PIM");
        return ExcelReader.getTestData("PIM");
    }

    @DataProvider(name = "pimDataBundle", parallel = false)
    public Object[][] pimDataBundle() {
        log.info("Bundling all PIM rows as a single argument");
        return new Object[][]{{ExcelReader.getTestData("PIM")}};
    }

  

    @DataProvider(name = "adminData", parallel = true)
    public Object[][] adminData() {
        log.info("Reading Excel sheet: Admin");
        return ExcelReader.getTestData("Admin");
    }

    @DataProvider(name = "adminDataBundle", parallel = false)
    public Object[][] adminDataBundle() {
        log.info("Bundling all Admin rows as a single argument");
        return new Object[][]{{ExcelReader.getTestData("Admin")}};
    }

    

    @DataProvider(name = "leaveApplyData", parallel = true)
    public Object[][] leaveApplyData() {
        log.info("Reading Excel sheet: LeaveApply (4 cols expected)");
        return pad(ExcelReader.getTestData("LeaveApply"), 4);
    }

    @DataProvider(name = "leaveAssignData", parallel = true)
    public Object[][] leaveAssignData() {
        log.info("Reading Excel sheet: LeaveAssign (5 cols expected)");
        return pad(ExcelReader.getTestData("LeaveAssign"), 5);
    }

    @DataProvider(name = "myLeaveData", parallel = true)
    public Object[][] myLeaveData() {
        log.info("Reading Excel sheet: MyLeave (fallback → LeaveMyList) (5 cols expected)");
        Object[][] myLeave = ExcelReader.getTestData("MyLeave");
        Object[][] leaveMyList = ExcelReader.getTestData("LeaveMyList");
        return pad(firstNonEmpty(myLeave, leaveMyList), 5);
    }
}
