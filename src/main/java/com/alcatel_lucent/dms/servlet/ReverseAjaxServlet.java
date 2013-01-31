package com.alcatel_lucent.dms.servlet;

import net.sf.json.JSONObject;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by IntelliJ IDEA.
 * User: Guoshun.Wu
 * Date: 13-1-28
 * Time: 下午9:45
 */
@WebServlet(name = "events", urlPatterns = {"/test/events"}, asyncSupported = true)
public class ReverseAjaxServlet extends HttpServlet {

    private final Queue<AsyncContext> asyncContexts = new ConcurrentLinkedQueue<AsyncContext>();
    private final String boundary = "ABCDEFGHIJKLMNOPQRST"; // generated
    private final Random random = new Random();

    private final Thread generator = new Thread("Event generator") {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(random.nextInt(5000));

                    while (!asyncContexts.isEmpty()) {
                        AsyncContext asyncContext = asyncContexts.poll();
                        HttpServletResponse peer = (HttpServletResponse) asyncContext.getResponse();
                        peer.getWriter().println(JSONObject.fromObject("{'msg': '" + DateFormat.getDateTimeInstance().format(new Date()) + "', 'status': -1}"));
                        peer.setStatus(HttpServletResponse.SC_OK);
                        peer.setContentType("application/json");
                        asyncContext.complete();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    };

    @Override
    public void init() throws ServletException {
        System.out.println("Comet long polling servlet event generator start...");
        generator.start();
    }

    @Override
    public void destroy() {
        generator.interrupt();
        System.out.println("Comet long polling servlet event generator stop...");

    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!request.isAsyncSupported()) {
            response.setContentType("application/json");
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
