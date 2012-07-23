package com.alcatel_lucent.dms.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alcatel_lucent.dms.util.Util;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring.xml" })
public class SimpleTest {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(SimpleTest.class);
	
	@SuppressWarnings("unused")
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	// private DictionaryService ds;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// log.info("Let's begin.");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// log.info("I am going to shutdown.");
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Ignore
	public void testMe() throws Exception {

		File file = new File("D:/tmp/TestFile.txt");
		System.out.println(file.getName());
		System.out.println(file.getAbsolutePath());
		System.out.println(file.getCanonicalPath());
		System.out.println(file.getPath());
		System.out.println(file.getParent());

		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();
		// String encoding = "UTF-16";
		// encoding = "UTF-8";
		// Charset charset = Charset.forName(encoding);

		// BufferedReader br = new BuD:fferedReader(new InputStreamReader(
		// new FileInputStream("/tmp/TestFile.txt"), charset));
		//
		// String line = br.readLine();
		// System.out.println(line);

		BufferedInputStream imp = new BufferedInputStream(new FileInputStream(
				"/tmp/TestFile.txt"));
		System.out.println(Util.detectEncoding(imp));

		// System.out.println("channel size: " + channel.size());
		// System.out.println("current position: " + channel.position());
		// ByteBuffer dst = ByteBuffer.allocate(12);
		//
		// channel.read(dst, 5);
		// System.out.println("dst content=" + new String(dst.array(), "GBK"));
		// System.out.println("current position: " + channel.position());
		//
		// ByteBuffer dst1 = ByteBuffer.allocate(3);
		// channel.read(dst1, 0);
		// System.out.println("dst content=" + new String(dst1.array(), "GBK"));
		// System.out.println("current position: " + channel.position());
		//
		// System.out.println(ByteOrder.BIG_ENDIAN);

		// br.close();

		channel.close();
		fis.close();
	}

	@Test
	public void testGroovy() {
		// GroovyTest g1 = new GroovyTest();
		// System.out.println(g1.getName());
		// System.out.println(g1.getTemp().get("age"));
	}

}
