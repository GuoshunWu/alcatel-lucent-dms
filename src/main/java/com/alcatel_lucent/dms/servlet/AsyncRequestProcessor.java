package com.alcatel_lucent.dms.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by guoshunw on 14-1-7.
 */
public class AsyncRequestProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(AsyncRequestProcessor.class);
    private static final SimpleDateFormat dFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private AsyncContext asyncContext;
    private int secs;

    public AsyncRequestProcessor(AsyncContext asyncContext, int secs) {
        this.asyncContext = asyncContext;
        this.secs = secs;
    }

    @Override
    public void run() {
        log.info("Async Supported? {}", asyncContext.getRequest().isAsyncStarted());
        longProcessing(secs);
        try {
            PrintWriter out = asyncContext.getResponse().getWriter();
            out.printf("Processing done for %d milliseconds at %s !!", secs, dFmt.format(new Date()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //complete the processing
        asyncContext.complete();
    }

    private void longProcessing(int secs) {
        //wait for given time before finishing
        try {
            Thread.sleep(secs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
