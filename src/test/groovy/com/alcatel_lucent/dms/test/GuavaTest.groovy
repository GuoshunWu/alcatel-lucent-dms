package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.BusinessWarning
import com.alcatel_lucent.dms.service.parser.NOEStrParser
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.base.Strings
import com.google.common.util.concurrent.*
import org.junit.Test

import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.Executors

//import org.glassfish.tyrus.client.ClientManager
//import org.glassfish.tyrus.server.Server
/**
 * Created by guoshunw on 2014/3/31.
 */
class GuavaTest {

//    @Test
    void testListenableFuture() {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10))
        ListenableFuture<String> explosion = service.submit({
            return "I am OK."
        } as Callable<String>)

        Futures.addCallback(explosion, [
                onSuccess: { String result ->
                    println "Call success, got result $result"
                },
                onFailure: { Throwable t ->
                    println "Call fail, got result $result"
                }
        ] as FutureCallback)

        ListenableFutureTask lft = ListenableFutureTask.create({
            return "Future task"
        } as Callable<String>)
    }

//    @Test
    void testNOEStringEscape() {
        String orig = "中\\Hc7\\Hc8Hello \\a,c"
        String unEsc = NOEStrParser.unescapeNOEString(orig)
        String esc = NOEStrParser.escapeNOEString(unEsc)

        println "unEsc = $unEsc, esc = $esc"

        println NOEStrParser.escapeNOEString("\u00b0")
        println "\u00b0"
        println NOEStrParser.unescapeNOEString("\\a_o")

    }


    @Test
    void testJsonArray() {
        List<Object> temp = Arrays.asList("测试2", "Study", "How", 123, new BusinessWarning(BusinessWarning.DUPLICATE_LABEL_KEY, 357, "aaa"))

        JsonNodeFactory factory = new JsonNodeFactory(false);

        // create a json factory to write the treenode as json. for the example
        // we just write to console
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();

        // the root node - album
        ObjectNode album = factory.objectNode();
        album.putPOJO("tea", temp)
        println mapper.writeValueAsString(temp)

    }

//    @Test
    void testJackson() {
        String domain = "AD4"
        String user = "guoshunw"
        String password = ""
        String strUrl = "http://www.163.com"

        setProxy("151.98.66.13", "8000", user, password, domain)
//        setProxy("135.251.33.16", "8080", user, password, domain)

        URL url = new URL(strUrl)
        String contentType = url.openConnection().getHeaderField("Content-Type")
        String charset = Charset.defaultCharset().name()
        int charsetPos
        if (!Strings.isNullOrEmpty(contentType) && -1 != (charsetPos = contentType.indexOf("charset"))) {
            charset = contentType.substring(charsetPos).split("=")[1]
        }
        url.readLines(charset).each { String line ->
            println line
        }
    }

    private void setProxy(String host, String port, String user = "", String password = "", String domain = "") {
        System.setProperty("proxySet", "true")
        System.setProperty("http.proxyHost", host)
        System.setProperty("http.proxyPort", port)

        System.setProperty("https.proxyHost", host)
        System.setProperty("https.proxyPort", port)
        if (password.empty) {
            return
        }

        Authenticator.default = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (!domain.empty) user = "${domain}//${user}"
                return new PasswordAuthentication(user, password.toCharArray())
            }
        }

    }
}
