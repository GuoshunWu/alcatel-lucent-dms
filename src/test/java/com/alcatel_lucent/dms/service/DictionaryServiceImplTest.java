/**
 * 
 */
package com.alcatel_lucent.dms.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.logicalcobwebs.proxool.ProxoolFacade;

import com.alcatel_lucent.dms.SpringContext;

/**
 * @author guoshunw
 * 
 */
public class DictionaryServiceImplTest {

	private static DictionaryServiceImpl dictService;
	private static DictionaryService ds;
	private static Class<? extends DictionaryService> cls = DictionaryServiceImpl.class;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dictService = new DictionaryServiceImpl();
		ds = (DictionaryService) SpringContext
				.getService(DictionaryService.class);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		dictService = null;
		ds = null;
		ProxoolFacade.shutdown(2);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

	}

	/**
	 * Test method for
	 * {@link com.alcatel_lucent.dms.service.DictionaryServiceImpl#detectEncoding(java.io.InputStream)}
	 * .
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testDetectEncoding() throws IOException, URISyntaxException {

		String expect = "UTF-16";
		File testFile = new File(cls.getResource("testUTF16BE.txt").toURI());
		assertEquals(expect, dictService.detectEncoding(testFile));

		testFile = new File(cls.getResource("testUTF16LE.txt").toURI());
		assertEquals(expect, dictService.detectEncoding(testFile));

		expect = "UTF-8";
		testFile = new File(cls.getResource("testUTF8.txt").toURI());
		assertEquals(expect, dictService.detectEncoding(testFile));

		expect = "ISO-8859-1";
		testFile = new File(cls.getResource("testNoBOM.txt").toURI());
		assertEquals(expect, dictService.detectEncoding(testFile));

	}

	/**
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testDeliverDCT() throws IOException, URISyntaxException {

		Long appId = 1L;
		// encoding encoding of source file, null if auto-detected
		// (ANSI/UTF8/UTF16)
		String encoding = null;
		// langCodes Alcatel code of languages to import, null if all languages
		// should be imported
		String[] langCodes = null;
		// langCharset mapping of language code and its source charset name
		Map<String, String> langCharset = new HashMap<String, String>();
		List<String> keys = Arrays
				.asList("CHK, GAE, FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0"
						.split(","));
		for(String key:keys){
			langCharset.put(key.trim(), "GBK");
		}

		String dctFileRelativePath = "dct_test_files/CH0/About.dic";
		String testFilePath = new File(cls.getResource(dctFileRelativePath)
				.toURI()).getAbsolutePath();

		ds.deliverDCT(testFilePath, appId, encoding, langCodes, langCharset);

	}

}
