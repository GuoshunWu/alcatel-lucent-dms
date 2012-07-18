package com.alcatel_lucent.dms.test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "/spring.xml" })
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
	// @Ignore
	public void testMe() throws Exception {
		File file = new File("D:/tmp/TestFile.txt");
		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();

		System.out.println("channel size: " + channel.size());
		System.out.println("current position: " + channel.position());
		ByteBuffer dst = ByteBuffer.allocate(12);

		channel.read(dst, 5);
		System.out.println("dst content=" + new String(dst.array(), "GBK"));
		System.out.println("current position: " + channel.position());

		ByteBuffer dst1 = ByteBuffer.allocate(3);
		channel.read(dst1, 0);
		System.out.println("dst content=" + new String(dst1.array(), "GBK"));
		System.out.println("current position: " + channel.position());

		System.out.println(ByteOrder.BIG_ENDIAN);

		channel.close();
		fis.close();
	}

}
