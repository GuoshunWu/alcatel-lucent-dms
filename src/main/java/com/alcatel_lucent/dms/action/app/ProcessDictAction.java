package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.ProgressAction;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.service.DeliveringDictPool;
import com.alcatel_lucent.dms.util.Util;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Action of creating a product
 *
 * @author allany
 */
public class ProcessDictAction extends ProgressAction {

    private static Logger log = LoggerFactory.getLogger(ProcessDictAction.class);

    private String filename;
    private Long appId;

    private SimpleDateFormat dFmt = new SimpleDateFormat("yyyyMMdd_HHmmss");

    @Value("${dms.deliver.dir}")
    private String deliverDir;

    private DeliveringDictPool deliveringDictPool;

    public void setDeliveringDictPool(DeliveringDictPool deliveringDictPool) {
        this.deliveringDictPool = deliveringDictPool;
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
        File dir = new File(deliverDir, UserContext.getInstance().getUser().getName() +"_"+ dFmt.format(new Date()));

        if (!dir.exists()) dir.mkdirs();
        File upload = new File(filename);
        log.info("upload={}, filename={}, targetdir={}, appId={}", new Object[]{upload, filename, dir, appId});

        boolean fileCreateSuccess = true;
        if (Util.isZipFile(filename)) {
            log.info("decompress file " + upload + " to " + dir.getAbsolutePath());
            ProgressQueue.setProgress("Decompressing zip package...", -1);
            Util.unzip(upload, dir.getAbsolutePath());

        } else {
            log.info("deliver normal(not zip) file.");
            fileCreateSuccess = upload.renameTo(new File(dir, upload.getName()));
            if (!fileCreateSuccess) {
                log.warn("move file fail.");
                fileCreateSuccess = false;
            }
        }
        if (fileCreateSuccess) {
            deliveringDictPool.addHandler(dir.getName(), appId);
        }

        setMessage(dir.getName());
        setStatus(0);
        return SUCCESS;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

}
