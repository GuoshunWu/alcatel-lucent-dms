/**
 * 
 */
package com.alcatel_lucent.dms.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

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
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

/**
 * @author guoshunw
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class DictionaryServiceImplTest {

	@Autowired
	private DictionaryService ds;

	@Autowired
	private DaoService dao;

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
		Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
		ds.deliverDCT(testFilePath, appId, encoding, langCodes, langCharset, warnings);

		// asserts
		// dictionary check
		Dictionary dbDict = (Dictionary) dao.retrieveOne(
				"from Dictionary where name=:name",
				JSONObject.fromObject("{'name':'" + testFile + "'}"),
				new String[] { "labels" });
		assertNotNull(dbDict);
		// labels check

		Context dbCtx = (Context) dao.retrieveOne(
				"from Context where name=:name",
				JSONObject.fromObject("{'name':'" + dbDict.getName() + "'}"));
		assertNotNull(dbCtx);

		List<Label> validateLabels = new ArrayList<Label>();
		Label tmpLabel = new Label();
		tmpLabel.setDictionary(dbDict);
		tmpLabel.setReference("Warning: This computer program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it may result in severe civil and criminal penalties, and will be prosecuted to the maximum extent possible under the law.");
		tmpLabel.setKey("WARNING");
		tmpLabel.setMaxLength("399");

		validateLabels.add(tmpLabel);

		tmpLabel = new Label();
		tmpLabel.setDictionary(dbDict);
		tmpLabel.setReference("Copyright 2007-2012 by Alcatel-Lucent. All rights reserved.\nAlcatel-Lucent and Alcatel-Lucent Logo are respectively registered\ntrademark and service mark of Alcatel-Lucent.");
		tmpLabel.setKey("COPYRIGHT");
		tmpLabel.setMaxLength("79,86,97");

		validateLabels.add(tmpLabel);

		tmpLabel = new Label();
		tmpLabel.setDictionary(dbDict);
		tmpLabel.setReference("My Instant Communicator client software version ");
		tmpLabel.setKey("MPC_VERSION");
		tmpLabel.setMaxLength("57");

		validateLabels.add(tmpLabel);

		// check if there are ('EN0','CH0','US0') language codes related
		// translations
		List validateTranslationsLangIDs = dao
				.retrieve("select al.language.id,al.code from AlcatelLanguageCode al where code in ('EN0','CH0','US0')");
		System.out.println(validateTranslationsLangIDs.get(0));

//		for (Label label : validateLabels) {
//			Label dbLabel = dbDict.getLabel(label.getKey());
//			assertNotNull(dbLabel);
//
//			label.setContext(dbCtx);
//
//			assertEquals(label.getReference(), dbLabel.getReference());
//			assertEquals(label.getMaxLength(), dbLabel.getMaxLength());
//
//			Map<String, Object> params = new HashMap<String, Object>();
//
//			params.put("reference", label.getReference());
//			params.put("contextid", label.getContext().getId());
//
//			Text dbText = (Text) dao
//					.retrieveOne(
//							"from Text where reference=:reference and context.id=:contextid",
//							params, new String[] { "translations" });
//
//			assertNotNull(dbText);
//
//			Translation trans = null;
//			log.info("validating if there are ('EN0','CH0','US0') translation in database.");
//			System.out.println(validateTranslationsLangIDs);
//			for (Long langID : validateTranslationsLangIDs) {
//				trans = dbText.getTranslation(langID);
//				assertNotNull("Translation item for not found.", trans);
//			}
//		}
	}

	// @Test
	public void testDeleteDictionary() {
		// dao.delete(null);
	}

	// @Test
	public void testPreviewDCT() throws Exception {
		String testFile = "About.dic";
		testFile = "BandHistory.dic";
		// testFile="communicateBy.dic";

		String dctFileRelativePath = "CH0/";
		// dctFileRelativePath = "CH1/";

		File file = new File(testFilesPathDir, dctFileRelativePath + testFile);

		Long appId = 1L;
		String encoding = null;
		Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
		ds.previewDCT(file.getAbsolutePath(), appId, encoding, warnings);
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
		langCharset.put("ko_KR", "KOI8-R");

		String dctFileRelativePath = testFilesPathDir + "abnormal/";

		String testFilePath = dctFileRelativePath + "dup_label.dic";

		Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
		ds.deliverDCT(testFilePath, appId, encoding, langCodes, langCharset, warnings);

	}

	// @Test
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
