package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.action.ProgressAction;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.service.DictionaryService;
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
public class GenerateDictAction extends ProgressAction {

    private String filename;
    private String dicts;
//    private String fileLoc;


    private DictionaryService dictionaryService;
    private SimpleDateFormat dFmt = new SimpleDateFormat("yyyyMMdd_HHmmss");


    @Value("${dms.generate.dir}")
    private String tmpDownload;


    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public String getDicts() {
        return dicts;
    }

    public void setDicts(String dicts) {
        this.dicts = dicts;
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

//    public String getFileLoc() {
//        return fileLoc;
//    }
//
//    public void setFileLoc(String fileLoc) {
//        this.fileLoc = fileLoc;
//    }

    public String performAction() throws Exception {
        String downTmpPath = tmpDownload + File.separator + UserContext.getInstance().getUser().getName() +"_"+ dFmt.format(new Date());
        dictionaryService.generateDictFiles(downTmpPath, toIdList(dicts));
        ProgressQueue.setProgress("Compressing...", 80);
        File generatedTaskFiles = new File(downTmpPath);
        if (!generatedTaskFiles.exists()) {
            setStatus(-1);
            setMessage(getText("message.genfail"));
            return SUCCESS;
        }

        File zipFile = new File(tmpDownload, filename);
        Util.createZip(new File(downTmpPath).listFiles(), zipFile);

        setStatus(0);
        setMessage(zipFile.getAbsolutePath());
        ProgressQueue.setProgress("Complete", 100);
        return SUCCESS;
    }
}
