package com.orangehrm.utils;

import java.io.*;
import java.util.Properties;

public class ConfigReader {
	private static Properties properties = new Properties();

	static {

		try (InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("config.properties")) {
			if (is != null) {
				properties.load(is);
			} else {

				String configPath = System.getProperty("user.dir") + "/resources/config.properties";
				try (FileInputStream fis = new FileInputStream(configPath)) {
					properties.load(fis);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load config.properties", e);
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
}
