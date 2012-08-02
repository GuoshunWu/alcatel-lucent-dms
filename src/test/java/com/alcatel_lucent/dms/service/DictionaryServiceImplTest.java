/**
 * 
 */
package com.alcatel_lucent.dms.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;

/**
 * @author Guoshun.Wu
 * 
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class DictionaryServiceImplTest {

	@Autowired
	private DictionaryService ds;

	private static String testFilesPathDir;

	private static Logger log = Logger
			.getLogger(DictionaryServiceImplTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		File testFilePath = new File(DictionaryServiceImpl.class.getResource(
				"/").toURI());
		testFilePath = testFilePath.getParentFile().getParentFile();
		testFilesPathDir = new File(testFilePath, "dct_test_files")
				.getAbsolutePath() + "/";
		log.info("Test file path is: " + testFilesPathDir);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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

	// @Ignore("It will throw dup_label BusinessException")
	@Test(expected = BusinessException.class, timeout = 10000)
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
		langCharset.put("ko_KR", "KOI8-R");

		String dctFileRelativePath = testFilesPathDir + "abnormal/";

		String testFilePath = dctFileRelativePath + "dup_label.dic";

		Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
		ds.deliverDCT("dup_label.dic", testFilePath, appId, encoding,
				langCodes, langCharset, warnings);
	}
	
}
