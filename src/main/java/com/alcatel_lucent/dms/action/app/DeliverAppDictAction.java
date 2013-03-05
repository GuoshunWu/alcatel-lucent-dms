package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DeliveringDictPool;
import com.alcatel_lucent.dms.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Action of creating a product
 *
 * @author allany
 */
@ParentPackage("dms-json")
@Result(type = "json", params = {"noCache", "true", "contentType", "text/html", "ignoreHierarchy", "false",
        "includeProperties", "message,status,filename"})

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
        log.info("deliverDir=" + deliverDir);
        File dir = new File(deliverDir, UserContext.getInstance().getUser().getName() +"_"+ dFmt.format(new Date()));
        if (!dir.exists()) dir.mkdirs();

        boolean fileCreateSuccess = true;
        if (Util.isZipFile(filename)) {
            log.info("decompress file " + upload + " to " + dir.getAbsolutePath());
            Util.unzip(upload, dir.getAbsolutePath());

        } else {
            log.info("deliver normal(not zip) file.");
            fileCreateSuccess = upload.renameTo(new File(dir, filename));
            if (!fileCreateSuccess) {
                log.warn("move file fail.");
                fileCreateSuccess = false;
            }
        }
        if (fileCreateSuccess) {
            deliveringDictPool.addHandler(dir.getName(), appId);
        }

        filename = dir.getName();

        setStatus(0);
        setMessage(getText("message.success"));
        return SUCCESS;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

}
