package com.alcatel_lucent.dms.service

import groovy.json.JsonBuilder

import java.util.concurrent.BlockingQueue

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-2-1
 * Time: 下午2:09
 * To change this template use File | Settings | File Templates.
 */
class Worker implements Runnable {
    private BlockingQueue<String> events
    //  The worker speed
    private int speed
    // The event generate frequency
    private int frequency

    Worker(BlockingQueue events, int speed = 1000, int frequency = 2000) {
        this.events = events
        this.speed = speed
        this.frequency = frequency
    }


    private simulateProcess() throws Exception {
        Random r = new Random(System.currentTimeMillis())
        int fileSize = 10000

        int currentProgress = 0
        float percent

        while (currentProgress < fileSize) {
            percent = (float) currentProgress / fileSize * 100
            events.put String.format("%05.2f", percent)
            currentProgress += r.nextInt(speed)
            Thread.sleep(r.nextInt(frequency))
        }
        events.put String.format("%05.2f", 100f)
        events.put "done."
    }

    @Override
    void run() {
        println "Worker thread [${Thread.currentThread().getName()}] start, speed=${speed}, frequency=${frequency}."
        try {
            simulateProcess()
        } catch (Exception e) {
            println e.message
        }

        println "Worker thread [${Thread.currentThread().getName()}] exit."
    }


    static void main(String[] args) {

        def builder = new JsonBuilder()


        builder {
            test 'sdfsdf'
        }
        builder.content.bb = 'cc'
        println builder.toPrettyString()

    }
}
