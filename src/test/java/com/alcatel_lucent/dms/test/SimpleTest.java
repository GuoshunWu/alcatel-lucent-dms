package com.alcatel_lucent.dms.test;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDeEncoding() throws UnsupportedEncodingException {
		String testString = "测试串";
		// real encoding
		String realEncoding = "gb2312"; 
		// detected encoding
		String assumedEncoding = "iso8859-1";

		// in file bytes
		byte[] realBytes = testString.getBytes(realEncoding);

		// String in previewDCT
		String assumedString = new String(realBytes, assumedEncoding);
		System.out.println(assumedString);

		byte[] assumedBytes = assumedString.getBytes(assumedEncoding);
		
		// String in importDCT
		String resultString = new String(assumedBytes, realEncoding);
		System.out.println(resultString);

		assertEquals(testString, resultString);

	}

}
