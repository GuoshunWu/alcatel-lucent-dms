package com.alcatel_lucent.dms;


import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-11-18
 * Time: 下午7:42
 * To change this template use File | Settings | File Templates.
 */
//@Ignore
public class ProfileConfigTest {
    
//    @Test
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

//    @Test
    public void testMultiMap(){
        MultiValueMap<String,String> test= new LinkedMultiValueMap<String,String>();
        test.add("AA", "BB");
//        test.add("AA", "AB");
//        test.add("AA", "AB");


        System.out.println(test.get("AA"));


    }

    @Test
    public void testIOUtil(){
        File f=new File("D:\\360CloudUI\\Cache\\45698397\\Documents");
        Collection<File> files= FileUtils.listFiles(f, null, true);

        for(File file: files){
            System.out.println(file);
        }

    }
}
