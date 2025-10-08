package com.orangehrm.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ReportManager {

    private static ExtentReports extent;

    private ReportManager() {
     
    }

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            try {
                String base = ConfigReader.getProperty("report.path");
                if (base == null || base.isBlank()) {
                    base = "output/test-output";
                }

                Files.createDirectories(Path.of(base));

                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String reportFile = Path.of(base, "ExtentReport_" + ts + ".html").toString();

                ExtentSparkReporter spark = new ExtentSparkReporter(reportFile);
                spark.config().setDocumentTitle("OrangeHRM Automation Report");
                spark.config().setReportName("UI Test Execution Summary");

                extent = new ExtentReports();
                extent.attachReporter(spark);

                extent.setSystemInfo("URL", ConfigReader.getProperty("url"));
              
                extent.setSystemInfo("Browser", System.getProperty("browser", ConfigReader.getProperty("browser")));
                extent.setSystemInfo("OS", System.getProperty("os.name"));
                extent.setSystemInfo("Java Version", System.getProperty("java.version"));

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        extent.flush();
                        System.out.println("Extent report flushed successfully on JVM shutdown.");
                    } catch (Exception e) {
                        System.err.println("Failed to flush extent report: " + e.getMessage());
                    }
                }));

            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize ExtentReports", e);
            }
        }
        return extent;
    }

    public static synchronized void flushReports() {
        if (extent != null) {
            extent.flush();
            System.out.println("Extent report manually flushed.");
        }
    }
}
