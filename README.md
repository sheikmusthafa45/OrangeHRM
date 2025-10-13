OrangeHRM Selenium Test Automation (POM + TestNG)


A simple Selenium framework for the OrangeHRM demo site using Page Object Model (POM), TestNG, Maven, and ExtentReports.
Supports data-driven tests (Excel), cross-browser (Chrome / Firefox), screenshots on failure, and logs.


Tech Stack:

Java 11
Maven
Selenium 4
TestNG
WebDriverManager
Apache POI (Excel)
ExtentReports
Log4j2


Demo Site & Credentials:
URL: https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
Username: Admin
Password: admin123
These are public demo creds. Do not commit real credentials.




Project Structure (key folders):

OrangeHRM-Automation/
├─ src/
│  ├─ main/java/
│  │  ├─ com/orangehrm/pages/      # Page Objects (LoginPage, DashboardPage, PIMPage, AdminPage, LeavePage, BasePage)
│  │  ├─ com/orangehrm/utils/      # Utilities (ConfigReader, ExcelReader, ReportManager, LoggerUtil)
│  │  ├─ com/orangehrm/managers/   # DriverManager
│  │  └─ com/orangehrm/data/       # TestNG DataProviders
│  └─ test/java/
│     ├─ com/orangehrm/tests/      # Tests (LoginTests, DashboardTests, PIMTests, AdminTests, LeaveTests)
│     ├─ com/orangehrm/listeners/  # TestListener (screenshots, reporting)
│     └─ com/orangehrm/suites/     # TestNGRunner (optional Java main)
├─ resources/                       # config.properties, testdata.xlsx (if not under src/test/resources)
└─ src/test/resources/
   ├─ testng.xml                    # TestNG suite
   └─ log4j2.xml                    # Logging config


Configuration:
Create or edit resources/config.properties (or src/test/resources/config.properties):
browser=chrome
url=https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
implicit.wait=10
explicit.wait=15
screenshot.path=output/screenshots/
report.path=output/test-output
default.username=Admin
default.password=admin123

Override browser at runtime:
-Dbrowser=chrome
-Dbrowser=firefox


Test Data (Excel):

File: resources/testdata.xlsx
Example sheets: Login, Dashboard, PIM, Admin, Leave, LeaveApply, LeaveAssign, MyLeave, LeaveTypes, LeaveEntitlements
Reader logic: tries classpath first; if not found, falls back to resources/testdata.xlsx.
Keep a header row; empty rows are skipped.

How to Run;
Maven (recommended)
mvn clean test
Uses src/test/resources/testng.xml.

Set browser:
mvn clean test -Dbrowser=chrome
mvn clean test -Dbrowser=firefox

Run from Java main (optional):
Run com.orangehrm.suites.TestNGRunner (points to src/test/resources/testng.xml).


Parallel & Cross-Browser:
Configured in testng.xml (adjust parallel and thread-count as needed).
Separate <test> blocks for Chrome and Firefox.
DriverManager uses ThreadLocal<WebDriver> to isolate sessions.

Reports, Screenshots, Logs:
ExtentReports HTML: output/test-output/ExtentReport_<timestamp>.html
Screenshots on failure: output/screenshots/
Logs (Log4j2): output/logs/framework.log (configured in log4j2.xml)



Test Coverage (high level):

Login:
Valid login + logout
Invalid username / password
Empty credentials

Dashboard:
Header & menus visible
Quick Launch widget + tiles
Employee Distribution (title + chart)
Pending Leave Requests (skips if not present)
Baseline widget titles

PIM:
Add employee with login (data-driven)
Employee List page checks
Search by Employee ID

Admin:
Add user + search
Add job title + verify
Add pay grade + currency/salary + verify
Add employment status + verify
Open Organization Structure

Leave:
Apply leave (handles “No Leave Types with Leave Balance” banner)
Assign leave (autocomplete)
My Leave table by date/status
Entitlements → Add Entitlements (list leave types)
Reports → Leave Balance (row count)

Common Commands:
# Clean & run all tests
mvn clean test
# Run on Firefox
mvn clean test -Dbrowser=firefox
# Run a single test class
mvn -Dtest=LoginTests test


 Troubleshooting:
Drivers: No manual setup needed; WebDriverManager downloads automatically.
Excel not found: Ensure testdata.xlsx is on classpath or under resources/.
Timeouts: Tune implicit.wait / explicit.wait in config.properties.
Paths: Make sure output folders are writable (screenshots, reports, logs).
Parallel runs: Keep ThreadLocal usage in DriverManager.

 Notes:
Pages extend BasePage and use its shared driver/wait.
PIMPage updated to rely on BasePage’s driver/wait (no duplication).
Keep locators stable; prefer explicit waits for dynamic elements.

License:
For learning/demo use. Respect OrangeHRM demo site terms.

















