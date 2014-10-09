package com.alcatel_lucent.dms.action.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Value;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.generator.GeneratorSettings;
import com.alcatel_lucent.dms.util.Util;

@SuppressWarnings("serial")
@Result(type = "stream", params = {"contentType", "application/zip", "inputName", "inStream", "contentDisposition", "attachment;filename=\"dict.zip\""})
public class GenerateDictAction extends APIAction {

	private InputStream inStream;
	
    private String prod;
    private String app;
    private String ver;
    private Boolean escapeApostrophe;

    @Value("${dms.generate.dir}")
    private String tmpDownload;
    
    private DictionaryService dictionaryService;
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    private SimpleDateFormat dFmt = new SimpleDateFormat("yyyyMMdd_HHmmss");
    
    public String performAction() throws Exception {
    	log.info("[API] GenerateDict: prod=" + prod + ",app=" + app + ",ver=" + ver);
    	String pathname = "api_generate_dict_" + dFmt.format(new Date());
    	String downTmpPath = tmpDownload + File.separator + pathname;
    	String zipFilename = pathname + ".zip";
    	Collection<Dictionary> dictList = dictionaryService.findDictionaries(prod, app, ver);
    	Collection<Long> idList = new ArrayList<Long>();
    	for (Dictionary dict : dictList) {
    		idList.add(dict.getId());
    	}
    	dictionaryService.generateDictFiles(downTmpPath, idList, new GeneratorSettings(escapeApostrophe));
        File zipFile = new File(tmpDownload, zipFilename);
        Util.createZip(new File(downTmpPath).listFiles(), zipFile);
		inStream = new FileInputStream(zipFile);
        return SUCCESS;
    }

	public InputStream getInStream() throws FileNotFoundException {
		return inStream;
	}

	public String getProd() {
		return prod;
	}

	public void setProd(String prod) {
		this.prod = prod;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getVer() {
		return ver;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}

	public Boolean getEscapeApostrophe() {
		return escapeApostrophe;
	}

	public void setEscapeApostrophe(Boolean escapeApostrophe) {
		this.escapeApostrophe = escapeApostrophe;
	}
}
