package com.alcatel_lucent.dms.service

import groovy.swing.SwingBuilder

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-2-4
 * Time: 下午9:55
 * To change this template use File | Settings | File Templates.
 */
class ProducerFrame {
    private BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<String>(10)

    private void createFrame() {
        SwingBuilder swingBuilder = new SwingBuilder()
        swingBuilder.frame(title: "Event Producer",
                defaultCloseOperation: JFrame.EXIT_ON_CLOSE,
                size: [400, 300],
                show: true,
                locationRelativeTo: null,
                layout: new BorderLayout()
        ) {
            button("Send Event", actionPerformed: { ActionEvent e ->
                String event = randomAlphanumeric(20)
//                blockingQueue.put(event)
                blockingQueue.offer(event)

                println "Add event to queue, '$event', queue =$blockingQueue."
            })
        }
    }

    static void main(String... args) throws Exception {
        ProducerFrame me = new ProducerFrame()
        me.createFrame()
        println "=" * 60

        while (true) {
            Thread.sleep(1000)
            String event = me.blockingQueue.take()
//            String event = me.blockingQueue.poll()
            if (null != event) {
                println "I am thread ${Thread.currentThread().name}, I got a msg '${event}' from queue."
            }else{
                println "I am tired with waiting, but there still is no event..."
            }
        }
        println "${Thread.currentThread().name} exit."

    }
}
