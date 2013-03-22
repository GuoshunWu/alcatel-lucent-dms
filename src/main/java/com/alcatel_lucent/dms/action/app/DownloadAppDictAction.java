package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.join;

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

    private String fileLoc;

    private ServletContext context = ServletActionContext.getServletContext();


    public InputStream getInStream() throws FileNotFoundException {
        inStream = new FileInputStream(fileLoc);
        return inStream;
    }


    public String getMimeType() {
        mimeType = context.getMimeType(getFilename());
        if (null == mimeType) mimeType = "application/zip";
        return mimeType;
    }

    public String getFilename() {
        if (null != filename) return filename;

        String[] snippets = FilenameUtils.getBaseName(fileLoc).split("_");
        filename = join(Arrays.copyOfRange(snippets, 0, Math.min(snippets.length, 3)), '_') + ".zip";

        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileLoc() {
        return fileLoc;
    }

    public void setFileLoc(String fileLoc) {
        this.fileLoc = fileLoc;
    }

    public String performAction() throws Exception {
        log.info("filename=" + this.getFilename());
        return SUCCESS;
    }
}
