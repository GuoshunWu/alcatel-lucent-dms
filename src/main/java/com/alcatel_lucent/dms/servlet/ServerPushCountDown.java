package com.alcatel_lucent.dms.servlet;

import net.sf.json.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static java.lang.Thread.sleep;

/**
 * Created by guoshunw on 13-12-27.
 *
 * This is an experimental feature, test showed that only worked on firefox
 */
@WebServlet(name = "serverPush", urlPatterns = {"/test/serverPush"})
public class ServerPushCountDown extends HttpServlet {

    private static final String randString = RandomStringUtils.randomAlphanumeric(10);
    private static final String boundary = "--" + randString;
    private static final String responseEnd = String.format("--%s--", randString);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //Set things up
        PrintWriter out = resp.getWriter();

        resp.setContentType(String.format("multipart/x-mixed-replace;boundary=%s", randString));
        out.println();

        for (int i = 10; i > 0; i--) {
            newResponse(out, "application/json");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        out.println(responseEnd);
        System.out.println("Response closed..");
        out.close();
    }

    private void newResponse(PrintWriter out, String contentType) {
//        end the last response
        out.println(boundary);
        out.flush();

        // start a new response here
        out.println("Content-Type: " + contentType);
        out.println();
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("timeStamp", System.currentTimeMillis());
        jsonObject.put("message", RandomStringUtils.randomAlphanumeric(20));
        out.println(jsonObject.toString(2));
    }

    @Override
    public void destroy() {
        super.destroy();
        System.out.println("Servlet push servlet destroy.");
    }

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("Servlet push servlet init.");
    }
}
