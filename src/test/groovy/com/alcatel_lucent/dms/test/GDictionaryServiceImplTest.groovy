package com.alcatel_lucent.dms.test


import static com.alcatel_lucent.dms.service.DictionaryServiceImpl.logDictDeliverFail
import static com.alcatel_lucent.dms.service.DictionaryServiceImpl.logDictDeliverSuccess
import static com.alcatel_lucent.dms.service.DictionaryServiceImpl.DictDeliverWarning

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.Matchers.nullValue
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

import java.io.File
import java.util.Collection
import java.util.List
import java.util.Map

import org.apache.commons.collections.keyvalue.MultiKey
import org.apache.commons.collections.map.MultiKeyMap
import org.apache.log4j.Logger
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration

import com.alcatel_lucent.dms.BusinessWarning
import com.alcatel_lucent.dms.model.Context
import com.alcatel_lucent.dms.model.Dictionary
import com.alcatel_lucent.dms.model.Label
import com.alcatel_lucent.dms.model.Text
import com.alcatel_lucent.dms.model.Translation
import com.alcatel_lucent.dms.service.DaoService
import com.alcatel_lucent.dms.service.DictionaryService

/**
 * @author Guoshun.Wu
 * Date: 2012-07-22
 *
 */

//@org.junit.Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = [ "/spring.xml" ])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class GDictionaryServiceImplTest {

	private static String testFilesPathDir

	@Autowired
	private DictionaryService ds

	@Autowired
	private DaoService dao

	private static Logger log=Logger.getLogger(GDictionaryServiceImplTest.class)

	@BeforeClass
	static void setUpBeforeClass() throws Exception {
		def testFilePath = new File(GDictionaryServiceImplTest.class.getResource("/").toURI())
		testFilePath = testFilePath.parentFile.parentFile
		testFilesPathDir = "${new File(testFilePath, 'dct_test_files').absolutePath}/"
		log.info "Test file path is: $testFilesPathDir"
	}

	@AfterClass
	static void tearDownAfterClass() throws Exception{

	}

	@Before
	void setUp() throws Exception {
	
	}

	@After
	void tearDown() throws Exception {
	
	}

	@Test
	@Ignore
	void testDemo(){
		println "Hello, I am test case. $ds"
		log.info("Hello, I am a log.")

		println "Defined Bean Names(Number: $context.beanDefinitionCount) ".center(100,'*')
		context.getBeanDefinitionNames().each{ println it }
		println "Defined Bean Names End".center(100,'*')
	}

	@Test
	@Ignore
	void testSampleAbout_DCT() throws Exception {

		Long appId = 1L
		String encoding = null

		// langCharset mapping of language code and its source charset name
		Map<String, String> langCharset = [:]
		"CHK, GAE, FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0".split("\\s*,\\s*").each{
			langCharset[it]='GBK'
		}
		//		print langCharset

		// langCodes Alcatel code of languages to import, null if all languages
		// should be imported
		String[] langCodes = {"CH1"}
		langCodes = null

		String dictName = "About.dic"

		String testFile = "CH0/About.dic"
		String updatedTestFile = "CH0/About_Changed.dic"

		String testFilePath = "$testFilesPathDir$testFile"
		println testFilePath

		Collection<BusinessWarning> warnings = []

		/***************************************** Test for deliver DCT ****************************************/

		//		com.alcatel_lucent.dms.model.Dictionary dbDict = ds.deliverDCT(dictName, testFilePath, appId,
		//				encoding, langCodes, langCharset, warnings)
		def dbDict = ds.deliverDCT(dictName, testFilePath, appId,
		encoding, langCodes, langCharset, warnings)

		// dictionary check
		dbDict = dao.retrieveOne "from Dictionary where name=:name", ['name':dictName], ["labels", "dictLanguages"] as String[]
		assertThat(dbDict, is(notNullValue()))

		// dictionary language check
		HashSet dbLangCodes = dbDict.allLanguageCodes

		// CHK is not saved in database.
		List<String> filelangCodes = "FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0"
		.split(",\\s*")
		assertTrue(dbLangCodes.containsAll(filelangCodes))
		// labels check

		Context dbCtx = dao.retrieveOne "from Context where name=:name", ['name':dictName]
		assertNotNull(dbCtx)

		//				 prepare expected result data
		List<Label> validateLabels = [
			new Label(
			'dictionary':dbDict,
			'reference':'Warning: This computer program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it may result in severe civil and criminal penalties, and will be prosecuted to the maximum extent possible under the law.',
			'key':'WARNING',
			'maxLength':'399'
			),
			new Label(
			'dictionary':dbDict,
			'reference':'Copyright 2007-2012 by Alcatel-Lucent. All rights reserved.\nAlcatel-Lucent and Alcatel-Lucent Logo are respectively registered\ntrademark and service mark of Alcatel-Lucent.',
			'key':'COPYRIGHT',
			'maxLength':'79,86,97'
			),
			new Label(
			'dictionary':dbDict,
			'reference':'My Instant Communicator client software version ',
			'key':'MPC_VERSION',
			'maxLength':'57'
			)
		]

		MultiKeyMap translatedStringMap = new MultiKeyMap()
		translatedStringMap[new MultiKey('WARNING','EN0')]='Warning: This computer program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it may result in severe civil and criminal penalties, and will be prosecuted to the maximum extent possible under the law.'
		translatedStringMap[new MultiKey('WARNING','CH0')]='警告：本计算机程序受到版权法和国际条约的保护。未经授权而复制或披露本程序或其任何部分程序，可能会受到严重的民事或刑事处罚，并将依法进行起诉'
		translatedStringMap[new MultiKey('WARNING','US0')]='Warning: This program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it may result in severe civil and criminal penalties, and violators will be prosecuted to the maximum extent possible under the law.'

		translatedStringMap[new MultiKey('COPYRIGHT','EN0')]='Copyright 2007-2012 by Alcatel-Lucent. All rights reserved.\nAlcatel-Lucent and Alcatel-Lucent Logo are respectively registered\ntrademark and service mark of Alcatel-Lucent.'
		translatedStringMap[new MultiKey('COPYRIGHT','CH0')]='2007-2012年阿尔卡特朗讯版权所有。保留所有权力。\nAlcatel-Lucent与Alcatel-Lucent标识是阿尔卡特朗讯各自的注册商标和服务标记。'
		translatedStringMap[new MultiKey('COPYRIGHT','US0')]='Copyright 2007-2012 by Alcatel-Lucent. All rights reserved.\nAlcatel-Lucent and Alcatel-Lucent Logo are respectively registered\ntrademark and service mark of Alcatel-Lucent.'

		translatedStringMap[new MultiKey('MPC_VERSION','EN0')]='My Instant Communicator client software version '
		translatedStringMap[new MultiKey('MPC_VERSION','CH0')]='我的即时通客户端软件版本 '
		translatedStringMap[new MultiKey('MPC_VERSION','US0')]='My Instant Communicator client software version '


		/*
		 * check if there are ('EN0','CH0','US0') language codes related
		 * translations
		 */
		Map<String, Long> validateTranslationsLangCodeAndIDMap = [:]
		
		dao.retrieve("select al.language.id,al.code from AlcatelLanguageCode al where code in ('EN0','CH0','US0')").each{
			def idAndCode=it as Object[]
			validateTranslationsLangCodeAndIDMap[idAndCode[1]]=idAndCode[0]
		}


		validateLabels.each {label->
			Label dbLabel = dbDict.getLabel(label.key)
			assertNotNull(dbLabel)
			
			label.context=dbCtx

			assertEquals(label.reference, dbLabel.reference)
			assertEquals(label.maxLength, dbLabel.maxLength)

			Text dbText = dao .retrieveOne "from Text where reference=:reference and context.id=:contextid",
			["reference":label.reference,"contextid":label.context.id], ["translations"] as String[] 

			assertNotNull(dbText)

			Translation trans = null
			log.info "validating if there are ('EN0','CH0','US0') translation in database."

			validateTranslationsLangCodeAndIDMap.each{code, id->
				trans = dbText.getTranslation(id)
				assertNotNull "Translation item for $code not found.", trans
				log.info "label key: ${label.key}, language code: $code"
				assertThat trans.translation, equalTo(translatedStringMap[new MultiKey(label.key, code)])
			}
		}

		/***************************************** Test updated test file deliver DCT ****************************************/
		testFilePath = "$testFilesPathDir$updatedTestFile"
		// re deliver the updated DCT file
		langCharset.CH1='BIG5'

		dbDict = ds.deliverDCT dictName, testFilePath, appId, encoding, langCodes, langCharset, warnings
		// check result

		dbDict = dao.retrieveOne "from Dictionary where name=:name", ['name':dictName], ["labels", "dictLanguages"] as String[]

		// check added dictionary language
		assertThat dbDict.allLanguageCodes, hasItem("CH1")

		// check added new label TESTLABEL
		Label dbLabel = dbDict.getLabel("TESTLABEL")
		assertThat dbLabel, is(notNullValue())

		Text dbText = dao.retrieveOne "from Text where reference=:reference and context.id=:contextid",
			["reference":dbLabel.reference,"contextid":dbLabel.context.id], ["translations"] as String[]
		// check translations

		// added translation
		translatedStringMap[new MultiKey("TESTLABEL", "EN0")]= "Test"
		translatedStringMap[new MultiKey("TESTLABEL", "US0")]= "Test"
		translatedStringMap[new MultiKey("TESTLABEL", "CH0")]= "测试"


		validateTranslationsLangCodeAndIDMap.each{code, id->
			Translation trans = dbText.getTranslation(id)
			assertNotNull "Translation item for $code not found.", trans
			log.info "label key: $dbLabel.key, language code: $code"
			assertThat trans.translation, equalTo(translatedStringMap[new MultiKey(dbLabel.key,code)])
		}

		// updated translation

		translatedStringMap[new MultiKey("COPYRIGHT", "CH0")] ="用于测试的改变，2007-2012年阿尔卡特朗讯版权所有。保留所有权力\nAlcatel-Lucent与Alcatel-Lucent标识是阿尔卡特朗讯各自的注册商标和服务标记。"

		dbLabel = dbDict.getLabel("COPYRIGHT")
		dbText = dao.retrieveOne "from Text where reference=:reference and context.id=:contextid",
		["reference":dbLabel.reference,"contextid":dbLabel.context.id], ["translations"] as String[] 

		Translation trans = dbText.getTranslation validateTranslationsLangCodeAndIDMap["CH0"]

		assertThat trans.translation, equalTo(translatedStringMap.get(dbLabel.key, "CH0"))

		/*************************** Test generate dct file from dictionary in database *************************/
		String targetFileName = "target/${dictName}_generated.dct"
		ds.generateDCT targetFileName, dbDict.getId(), encoding, langCodes,langCharset
		def generatedFile = new File(targetFileName)
		assertTrue "Dictionary $generatedFile.name is not generated.", generatedFile.exists()

		/*************************** Test deletel dictionary in database *************************/
		ds.deleteDCT dictName
		def origDict = dbDict 		
		dbDict = dao.retrieveOne "from Dictionary where name=:name", ['name':dictName], ["labels", "dictLanguages"] as String[]

		// check dictionary
		assertThat dbDict, is(nullValue())

		// check labels
		List<Label> labels = dao.retrieve "from Label where dictionary.id=:dictId", ['dictId':origDict.id]
		assertTrue "Some label(s): $labels in $origDict.name dictionary is(are) not deleted.", labels.isEmpty()
	}

	
	@Test
//	@Ignore
	public void testDeliverDCTFiles() {
		Long appId = 1L
		String encoding = null
	
		// langCodes Alcatel code of languages to import, null if all languages
		// should be imported
		String[] langCodes = ["AR0", "ar"]
		// langCodes = null

		String rootDir = "Z:/EN-UK"
//		rootDir = "D:/tmp/AR"
		
		String testFilePath = rootDir
//		testFilePath="$rootDir/6.6.000.107.a/data_access_service/dataaccess/WEB-INF/classes/com/alcatel/dataaccess/global/dico/DtaEccServer.dct"

		Collection<BusinessWarning> warnings = []
		
		String header=String.format("%s, %s, %s, %s", "Name", "encoding","Path","cause")
		logDictDeliverFail.info header
		DictDeliverWarning.info header
		
		long before=System.currentTimeMillis() 
		def dicts = ds.deliverDCTFiles rootDir, new File(testFilePath), appId, encoding, langCodes, null, warnings
		long after=System.currentTimeMillis() 
		log.info "Using a total of ${after-before} millisecond to perform delivering."
		
		assertThat dicts, is(notNullValue())
//		output all the warnings
		
//		warnings.each {warning->
//			logWarning.warn(warning)
//		}
//		warnings.info("Sum of warnning: $warning.size()")
		
		String format = "%s, %s, %s, %s"
		logDictDeliverSuccess.info String.format(format, "ID", "name", "encoding", "path")
		
		
		dicts.each{dict->
			logDictDeliverSuccess.info String.format(format, dict.id,dict.name, dict.encoding, dict.path)
		}
		println()
	}
}
