package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.util.Util;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Action of download dictionaries
 *
 * @author Guoshun Wu
 */
@Result(type = "stream", params = {"contentType", "${mimeType}", "inputName", "inStream", "contentDisposition", "attachment;filename=\"${filename}\""})
public class DownloadAppDictAction extends JSONAction {

    private InputStream inStream;
    private String mimeType;
    private String filename;
    
    private String dicts;

    private ServletContext context = ServletActionContext.getServletContext();
    private DictionaryService dictionaryService;
    private String tmpDir="downloadtmp";

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public String getDicts() {
        return dicts;
    }

    public void setDicts(String dicts) {
        this.dicts = dicts;
    }

    public InputStream getInStream() {
        return inStream;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimeType() {
        if (null == filename) filename = "Unknown.zip";
        mimeType = context.getMimeType(filename);
        if (null == mimeType) mimeType = "application/zip";
        return mimeType;
    }

    public String getFilename() {
        return filename;
    }

    public String performAction() throws Exception {
        dictionaryService.generateDCTFiles(tmpDir,toIdList(dicts),null);
        File zipFile=new File(tmpDir,filename);
        Util.createZip(new File(tmpDir),zipFile);
        inStream = new FileInputStream(zipFile);
        return SUCCESS;
    }
}
