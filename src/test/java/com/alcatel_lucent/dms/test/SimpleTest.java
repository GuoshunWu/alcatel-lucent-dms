package com.alcatel_lucent.dms.test;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring.xml" })
public class SimpleTest {

	private static Logger log = Logger.getLogger(SimpleTest.class);

	@Autowired
	// private DictionaryService ds;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log.info("Let's begin.");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		log.info("I am going to shutdown.");
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
//	@Ignore
	public void testMe(){
		MultiKeyMap translatedStringMap=new MultiKeyMap();
		translatedStringMap.put("China","Gansu","LanZhou");
		translatedStringMap.put("China","Qinhai","Xining");
		translatedStringMap.put("China","Shannxi","XiAn");
		
		System.out.println(translatedStringMap.get("China", "Shannxi"));
	}

	
}
