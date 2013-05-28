package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.action.ProgressAction;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.service.DeliveringDictPool;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Action of creating a product
 *
 * @author allany
 */
@ParentPackage("dms-json")
@Result(type = "json", params = {"noCache", "true", "contentType", "text/html", "ignoreHierarchy", "false",
        "includeProperties", "message,status,filename,appId"})

public class DeliverAppDictAction extends JSONAction {

    private static Logger log = LoggerFactory.getLogger(DeliverAppDictAction.class);

    private File upload;
    private String contentType;
    private String filename;
    private Long appId;

    private SimpleDateFormat dFmt = new SimpleDateFormat("yyyyMMdd_HHmmss");

    @Value("${dms.deliver.dir}")
    private String deliverDir;

    private DeliveringDictPool deliveringDictPool;

    public void setDeliveringDictPool(DeliveringDictPool deliveringDictPool) {
        this.deliveringDictPool = deliveringDictPool;
    }

    public void setUpload(File upload) {
        this.upload = upload;
    }

    public void setUploadContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setUploadFileName(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String performAction() throws Exception {
        log.info("upload={}, filename={}, contentType={}, appId={}", new Object[]{upload, filename, contentType, appId});
        File tmpFile = new File(FileUtils.getTempDirectory(), filename);
        if(tmpFile.exists()) tmpFile.delete();
        boolean fileCreateSuccess = upload.renameTo(tmpFile);
        if (!fileCreateSuccess) {
            log.warn("move file fail.");
        }
        filename = FilenameUtils.normalize(tmpFile.getAbsolutePath(),true);
        setStatus(0);
        String msg = fileCreateSuccess ? getText("message.success") : getText("message.createfail", Arrays.asList(tmpFile));
        setMessage(msg);
        return SUCCESS;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

}
