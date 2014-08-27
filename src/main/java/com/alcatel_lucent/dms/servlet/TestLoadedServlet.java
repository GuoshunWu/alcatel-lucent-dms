package com.alcatel_lucent.dms.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by guoshunw on 2014/5/6.
 */

@WebServlet(name = "loadServlet", urlPatterns = {"/test/loaded"})
public class TestLoadedServlet extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(TestLoadedServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        out.println("Hello, world 12345.");

    }
}
