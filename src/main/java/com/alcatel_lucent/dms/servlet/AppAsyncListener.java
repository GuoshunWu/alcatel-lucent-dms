package com.alcatel_lucent.dms.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by guoshunw on 14-1-7.
 */
public class AppAsyncListener implements AsyncListener {
    private static final Logger log = LoggerFactory.getLogger(AsyncRequestProcessor.class);

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        log.info("AppAsyncListener onComplete");
        // we can do resource cleanup activity here
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        log.info("AppAsyncListener onTimeout");
        //we can send appropriate response to client
        ServletResponse response = event.getAsyncContext().getResponse();
        PrintWriter out = response.getWriter();
        out.println("TimeOut Error in Processing");
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        log.info("AppAsyncListener onError");
        //we can return error response to client
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        log.info("AppAsyncListener onStartAsync");
        //we can log the event here
    }
}
