package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.service.parser.NOEStrParser
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.google.common.util.concurrent.*
import net.sf.json.JSON
import net.sf.json.JSONSerializer
import org.apache.commons.lang.StringEscapeUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.junit.Test

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

//    @Test
    void microsoftTranslatorTest() {
        String url = "http://api.microsofttranslator.com/v2/ajax.svc/TranslateArray2"
        HttpHost proxy = new HttpHost("151.98.66.13", 8000, "http")
        CloseableHttpClient httpClient = HttpClients.custom().setProxy(proxy).build()

        HttpUriRequest translator = RequestBuilder.get().setUri(url)
                .addParameter("appId", "TpxSjpehBaPQKLW1j4Gt3atp0Lo1Uvn4K5FqKo6MY5E0_gF6oy2HMwLrcL-TGHoME")
                .addParameter("texts", "[\"hello\", \"world\"]")
                .addParameter("from", "")
                .addParameter("to", "zh-CHS")
                .addParameter("options", "{}")
                .addParameter("_", System.currentTimeMillis() + "")
                .build()

        HttpResponse httpResponse = httpClient.execute(translator)
        HttpEntity entity = httpResponse.getEntity()
        if (null != entity) {
            println EntityUtils.toString(entity, "GBK")
        }
        httpResponse.close()

        httpClient.close()
    }

//    @Test
    void testJsonArray() {
        String str1 = "[<timestamp>]"
        String cStr1 = StringEscapeUtils.escapeHtml(str1.toString())

        println "str1=$str1"
        println "cStr1=$cStr1"
        JSON js = JSONSerializer.toJSON(cStr1)
        println js.toString(4)
    }

//    @Test
    void testJackson() {
        String str1 = "\"[<timestamp>]"
        String cStr1 = StringEscapeUtils.escapeHtml(str1.toString())

        // Create the node factory that gives us nodes.
        JsonNodeFactory factory = new JsonNodeFactory(false);

        // create a json factory to write the treenode as json. for the example
        // we just write to console
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();

        // the root node - album
        ArrayNode links = factory.arrayNode()

        links.add(cStr1)
        JsonGenerator generator = jsonFactory.createGenerator(System.out);
        mapper.writeTree(generator, links)
        println mapper.writeValueAsString(links)
    }

}
