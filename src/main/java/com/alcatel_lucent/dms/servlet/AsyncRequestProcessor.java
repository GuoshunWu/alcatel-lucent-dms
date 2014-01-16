package com.alcatel_lucent.dms.servlet;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private HttpServletRequest request;
    private HttpServletResponse response;


    public AsyncRequestProcessor(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
        this.request = (HttpServletRequest) asyncContext.getRequest();
        this.response = (HttpServletResponse) asyncContext.getResponse();
    }

    @Override
    public void run() {
        log.info("Async Supported? {}", asyncContext.getRequest().isAsyncStarted());
        try {
            streamOutput();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //complete the processing
        asyncContext.complete();
    }

    private void streamOutput() throws IOException, InterruptedException {
        response.setContentType("json/application");
//        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        while (true) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("name", "DMS server push channel.");
            jsonObj.put("timestamp", dFmt.format(new Date()));
            out.println(jsonObj.toString(2));
            log.info("Thread: {} execute, time: {}", Thread.currentThread().getName(), dFmt.format(new Date()));
            out.flush();
            Thread.sleep(1000);
        }
    }
}
