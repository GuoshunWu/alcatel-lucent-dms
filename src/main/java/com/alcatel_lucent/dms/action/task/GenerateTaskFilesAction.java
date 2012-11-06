package com.alcatel_lucent.dms.action.task;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.TaskService;
import com.alcatel_lucent.dms.util.Util;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Action of download dictionaries
 *
 * @author Guoshun Wu
 */
@SuppressWarnings("serial")
@ParentPackage("json-default")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "fileLoc,message,status"})
public class GenerateTaskFilesAction extends JSONAction {

    private String filename;
    private Long id;
    private String fileLoc;


    private TaskService taskService;
    private SimpleDateFormat dFmt = new SimpleDateFormat("yyyyMMdd_HHmmss");


    @Value("${dms.send.dir}")
    private String tmpDownload;


    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
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
        String downTmpPath = tmpDownload + File.separator + "USER_" + dFmt.format(new Date());
        taskService.generateTaskFiles(downTmpPath, id);


        File generatedTaskFiles=new File(downTmpPath);
        if (!generatedTaskFiles.exists()){
            setStatus(-1);
            setMessage(getText("message.genfail"));
            return SUCCESS;
        }
        File zipFile = new File(tmpDownload, filename);
        Util.createZip(generatedTaskFiles.listFiles(), zipFile);

        setFileLoc(zipFile.getAbsolutePath());

        setStatus(0);
        setMessage(getText("message.success"));
        return SUCCESS;
    }
}
