package com.alcatel_lucent.dms;


import com.alcatel_lucent.dms.service.parser.NOEStrParser;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-11-18
 * Time: 下午7:42
 * To change this template use File | Settings | File Templates.
 */
@Ignore
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
    public void testMultiMap() {
        MultiValueMap<String, String> test = new LinkedMultiValueMap<String, String>();
        test.add("AA", "BB");
        System.out.println(test.get("AA"));
    }

    //    @Test
    public void testJSEngine() throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        System.out.println("Test begin...");
        testUsingJDKClasses(engine);//演示脚本语言如何使用JDK平台下的类
    }

    private void testUsingJDKClasses(ScriptEngine engine) throws ScriptException, NoSuchMethodException {
        //Packages是脚本语言里的一个全局变量,专用于访问JDK的package
        String js = "function doSwing(t){" +
                "   var f=new Packages.javax.swing.JFrame(t);" +
                "       f.setSize(400,300);" +
                "       f.setVisible(true);" +
                "}";
        engine.eval(js);
        //Invocable 接口: 允许java平台调用脚本程序中的函数或方法
        Invocable inv = (Invocable) engine;
        //invokeFunction()中的第一个参数就是被调用的脚本程序中的函数，第二个参数是传递给被调用函数的参数；
        inv.invokeFunction("doSwing", "Scripting Swing");
    }

    //    @Test
    public void testEncoding() throws UnsupportedEncodingException, CharacterCodingException {

        String charset = "windows-1252";
        ByteBuffer buffer = ByteBuffer.allocate(255);
        buffer.put((byte) 0x82);
        buffer.put((byte) 0x70);
        buffer.flip();

        CharBuffer cb = Charset.forName(charset).decode(buffer);
        System.out.println(cb.toString());

    }

    //    @Test
    public void testAccent() throws Exception {
//        MapUtils.debugPrint(System.out, "ESCAPE_SEARCH_MAP", ESCAPE_SEARCH_MAP);
        String filePath = "D:\\360CloudUI\\Cache\\45698397\\Documents\\Alcatel-Lucent\\DMS\\DMSFiles\\TestFile.txt";
        String str = FileUtils.readFileToString(new File(filePath), "GBK");
        System.out.println("Original: " + str);
        System.out.print("Translated: ");
        System.out.println(NOEStrParser.unescapeNOEString(str));

    }

    @Test
    public void testGuava() throws InterruptedException {
    }

}
