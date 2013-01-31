package com.alcatel_lucent.dms.servlet;

import net.sf.json.JSONObject;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometProcessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Guoshun.Wu
 * Date: 13-1-28
 * Time: 下午9:45
 */
@WebServlet(name = "events", urlPatterns = {"/test/cat-events"}, asyncSupported = true)
public class TomcatCometServlet extends HttpServlet implements CometProcessor {
    @Override
    public void event(CometEvent cometEvent) throws IOException, ServletException {
        HttpServletRequest request = cometEvent.getHttpServletRequest();
        HttpServletResponse response = cometEvent.getHttpServletResponse();
        response.setContentType("application/json");
        response.getWriter().println(JSONObject.fromObject("{'msg': '" + DateFormat.getDateTimeInstance().format(new Date()) + "', 'status': -1}"));
        cometEvent.close();
    }
}
