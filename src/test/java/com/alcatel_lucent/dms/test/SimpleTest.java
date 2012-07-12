package com.alcatel_lucent.dms.test;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Locale;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

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

//	@Test
	public void testDeEncoding() throws UnsupportedEncodingException {
		String testString = "測試中國";
		// real encoding
		String realEncoding = "gb2312";
		realEncoding = "utf8";
		realEncoding = "utf16";
		realEncoding = "big5";

		// detected encoding
		String assumedEncoding = "iso8859-1";
		// assumedEncoding = "utf8";
		// assumedEncoding = "utf16";

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

//	@Test
	public void testMessageFormat() {
		String localizedString = "This is {0}, 测试 {1}, Who. {5}";
		Object[] params =new Object[] { "Test", "Hello", 23 };
		params=null;
		MessageFormat msgFmt = new MessageFormat(localizedString,
				Locale.getDefault());
		StringBuffer result=new StringBuffer();
		StringBuffer r1=msgFmt.format(params,result, null);
		System.out.println(result);
		System.out.println(r1);
	}
	


}
