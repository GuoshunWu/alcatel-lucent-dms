package com.alcatel_lucent.dms.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by guoshunw on 2014/3/21.
 */

@WebServlet(name = "TestServlet", urlPatterns = {"/test/testServlet"}, asyncSupported = true)
@MultipartConfig(location = "/tmp/upload", fileSizeThreshold = 2 * 1024 * 1024)
public class TestServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain;charset=utf-8");
        PrintWriter out = response.getWriter();
        Part part = request.getPart("file");
    }
}
