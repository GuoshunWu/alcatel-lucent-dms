package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.File;

/**
 * Action of creating a product
 *
 * @author allany
 */
@ParentPackage("json-default")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "message,status"})

public class DeliverAppDictAction extends JSONAction {

    private File upload;
    private String contentType;
    private String filename;
    
    public void setUpload(File upload){
        this.upload=upload;
    }

    public void setUploadContentType(String contentType){
        this.contentType=contentType;
    }
    
    public void setUploadFileName(String filename){
        this.filename=filename;
    }
    
    public String performAction() throws Exception {
        System.out.println("file="+upload);
        System.out.println("contentType="+contentType);
        System.out.println("uploadFileName="+filename);
        File destFile=new File("tmp",filename);
        System.out.println("copy file to "+destFile.getAbsolutePath());
//        or we can do import here.
        FileUtils.copyFile(upload,destFile);
        

        setStatus(0);
        setMessage("Success");
        return SUCCESS;
    }

}
