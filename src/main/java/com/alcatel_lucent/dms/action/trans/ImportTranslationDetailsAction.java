package com.alcatel_lucent.dms.action.trans;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TranslationService;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

/**
 * Action of creating a product
 *
 * @author allany
 */
@ParentPackage("dms-json")
@Result(type = "json", params = {"noCache", "true", "contentType", "text/html", "ignoreHierarchy", "false",
        "includeProperties", "message,status,filename"})

public class ImportTranslationDetailsAction extends JSONAction {

    private static Logger log = LoggerFactory.getLogger(ImportTranslationDetailsAction.class);

    private File upload;
    private String contentType;
    private String filename;

    @Value("${dms.deliver.dir}")
    private String deliverDir;

    @Autowired
    private TranslationService translationService;

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
        File restoredFile = new File(deliverDir, filename);
        log.info("upload={}, filename={}, contentType={}, restoredFile={}", new Object[]{upload, filename, contentType, restoredFile});
        if (restoredFile.exists()) restoredFile.delete();
        FileUtils.copyFile(upload, restoredFile);
        int num = translationService.importTranslations(upload);
        log.info("{} translation updated.", num);
        setStatus(0);
        setMessage(getText("message.translationupdated", new String[]{num + ""}));
        return SUCCESS;
    }

}
