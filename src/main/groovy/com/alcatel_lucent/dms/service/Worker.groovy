package com.alcatel_lucent.dms.service

import groovy.json.JsonBuilder
import groovy.json.JsonOutput

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-2-1
 * Time: 下午2:09
 * To change this template use File | Settings | File Templates.
 */
class Worker implements Runnable {
    private BlockingQueue<String> events;

    Worker(BlockingQueue events) {
        this.events = events
    }


    private simulateProcess() {
        Random r = new Random()
        int fileSize = 10000

        int currentProgress = 0
        float percent

        while (currentProgress < fileSize) {
            percent = (float) currentProgress / fileSize * 100
            events.put String.format("%05.2f%%", percent)
            currentProgress += r.nextInt(1000)
            Thread.sleep(r.nextInt(2000))
        }
        events.put String.format("%05.2f%%", 100f)
        events.put "done."
    }

    @Override
    void run() throws Exception {
        println "Worker thread ${Thread.currentThread().getName()} start."
        simulateProcess()
        println "Worker thread ${Thread.currentThread().getName()} exit."
    }

    static void simulateServletResponse() {
        BlockingQueue<String> events = new LinkedBlockingQueue<>();
        new Thread(new Worker(events), "upload").start()

        String msg = "start"
        while (msg = events.take()) {
            println "Got msg in thread ${Thread.currentThread().getName()}, msg: ${msg}"
            if (msg.startsWith("done")) {
                break
            }
        }
    }

    static void main(String[] args) {
        println "Servlet thread ${Thread.currentThread().getName()} start."
//        simulateServletResponse()
        println "Servlet thread ${Thread.currentThread().getName()} exit."

        def builder = new JsonBuilder()
        builder {
            msg 'zero'
        }
        builder.content.bb='cc'
        println builder.toPrettyString()
    }
}
