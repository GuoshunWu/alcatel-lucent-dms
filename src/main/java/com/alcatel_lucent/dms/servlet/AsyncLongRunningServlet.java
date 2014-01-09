package com.alcatel_lucent.dms.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: Guoshun.Wu
 * Date: 13-1-28
 * Time: 下午9:45
 */
@WebServlet(name = "AsyncLongRunningServlet", urlPatterns = {"/test/AsyncLongRunningServlet"}, asyncSupported = true)
public class AsyncLongRunningServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AsyncLongRunningServlet.class);

    private static final SimpleDateFormat dFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    private ExecutorService pool;

    @Override
    public void init() throws ServletException {
        pool = Executors.newFixedThreadPool(100);
    }

    @Override
    public void destroy() {
        pool.shutdown();
        pool = null;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        Thread currentThread = Thread.currentThread();
        log.info("AsyncLongRunningServlet Start::Name={}::ID={} Start at {}",
                currentThread.getName(), currentThread.getId(), dFmt.format(new Date()));
        req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);

        String time = req.getParameter("time");
        if (null == time) time = "5000";
        int secs = Integer.parseInt(time);

        // max 10 seconds
        if (secs > 10000) secs = 10000;

        AsyncContext asyncCtx = req.startAsync();
        asyncCtx.addListener(new AppAsyncListener());
        asyncCtx.setTimeout(9000);

        pool.execute(new AsyncRequestProcessor(asyncCtx, secs));
        long endTime = System.currentTimeMillis();
        log.info("AsyncLongRunningServlet End::Name={}::ID={}::Time Taken={} End at {}",
                currentThread.getName(), currentThread.getId(), endTime - startTime, dFmt.format(new Date()));

    }
}
