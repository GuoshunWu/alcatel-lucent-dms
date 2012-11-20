package com.alcatel_lucent.dms;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-11-18
 * Time: 下午7:42
 * To change this template use File | Settings | File Templates.
 */
public class ProfileConfigTest {
    
    @Test
    public void testConfig() throws IOException {
        InputStream in = ClassLoader.getSystemResourceAsStream("proxool.properties");
        Properties p = new Properties();
        p.load(in);

        String dbDriver = (String) p.get("jdbc.proxool.driver-class");
        String dburl = (String) p.get("jdbc.proxool.driver-url");
        String dbuser = (String) p.get("jdbc.user");
        String dbpassword = (String) p.get("jdbc.password");

        System.out.println(dbDriver);
        System.out.println(dburl);
        System.out.println(dbuser);
        System.out.println(dbpassword);
    }
}
