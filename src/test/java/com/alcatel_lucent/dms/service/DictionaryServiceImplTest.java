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

import org.apache.log4j.Logger;
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

	private static DictionaryService ds;
	private static String testFilesPathDir;
	private static Logger log;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log = Logger.getLogger(DictionaryServiceImplTest.class);
		ds = (DictionaryService) SpringContext
				.getService(DictionaryService.class);

		File testFilePath = new File(DictionaryServiceImpl.class.getResource(
				"/").toURI());
		testFilePath = testFilePath.getParentFile().getParentFile();
		testFilesPathDir = new File(testFilePath, "dct_test_files")
				.getAbsolutePath() + "/";
		log.info("Test file path is: " + testFilesPathDir);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ds = null;
		testFilesPathDir = null;
		log = null;
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
	public void testDeliverDCT() throws Exception {

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
		// testFile = "BandHistory.dic";
		// testFile="communicateBy.dic";

		String dctFileRelativePath = "CH0/";
		// dctFileRelativePath = "CH1/";

		String testFilePath = testFilesPathDir + dctFileRelativePath + testFile;
		ds.deliverDCT(testFilePath, appId, encoding, langCodes, langCharset);
	}

	@Test
	public void testPreviewDCT() throws Exception {
		String testFile = "About.dic";
		testFile = "BandHistory.dic";
		// testFile="communicateBy.dic";

		String dctFileRelativePath = "CH0/";
		// dctFileRelativePath = "CH1/";

		File file = new File(testFilesPathDir, dctFileRelativePath + testFile);

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
				.asList("CHK, GAE, FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0, en, ru, en_US, en_GB, fr, de, es, it, nl, no, pt, ko_KR, zh_CN, pl, fi, ca_ES, cs, sv, hu, zh_TW, ar, he, tr, da"
						.split(","));
		for (String key : keys) {
			langCharset.put(key.trim(), "GBK");
		}
		String dctFileRelativePath = testFilesPathDir + "abnormal/";

		String testFilePath = dctFileRelativePath + "dup_label.dic";

		ds.deliverDCT(testFilePath, appId, encoding, langCodes, langCharset);

	}

	@Test
	public void testgenerateDCT() {
		Long dctId = 1L;
		String encoding = "GBK";
		String fileName = testFilesPathDir + "Test" + encoding + ".sql";

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
