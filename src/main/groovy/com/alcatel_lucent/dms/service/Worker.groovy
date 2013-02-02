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
    private BlockingQueue<String> events;

    Worker(BlockingQueue events) {
        this.events = events
    }


    private simulateProcess() throws Exception{
        Random r = new Random(System.currentTimeMillis())
        int fileSize = 10000

        int currentProgress = 0
        float percent

        while (currentProgress < fileSize) {
            percent = (float) currentProgress / fileSize * 100
            events.put String.format("%05.2f", percent)
            currentProgress += r.nextInt(1000)
            Thread.sleep(r.nextInt(2000))
        }
        events.put String.format("%05.2f", 100f)
        events.put "done."
    }

    @Override
    void run() {
        println "Worker thread [${Thread.currentThread().getName()}] start."
        try{
            simulateProcess()
        }catch(Exception e){
            println e.message
        }

        /**
         * TODO: Maybe we should clear remove this event from session when this task is done.
         * */
        println "Worker thread [${Thread.currentThread().getName()}] exit."
    }


    static void main(String[] args) {
        def builder = new JsonBuilder()
        String t="asdfsdfs"

        builder {
            msg 'zero'
        }
        builder{
            test t
        }
        builder.content.bb='cc'
        println builder.toPrettyString()
    }
}
