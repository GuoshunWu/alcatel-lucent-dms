package com.alcatel_lucent.dms.servlet;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by IntelliJ IDEA.
 * User: Guoshun.Wu
 * Date: 13-1-28
 * Time: 下午9:45
 */
@WebServlet(name = "myevents", urlPatterns = {"/test/myevents"}, asyncSupported = true)
public class MyReverseAjaxServlet extends HttpServlet {

    private final Queue<AsyncContext> asyncContexts = new ConcurrentLinkedQueue<AsyncContext>();
    private final String boundary = "ABCDEFGHIJKLMNOPQRST"; // generated
    private final Random random = new Random();

    private Logger log = LoggerFactory.getLogger(MyReverseAjaxServlet.class);

    private final Thread generator = new Thread("Event generator") {
        @Override
        public void run() {
            log.info("Comet long polling servlet event generator start...");

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(random.nextInt(5000));

                    while (!asyncContexts.isEmpty()) {
                        AsyncContext asyncContext = asyncContexts.poll();
                        HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
                        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
                        log.info("=========A response to " + request.getRemoteAddr() + "==========");

                        response.setContentType("application/json");

                        Map info = new HashMap<String, String>();
                        info.put("msg", DateFormat.getDateTimeInstance().format(new Date()));
                        info.put("status", 0);
                        info.put("client", request.getRemoteAddr());

                        response.getWriter().println(JSONObject.fromObject(info));
                        response.setStatus(HttpServletResponse.SC_OK);

                        asyncContext.complete();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            log.info("Comet long polling servlet event generator stop.");
        }
    };

    @Override
    public void init() throws ServletException {
        generator.start();
    }

    @Override
    public void destroy() {
        generator.interrupt();
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!request.isAsyncSupported()) {
            response.setContentType("application/json");
            log.warn("Asynchronous mode is not supported.");
            generator.interrupt();
            response.getWriter().println(JSONObject.fromObject("{'msg': 'Asynchronous mode is not supported by this web container', 'status': -1}"));
            response.flushBuffer();
            return;
        }
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0);
        asyncContexts.offer(asyncContext);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
