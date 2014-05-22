package com.alcatel_lucent.dms.servlet;

import com.google.common.collect.ImmutableMap;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@MultipartConfig(fileSizeThreshold = 2 * 1024 * 1024)
public class TestServlet extends HttpServlet {

    private static Logger log = LoggerFactory.getLogger(TestServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();

        Part part = request.getPart("file");
        String md5 = request.getParameter("md5");
        String fileName = request.getParameter("fileName");
//        part.getSubmittedFileName();
        log.info("partName={}, fileName={}, partSize ={}, md5={}", part.getName(), fileName,part.getSize(), md5);

        out.println(JSONObject.fromObject(ImmutableMap.of(
                "status", 0,
                "msg", "call success"
        )).toString(4));
    }
}
