/**
 * 
 */
package com.alcatel_lucent.dms.service;

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
import com.alcatel_lucent.dms.model.Dictionary;

/**
 * @author guoshunw
 * 
 */
public class DictionaryServiceImplTest {

	private static DictionaryServiceImpl dictService;
	private static DictionaryService ds;
	private static Class<? extends DictionaryService> cls = DictionaryServiceImpl.class;

	private static DaoService dao;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dictService = new DictionaryServiceImpl();
		ds = (DictionaryService) SpringContext
				.getService(DictionaryService.class);
		dao = (DaoService) SpringContext.getService(DaoService.class);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		dictService = null;
		ds = null;
		dao = null;
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

		// langCharset mapping of language code and its source charset name
		Map<String, String> langCharset = new HashMap<String, String>();
		List<String> keys = Arrays
				.asList("CHK, GAE, FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0"
						.split(","));
		for (String key : keys) {
			langCharset.put(key.trim(), "GBK");
		}
		langCharset.put("CH1", "BIG5");

		// langCodes Alcatel code of languages to import, null if all languages
		// should be imported
		String[] langCodes = new String[] { "CH1" };
		langCodes = null;
		String testFile = "About.dic";
		//testFile = "BandHistory.dic";
		// testFile="communicateBy.dic";

		String dctFileRelativePath = "dct_test_files/CH0/" + testFile;

		// dctFileRelativePath = "dct_test_files/CH0";

		String testFilePath = new File(cls.getResource(dctFileRelativePath)
				.toURI()).getAbsolutePath();

		Dictionary dict = ds.deliverDCT(testFilePath, appId, encoding,
				langCodes, langCharset);
	}

	// @Test
	public void testPreviewDCT() throws URISyntaxException {
		String testFile = "About.dic";
		testFile = "BandHistory.dic";
		// testFile="communicateBy.dic";

		String dctFileRelativePath = "dct_test_files/CH0/" + testFile;

		// dctFileRelativePath = "dct_test_files/CH0";

		String testFilePath = new File(cls.getResource(dctFileRelativePath)
				.toURI()).getAbsolutePath();

		File file = new File(testFilePath);
		Long appId = 1L;
		String encoding = null;

		ds.previewDCT(file.getAbsolutePath(), appId, encoding);
	}
	@Test
	public void testAbnormalDCT() throws Exception {
		Long appId = 1L;
		String encoding = null;
		String[] langCodes = null;
		// langCharset mapping of language code and its source charset name
		Map<String, String> langCharset = new HashMap<String, String>();
		List<String> keys = Arrays
				.asList("CHK, GAE, FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0"
						.split(","));
		for (String key : keys) {
			langCharset.put(key.trim(), "GBK");
		}
		String dctFileRelativePath = "dct_test_files/abnormal/invalid-utf8.dct";

		String testFilePath = new File(cls.getResource(dctFileRelativePath)
				.toURI()).getAbsolutePath();

		Dictionary dict = ds.deliverDCT(testFilePath, appId, encoding,
				langCodes, langCharset);
	}

	@Test
	public void testgenerateDCT() {
		Long dctId = 1L;
		String encoding = "GBK";
		String fileName = "Test" + encoding + ".sql";

		Map<String, String> langCharset = new HashMap<String, String>();

		List<String> keys = Arrays
				.asList("CHK, ZH0, GAE, FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0"
						.split(","));
		for (String key : keys) {
			langCharset.put(key.trim(), "GBK");
		}
		langCharset.put("CH1", "BIG5");

		String[] langCodes = new String[] { "CH0", "CH1" };
		langCodes = null;
		ds.generateDCT(fileName, dctId, encoding, langCodes, langCharset);
	}
}
