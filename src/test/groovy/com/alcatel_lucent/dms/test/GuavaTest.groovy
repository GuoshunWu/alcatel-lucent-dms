package com.alcatel_lucent.dms.test

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListenableFutureTask
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import org.junit.Test

import javax.annotation.Nullable
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
}
