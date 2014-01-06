package com.alcatel_lucent.dms.servlet;

import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: Guoshun.Wu
 * Date: 13-1-28
 * Time: 下午9:45
 */
@WebServlet(name = "events", urlPatterns = {"/test/cat-events"}, asyncSupported = true)
public class TomcatCometServlet extends HttpServlet implements CometProcessor {

    private static Logger log = LoggerFactory.getLogger(TomcatCometServlet.class);
    protected List<HttpServletResponse> connections = new ArrayList<HttpServletResponse>();
    protected MessageSender messageSender;

    @Override
    public void destroy() {
        connections.clear();
        messageSender.stop();
        messageSender = null;
    }

    @Override
    public void init() throws ServletException {
        messageSender = new MessageSender();
        Thread messageSenderThread = new Thread(messageSender, String.format("MessageSender[%s]", getServletContext().getContextPath()));
        messageSenderThread.setDaemon(true);
        messageSenderThread.start();
    }

    /**
     * Process the given Comet event.
     *
     * @param cometEvent The Comet event that will be processed
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void event(CometEvent cometEvent) throws IOException, ServletException {
        HttpServletRequest request = cometEvent.getHttpServletRequest();
        HttpServletResponse response = cometEvent.getHttpServletResponse();
        String sessionId;
        PrintWriter out;

        switch (cometEvent.getEventType()) {
            case BEGIN:
                sessionId = request.getSession().getId();
                out = response.getWriter();

                log.info("Begin for session: {}", sessionId);
                out.println("<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">");
                out.println("<head><title>JSP Chat</title></head><body bgcolor=\"#FFFFFF\">");
                out.flush();
                synchronized (connections) {
                    connections.add(response);
                }
                break;
            case ERROR:
                sessionId = request.getSession().getId();
                log.info("Error for session: {}", sessionId);
                synchronized (connections) {
                    connections.remove(response);
                }
                cometEvent.close();
                break;
            case END:
                sessionId = request.getSession().getId();
                out = response.getWriter();
                log.info("End for session: {}", sessionId);
                synchronized (connections) {
                    connections.remove(response);
                }
                out.println("</body></html>");
                cometEvent.close();
                break;
            case READ:
                InputStream is = request.getInputStream();
                byte[] buf = new byte[512];

                do {
                    int n = is.read(buf);
                    if (n < 0) {
                        log.warn("Error reading...");
//                        error(cometEvent, request, response);
                        return;
                    }
                    log.info("Read {} bytes: {}", n, new String(buf));
                } while (is.available() > 0);
                break;
            default:
                break;
        }

//        response.setContentType("application/json");
//        response.getWriter().println(JSONObject.fromObject("{'msg': '" + DateFormat.getDateTimeInstance().format(new Date()) + "', 'status': -1}"));
//        cometEvent.close();
    }
}

class MessageSender implements Runnable {

    protected boolean running = true;
    protected ArrayList<String> messages = new ArrayList<String>();

    public void stop() {
        running = false;
    }

    /**
     * Add message for sending.
     */

    public void send(String user, String message) {
        synchronized (message) {
            messages.add(String.format("[%s]: %s", user, message));
            message.notify();
        }
    }

    @Override
    public void run() {
        while (running) {
            if (messages.isEmpty()) {
                try {
                    synchronized (messages) {
                        messages.wait();
                    }
                } catch (InterruptedException e) {
                    //Ignore
                }
            }

//            synchronized (connections) {
//                String[] pendingMessages = null;
//                synchronized (messages) {
//                    pendingMessages = messages.toArray(new String[0]);
//                    messages.clear();
//                }

                // sending any pending message on all the open connections

//                for(HttpServletResponse connection: connections){
//
//                }
            }
//        }
    }
}
