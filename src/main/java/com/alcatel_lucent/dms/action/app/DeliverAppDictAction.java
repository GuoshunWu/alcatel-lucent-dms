package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.SpringContext;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.util.Util;
import com.opensymphony.xwork2.inject.Inject;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Action of creating a product
 *
 * @author allany
 */
@ParentPackage("json-default")
@Result(type = "json", params = {"noCache", "true", "contentType", "text/html", "ignoreHierarchy", "false", "includeProperties", "message,status"})

public class DeliverAppDictAction extends JSONAction {

    private File upload;
    private String contentType;
    private String filename;

    private SimpleDateFormat dFmt=new SimpleDateFormat("yyyyMMdd_HHmmss_S");

//    @Inject("struts.multipart.saveDir")
//    private String uploadRepository;

    public void setUpload(File upload) {
        this.upload = upload;
    }

    public void setUploadContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setUploadFileName(String filename) {
        this.filename = filename;
    }

    public String performAction() throws Exception {

        System.out.println("file=" + upload);
        System.out.println("contentType=" + contentType);
        System.out.println("uploadFileName=" + filename);

        if(!Util.isZipFile(filename)){
            setMessage(getText(""));
            return SUCCESS;
        }
//        System.out.println("Repos: "+uploadRepository);
        File dir =  new File(upload.getParent(), "USER_"+dFmt.format(new Date()));
        System.out.println("decompress file " + upload +" to "+dir.getAbsolutePath());

        Util.unzip(upload, dir.getAbsolutePath());

        setStatus(0);
        setMessage("Success");
        return SUCCESS;
    }

}
