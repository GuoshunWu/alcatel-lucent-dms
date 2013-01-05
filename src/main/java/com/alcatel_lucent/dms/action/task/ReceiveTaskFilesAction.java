package com.alcatel_lucent.dms.action.task;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TaskService;
import com.alcatel_lucent.dms.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("serial")

@ParentPackage("dms-json")
@Result(type = "json", params = {"noCache", "true", "contentType", "text/html", "ignoreHierarchy", "false",
        "includeProperties", "message,status"})
public class ReceiveTaskFilesAction extends JSONAction {

    private TaskService taskService;

    private static Logger log = LoggerFactory.getLogger(ReceiveTaskFilesAction.class);

    private File upload;
    private String contentType;
    private String filename;
    private Long id;

    private SimpleDateFormat dFmt = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
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

    @Value("${dms.receive.dir}")
    private String receiveDir;

    @Override
    protected String performAction() throws Exception {
        log.info("receive dir=" + receiveDir + ", task id=" + id + ", filename=" + filename);

        File dir = new File(receiveDir, UserContext.getInstance().getUser().getName() + "_" + dFmt.format(new Date()));
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

        taskService.receiveTaskFiles(id, dir.getAbsolutePath());

        setMessage(getText("message.success"));
        return SUCCESS;
    }

}
