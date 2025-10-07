package com.orangehrm.utils;

import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {
	private static final Logger log = LoggerUtil.getLogger(ExcelReader.class);

	public static Object[][] getTestData(String sheetName) {

		try (InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("testdata.xlsx")) {
			if (is != null) {
				return readSheet(is, sheetName, "classpath:testdata.xlsx");
			}
		} catch (Exception ignored) {}


		String excelPath = System.getProperty("user.dir") + "/resources/testdata.xlsx";
		try (FileInputStream fis = new FileInputStream(excelPath)) {
			return readSheet(fis, sheetName, excelPath);
		} catch (Exception e) {
			log.error("Failed to read test data from sheet: " + sheetName, e);
			return new Object[0][0];
		}
	}

	private static Object[][] readSheet(InputStream stream, String sheetName, String origin) throws IOException {
		try (Workbook workbook = WorkbookFactory.create(stream)) {
			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				log.error("Sheet '{}' not found in file: {}", sheetName, origin);
				return new Object[0][0];
			}

			Row header = sheet.getRow(0);
			if (header == null) {
				log.error("No header row (row 0) in sheet: {} (file: {})", sheetName, origin);
				return new Object[0][0];
			}


			int lastHeaderCell = header.getLastCellNum(); 
			int cols = 0;
			for (int c = lastHeaderCell - 1; c >= 0; c--) {
				Cell hc = header.getCell(c);
				String hv = (hc == null) ? "" : hc.toString().trim();
				if (!hv.isEmpty()) { cols = c + 1; break; }
			}
			if (cols <= 0) {
				log.error("Header row appears empty in sheet: {} (file: {})", sheetName, origin);
				return new Object[0][0];
			}

			DataFormatter fmt = new DataFormatter(); 
			List<Object[]> rows = new ArrayList<>();

			for (int r = 1; r <= sheet.getLastRowNum(); r++) {
				Row row = sheet.getRow(r);
				Object[] data = new Object[cols];
				boolean allBlank = true;

				for (int c = 0; c < cols; c++) {
					String val = "";
					if (row != null) {
						Cell cell = row.getCell(c);
						if (cell != null) val = fmt.formatCellValue(cell).trim();
					}
					data[c] = val;
					if (!val.isEmpty()) allBlank = false;
				}


				if (!allBlank) rows.add(data);
			}

			log.info("Successfully read test data from sheet: {} ({} rows, {} cols trimmed)",
					sheetName, rows.size(), cols);
			return rows.toArray(new Object[0][]);
		}
	}
}
