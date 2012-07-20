/**
 * 
 */
package com.alcatel_lucent.dms.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

import static com.alcatel_lucent.dms.service.DictionaryServiceImpl.*;

/**
 * @author Guoshun.Wu
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
	public void testSampleAbout_DCT() throws Exception {

		Long appId = 1L;
		String encoding = null;

		// langCharset mapping of language code and its source charset name
		Map<String, String> langCharset = new HashMap<String, String>();
		List<String> keys = Arrays
				.asList("CHK, GAE, FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0"
						.split(", *"));
		for (String key : keys) {
			langCharset.put(key.trim(), "GBK");
		}

		// langCodes Alcatel code of languages to import, null if all languages
		// should be imported
		String[] langCodes = new String[] { "CH1" };
		langCodes = null;

		String dictName = "About.dic";
		String testFile = "CH0/About.dic";
		String updatedTestFile = "CH0/About_Changed.dic";

		String testFilePath = testFilesPathDir + testFile;

		Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();

		/***************************************** Test for deliver DCT ****************************************/

		Dictionary dbDict = ds.deliverDCT(dictName, testFilePath, appId,
				encoding, langCodes, langCharset, warnings);

		// dictionary check
		dbDict = (Dictionary) dao.retrieveOne(
				"from Dictionary where name=:name",
				JSONObject.fromObject("{'name':'" + dictName + "'}"),
				new String[] { "labels", "dictLanguages" });
		assertThat(dbDict, is(notNullValue()));

		// dictionary language check
		HashSet dbLangCodes = dbDict.getAllLanguageCodes();

		// CHK is not saved in database.
		List<String> filelangCodes = Arrays
				.asList("FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0"
						.split(", *"));
		Collections.sort(filelangCodes);

		assertTrue(dbLangCodes.containsAll(filelangCodes));
		// labels check

		Context dbCtx = (Context) dao.retrieveOne(
				"from Context where name=:name",
				JSONObject.fromObject("{'name':'" + dbDict.getName() + "'}"));
		assertNotNull(dbCtx);

		// prepare expected result data
		List<Label> validateLabels = new ArrayList<Label>();
		Label tmpLabel = new Label();
		tmpLabel.setDictionary(dbDict);
		tmpLabel.setReference("Warning: This computer program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it may result in severe civil and criminal penalties, and will be prosecuted to the maximum extent possible under the law.");
		tmpLabel.setKey("WARNING");
		tmpLabel.setMaxLength("399");

		validateLabels.add(tmpLabel);

		tmpLabel = new Label();
		tmpLabel.setDictionary(dbDict);
		tmpLabel.setReference("Copyright 2007-2012 by Alcatel-Lucent. All rights reserved./nAlcatel-Lucent and Alcatel-Lucent Logo are respectively registered/ntrademark and service mark of Alcatel-Lucent.");
		tmpLabel.setKey("COPYRIGHT");
		tmpLabel.setMaxLength("79,86,97");

		validateLabels.add(tmpLabel);

		tmpLabel = new Label();
		tmpLabel.setDictionary(dbDict);
		tmpLabel.setReference("My Instant Communicator client software version ");
		tmpLabel.setKey("MPC_VERSION");
		tmpLabel.setMaxLength("57");

		validateLabels.add(tmpLabel);

		MultiKeyMap translatedStringMap = new MultiKeyMap();

		translatedStringMap
				.put("WARNING",
						"EN0",
						"Warning: This computer program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it may result in severe civil and criminal penalties, and will be prosecuted to the maximum extent possible under the law.");
		translatedStringMap
				.put("WARNING", "CH0",
						"警告：本计算机程序受到版权法和国际条约的保护。未经授权而复制或披露本程序或其任何部分程序，可能会受到严重的民事或刑事处罚，并将依法进行起诉");
		translatedStringMap
				.put("WARNING",
						"US0",
						"Warning: This program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it may result in severe civil and criminal penalties, and violators will be prosecuted to the maximum extent possible under the law.");

		translatedStringMap
				.put("COPYRIGHT",
						"EN0",
						"Copyright 2007-2012 by Alcatel-Lucent. All rights reserved./nAlcatel-Lucent and Alcatel-Lucent Logo are respectively registered/ntrademark and service mark of Alcatel-Lucent.");
		translatedStringMap
				.put("COPYRIGHT",
						"CH0",
						"2007-2012年阿尔卡特朗讯版权所有。保留所有权力。/nAlcatel-Lucent与Alcatel-Lucent标识是阿尔卡特朗讯各自的注册商标和服务标记。");
		translatedStringMap
				.put("COPYRIGHT",
						"US0",
						"Copyright 2007-2012 by Alcatel-Lucent. All rights reserved./nAlcatel-Lucent and Alcatel-Lucent Logo are respectively registered/ntrademark and service mark of Alcatel-Lucent.");

		translatedStringMap.put("MPC_VERSION", "EN0",
				"My Instant Communicator client software version ");
		translatedStringMap.put("MPC_VERSION", "CH0", "我的即时通客户端软件版本 ");
		translatedStringMap.put("MPC_VERSION", "US0",
				"My Instant Communicator client software version ");

		/*
		 * check if there are ('EN0','CH0','US0') language codes related
		 * translations
		 */
		List validateTranslationsLangCodeAndIDList = dao
				.retrieve("select al.language.id,al.code from AlcatelLanguageCode al where code in ('EN0','CH0','US0')");

		Map<String, Long> validateTranslationsLangCodeAndIDMap = new HashMap<String, Long>();
		for (Object langIDCode : validateTranslationsLangCodeAndIDList) {
			Object[] arrayLangIDCode = (Object[]) langIDCode;
			Long langID = (Long) (arrayLangIDCode[0]);
			String langCode = (String) (arrayLangIDCode[1]);
			validateTranslationsLangCodeAndIDMap.put(langCode, langID);
		}

		for (Label label : validateLabels) {
			Label dbLabel = dbDict.getLabel(label.getKey());
			assertNotNull(dbLabel);

			label.setContext(dbCtx);

			assertEquals(label.getReference(), dbLabel.getReference());
			assertEquals(label.getMaxLength(), dbLabel.getMaxLength());

			Map<String, Object> params = new HashMap<String, Object>();

			params.put("reference", label.getReference());
			params.put("contextid", label.getContext().getId());

			Text dbText = (Text) dao
					.retrieveOne(
							"from Text where reference=:reference and context.id=:contextid",
							params, new String[] { "translations" });

			assertNotNull(dbText);

			Translation trans = null;
			log.info("validating if there are ('EN0','CH0','US0') translation in database.");

			// key:langCode, value:langID
			for (Map.Entry<String, Long> codeAndId : validateTranslationsLangCodeAndIDMap
					.entrySet()) {

				trans = dbText.getTranslation(codeAndId.getValue());
				assertNotNull("Translation item for " + codeAndId.getKey()
						+ " not found.", trans);
				log.info("label key: " + label.getKey() + ", language code: "
						+ codeAndId.getKey());
				assertThat(trans.getTranslation(),
						equalTo(translatedStringMap.get(label.getKey(),
								codeAndId.getKey())));
			}
		}

		/***************************************** Test updated test file deliver DCT ****************************************/
		testFilePath = testFilesPathDir + updatedTestFile;
		// re deliver the updated DCT file
		langCharset.put("CH1", "BIG5");

		dbDict = ds.deliverDCT(dictName, testFilePath, appId, encoding,
				langCodes, langCharset, warnings);
		// check result

		dbDict = (Dictionary) dao.retrieveOne(
				"from Dictionary where name=:name",
				JSONObject.fromObject("{'name':'" + dictName + "'}"),
				new String[] { "labels", "dictLanguages" });

		// check added dictionary language
		assertThat(dbDict.getAllLanguageCodes(), hasItem("CH1"));

		// check added new label TESTLABEL
		Label dbLabel = dbDict.getLabel("TESTLABEL");
		assertThat(dbLabel, is(notNullValue()));

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("reference", dbLabel.getReference());
		params.put("contextid", dbLabel.getContext().getId());
		Text dbText = (Text) dao
				.retrieveOne(
						"from Text where reference=:reference and context.id=:contextid",
						params, new String[] { "translations" });
		// check translations

		// added translation
		translatedStringMap.put("TESTLABEL", "EN0", "Test");
		translatedStringMap.put("TESTLABEL", "US0", "Test");
		translatedStringMap.put("TESTLABEL", "CH0", "测试");

		// key:langCode, value:langID
		for (Map.Entry<String, Long> codeAndId : validateTranslationsLangCodeAndIDMap
				.entrySet()) {

			Translation trans = dbText.getTranslation(codeAndId.getValue());
			assertNotNull("Translation item for " + codeAndId.getKey()
					+ " not found.", trans);
			log.info("label key: " + dbLabel.getKey() + ", language code: "
					+ codeAndId.getKey());
			assertThat(
					trans.getTranslation(),
					equalTo(translatedStringMap.get(dbLabel.getKey(),
							codeAndId.getKey())));
		}

		// updated translation

		translatedStringMap
				.put("COPYRIGHT",
						"CH0",
						"用于测试的改变，2007-2012年阿尔卡特朗讯版权所有。保留所有权力/nAlcatel-Lucent与Alcatel-Lucent标识是阿尔卡特朗讯各自的注册商标和服务标记。");

		dbLabel = dbDict.getLabel("COPYRIGHT");
		params = new HashMap<String, Object>();
		params.put("reference", dbLabel.getReference());
		params.put("contextid", dbLabel.getContext().getId());
		dbText = (Text) dao
				.retrieveOne(
						"from Text where reference=:reference and context.id=:contextid",
						params, new String[] { "translations" });

		Translation trans = dbText
				.getTranslation(validateTranslationsLangCodeAndIDMap.get("CH0"));

		assertThat(trans.getTranslation(),
				equalTo(translatedStringMap.get(dbLabel.getKey(), "CH0")));

		/*************************** Test generate dct file from dictionary in database *************************/
		String targetFileName = "target/" + dictName + "_generated.dct";
		ds.generateDCT(targetFileName, dbDict.getId(), encoding, langCodes,
				langCharset);
		File generatedFile = new File(targetFileName);
		assertTrue("Dictionary " + generatedFile.getName()
				+ " is not generated.", generatedFile.exists());

		/*************************** Test deletel dictionary in database *************************/
		ds.deleteDCT(dictName);
		Dictionary origDict = dbDict;
		dbDict = (Dictionary) dao.retrieveOne(
				"from Dictionary where name=:name",
				JSONObject.fromObject("{'name':'" + dictName + "'}"),
				new String[] { "labels", "dictLanguages" });

		// check dictionary
		assertThat(dbDict, is(nullValue()));

		// check labels
		params.clear();
		params.put("dictId", origDict.getId());
		List<Label> labels = dao.retrieve(
				"from Label where dictionary.id=:dictId", params);
		assertTrue("Some label(s): " + labels + " in " + origDict.getName()
				+ " dictionary is(are) not deleted.", labels.isEmpty());

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

	@Test
	// @Ignore("Debug...")
	public void testRealDCTFile() {
		Long appId = 1L;
		String encoding = null;

		// langCharset mapping of language code and its source charset name
		Map<String, String> langCharset = new HashMap<String, String>();
		List<String> keys = Arrays
				.asList("CHK, GAE, FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0, en, fr, de, es, pt, it, no, ko, nl, zh_CN, ru, fi, pl, en_US, ca, cs, hu, zh, sv, ar, he, tr, da"
						.split(",//s*"));
		for (String key : keys) {
			langCharset.put(key.trim(), "GBK");
		}

		// langCodes Alcatel code of languages to import, null if all languages
		// should be imported
		String[] langCodes = new String[] { "CH1" };
		langCodes = null;

		String tempFilesPathDir = "D:/tmp/AR/6.6.000.107.a/web_administration/wadmin/Ihm/CommonAdmin/xml/";
		String testFile = "appli.labels.dct";
		String testFilePath = tempFilesPathDir + testFile;
		String dictName = "appli.labels.dct";

		// tempFilesPathDir =
		// "D:/tmp/AR/6.6.000.107.a/voice_applications/eCC_tui/VoiceApplications/dictionaries/";
		// testFile = "TUI.dct";
		// testFilePath = testFilesPathDir + testFile;
		// dictName = "TUI.dct";
		//
		// tempFilesPathDir =
		// "D:/tmp/AR/6.6.000.107.a/data_access_service/dataaccess/WEB-INF/classes/com/alcatel/dataaccess/global/dico/";
		// testFile = "DtaEccServer.dct";
		// testFilePath = testFilesPathDir + testFile;
		// dictName = "DtaEccServer.dct";

		Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();

		long before = System.currentTimeMillis();
		ds.deliverDCT(dictName, testFilePath, appId, encoding, langCodes,
				langCharset, warnings);
		long after = System.currentTimeMillis();

		log.info("**************DeliverDCT take " + (after - before)
				+ " milliseconds of time.***************");
	}

	@Test
	public void testDeliverDCTFiles() {
		Long appId = 1L;
		String encoding = null;

		// langCharset mapping of language code and its source charset name
		Map<String, String> langCharset = new HashMap<String, String>();
//		List<String> keys = Arrays
//				.asList("CHK, GAE, FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0, en, fr, de, es, pt, it, no, ko, nl, zh_CN, ru, fi, pl, en_US, ca, cs, hu, zh, sv, ar, he, tr, da"
//						.split(",\\s*"));
		
		List<String> keys = Arrays
				.asList("AR0, ar"
						.split(",\\s*"));
		
		for (String key : keys) {
			langCharset.put(key.trim(), "windows-1256");
		}

		// langCodes Alcatel code of languages to import, null if all languages
		// should be imported
		String[] langCodes = new String[] { "AR0", "ar" };
//		langCodes = null;


		String rootDir="Z:/AR";
		String testFilePath = rootDir;
		
		

//		encoding in utf8 without BOM
//		testFilePath = "Z:/AR/6.6.000.107.a/mail_access_service_component/masc/ServerMasc/masc-core/src/main/alarms/MASC.dic";
//		langCharset.put("AR0", "UTF-8");
//		langCharset.put("ar", "UTF-8");
//		
//		encoding in utf8
//		testFilePath = "Z:/AR/6.6.000.107.a/call_routing_service/ecccrs/crs.dic";
//		langCharset.put("AR0", "UTF-8");
//		langCharset.put("ar", "UTF-8");
		
//		encoding in USC-2 Little Endian
//		testFilePath = "Z:/AR/6.6.000.107.a/authentication_form/ecc_common/authentication_form/FormLogin.dic";
//		langCharset.put("AR0", "UTF-16");
//		langCharset.put("ar", "UTF-16");
		
//		testFilePath = "Z:/AR/6.6.000.107.a/otuclib/ecc_common/otuclib/dico/otuclib.dic";

		
		
		Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
		
		logDictDeliverFail.info(String.format("%s, %s, %s", "Name","Path","cause" ));
		Collection<Dictionary> dicts=ds.deliverDCTFiles(rootDir, new File(testFilePath), appId, encoding, langCodes,
				langCharset, warnings);
		
		String format="%s, %s, %s, %s";
		logDictDeliverSuccess.info(String.format(format, "ID", "name", "encoding", "path"));
		for(Dictionary dict:dicts){
			logDictDeliverSuccess.info(String.format(format, dict.getId(), dict.getName(), dict.getEncoding(), dict.getPath()));
		}
		System.out.println();
		
	}
}
