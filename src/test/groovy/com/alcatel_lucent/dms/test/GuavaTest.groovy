package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.service.parser.NOEStrParser
import com.google.common.util.concurrent.*
import org.junit.Test

//import org.glassfish.tyrus.client.ClientManager
//import org.glassfish.tyrus.server.Server
import java.util.concurrent.Callable
import java.util.concurrent.Executors

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

    @Test

    void testNOEStringEscape(){
        String orig = "中\\Hc7\\Hc8Hello \\a,c"
        String unEsc = NOEStrParser.unescapeNOEString(orig)
        String esc = NOEStrParser.escapeNOEString(unEsc)

        println "unEsc = $unEsc, esc = $esc"

        println NOEStrParser.escapeNOEString("\u00b0")
        println "\u00b0"
        println NOEStrParser.unescapeNOEString("\\a_o")

    }

}
