package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.ProgressAction;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.service.DeliveringDictPool;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.io.FileUtils;
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
        String tempName = UserContext.getInstance().getUser().getName() + "_" + dFmt.format(new Date());
        File deliverDestDir = new File(deliverDir, tempName);
        if (!deliverDestDir.exists()) deliverDestDir.mkdirs();

        File upload = new File(filename);
        log.info("upload={}, filename={}, targetdir={}, appId={}", new Object[]{upload, filename, deliverDestDir, appId});

        if (Util.isZipFile(filename)) {
            log.info("decompress file " + upload + " to " + deliverDestDir.getAbsolutePath());
            ProgressQueue.setProgress("Decompressing zip package...", -1);
            Util.unzip(upload, deliverDestDir.getAbsolutePath());

            //move and rename zip file to deliver dest dir
            FileUtils.moveFile(upload, new File(deliverDir, tempName + ".zip"));

        } else {
            log.info("deliver normal(not zip) file.");
            File destFile = new File(deliverDestDir, upload.getName());
            FileUtils.moveFile(upload, destFile);

            //compress backup
            Util.createZip(destFile.getAbsolutePath(), deliverDestDir.getAbsolutePath() + ".zip");
        }
        deliveringDictPool.addHandler(deliverDestDir.getName(), appId);

        FileUtils.deleteDirectory(deliverDestDir);

        setMessage(deliverDestDir.getName());
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
