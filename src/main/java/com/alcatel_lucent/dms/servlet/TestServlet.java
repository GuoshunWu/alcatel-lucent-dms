package com.alcatel_lucent.dms.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 上午10:07
 * To change this template use File | Settings | File Templates.
 */
public class TestServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String springConfigLoc=getServletContext().getInitParameter("contextConfigLocation");
        response.setContentType("text/plain;charset=utf8");
        PrintWriter out=response.getWriter();
        out.println("Spring Config loc1: " + springConfigLoc);
        out.println("Why abc?");
        out.flush();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request,response);
    }
}
