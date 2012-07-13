package com.alcatel_lucent.dms.test;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alcatel_lucent.dms.service.DictionaryService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring.xml" })
public class SimpleTest{
	

	private static Logger log=Logger.getLogger(SimpleTest.class);

	@Autowired
	private DictionaryService ds;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log.info("Let's begin.");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ProxoolFacade.shutdown(2);
		log.info("I am going to shutdown.");
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	public void testDeEncoding() throws UnsupportedEncodingException {
		String testString = "測試中國";
		// real encoding

		Charset gbkCharset = Charset.forName("gbk");
		Charset utf8Charset = Charset.forName("utf8");
		// Charset big5Charset=Charset.forName("big5");
		// Charset utf16Charset=Charset.forName("utf16");

		// in file bytes
		ByteBuffer buf = gbkCharset.encode(testString);

		// String in previewDCT
		CharBuffer assumedCharBuf = utf8Charset.decode(buf);

		System.out.println(assumedCharBuf);

		ByteBuffer assumedByteBuffer = utf8Charset.encode(assumedCharBuf);

		// String in importDCT
		CharBuffer resultString = gbkCharset.decode(assumedByteBuffer);
		System.out.println(resultString);
		//
		// assertEquals(testString, resultString);

	}

	@Test
	public void testMessageFormat() {
		Long long1=new Long(1);
		Long long2=new Long(1);
//		assertTrue(long1.equals(long2));
//		assertTrue();
		System.out.println(long1==long2);
		
		
		
	}

	
}
