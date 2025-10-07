package com.orangehrm.suites;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.collections.Lists;
import java.util.List;

public class TestNGRunner {
	public static void main(String[] args) {
		TestListenerAdapter tla = new TestListenerAdapter();
		TestNG testng = new TestNG();


		List<String> suites = Lists.newArrayList();
		suites.add("src/test/resources/testng.xml");

		testng.setTestSuites(suites);
		testng.addListener(tla);
		testng.run();
	}
}
