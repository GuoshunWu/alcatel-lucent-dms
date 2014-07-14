package com.alcatel_lucent.dms.action.api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.UnexpectedRollbackException;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.service.DeliveringDictPool;
import com.alcatel_lucent.dms.service.DeliveryReport;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.JSONService;
import com.alcatel_lucent.dms.util.Util;

@SuppressWarnings("serial")
@Result(type="json", params={"noCache","true","ignoreHierarchy","false","includeProperties","status,message,report.*"})
public class DeliverDictAction extends APIAction {
	
	private static final int MAX_WARNINGS = 100;

    private String prod;
    private String app;
    private String ver;
    private File upload;
    private String filename;
	private Boolean autoCreateLang;
	private Boolean test;

    private int status = 0;
    private String message = "success";
    private DeliveryReport report;
    
    @Value("${dms.deliver.dir}")
    private String deliverDir;

    private DeliveringDictPool deliveringDictPool;
    private DictionaryService dictionaryService;
	private JSONService jsonService;
    
    private SimpleDateFormat dFmt = new SimpleDateFormat("yyyyMMdd_HHmmss");

    @Override
	protected String performAction() throws Exception {
        log.info("upload={}, filename={}, prod={}, app={}, ver={}", new Object[]{upload, filename, prod, app, ver});
        try {
        	// find application
	        Application application = dictionaryService.findApplication(prod, app, ver);
	        if (application == null) {
	        	status = 1;
	        	message = "Application not found, please input correct prod/app/ver parameters.";
	        	return SUCCESS;
	        }
	        
	        // parse uploaded file
	        File tmpFile = new File(FileUtils.getTempDirectory(), filename);
	        if(tmpFile.exists()) tmpFile.delete();
	        boolean fileCreateSuccess = upload.renameTo(tmpFile);
	        if (!fileCreateSuccess) {
	            log.warn("move file fail.");
	        }
	        filename = FilenameUtils.normalize(tmpFile.getAbsolutePath(),true);
	        String tempName = UserContext.getInstance().getUser().getName() + "_" + dFmt.format(new Date());
	        File deliverDestDir = new File(deliverDir, tempName);
	        if (!deliverDestDir.exists()) deliverDestDir.mkdirs();
	        if (Util.isZipFile(filename)) {
	            log.info("decompress file " + tmpFile + " to " + deliverDestDir.getAbsolutePath());
	            ProgressQueue.setProgress("Decompressing zip package...", -1);
	            Util.unzip(tmpFile, deliverDestDir.getAbsolutePath());
	
	            //move and rename zip file to deliver dest dir
	            FileUtils.moveFile(tmpFile, new File(deliverDir, tempName + ".zip"));
	
	        } else {
	            log.info("deliver normal(not zip) file.");
	            File destFile = new File(deliverDestDir, tmpFile.getName());
	            FileUtils.moveFile(tmpFile, destFile);
	
	            //compress backup
	            Util.createZip(destFile.getAbsolutePath(), deliverDestDir.getAbsolutePath() + ".zip");
	        }
	        String handler = deliverDestDir.getName();
	        deliveringDictPool.addHandler(handler, application.getId());
	        FileUtils.deleteDirectory(deliverDestDir);
	        
	        // import dictionaries
			Collection<Dictionary> dictList = deliveringDictPool.getDictionaries(handler);
			Constants.ImportingMode mode = test != null && test.booleanValue() ? Constants.ImportingMode.TEST : Constants.ImportingMode.DELIVERY;
			report = importDictionaries(application.getId(), dictList, mode);
			deliveringDictPool.removeHandler(handler);

        } catch (BusinessException e) {
        	status = 1;
        	message = e.getMessage();
        }
        return SUCCESS;
	}

    private DeliveryReport importDictionaries(Long appId, Collection<Dictionary> dictList, Constants.ImportingMode mode) throws BusinessException {
        DeliveryReport report = new DeliveryReport();
        Map<String, Collection<String>> warningMap = new TreeMap<String, Collection<String>>();
        Iterator<Dictionary> dictionaryIterator = dictList.iterator();
        while(dictionaryIterator.hasNext()){
            Dictionary dict = dictionaryIterator.next();
            // remote from the collection to free memory
            dictionaryIterator.remove();

            Map<String, String> langCharset = new HashMap<String, String>();
            if (dict.getDictLanguages() != null) {
                for (DictionaryLanguage dl : dict.getDictLanguages()) {
                    langCharset.put(dl.getLanguageCode(), dl.getCharset().getName());
                }
            }
            ArrayList<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
            try {
            	dictionaryService.importDictionary(appId, dict, dict.getVersion(), mode, null, langCharset, autoCreateLang, warnings, report);
            } catch (UnexpectedRollbackException e) {
            	if (mode == Constants.ImportingMode.TEST) {
            		log.info("Rolled back all changes of importing because of TEST mode");
            	} else {
            		throw e;
            	}
            }
            if (warnings.size() > 0) {
            	ArrayList<String> warnMessages = new ArrayList<String>();
            	int count = 0;
            	for (BusinessWarning warning : warnings) {
            		if (count == MAX_WARNINGS) {
            			warnMessages.add(new BusinessWarning(BusinessWarning.MORE, warnings.size() - MAX_WARNINGS).toString());
            			break;
            		}
            		warnMessages.add(warning.toString());
            		count++;
            	}
            	warningMap.put(dict.getName(), warnMessages);
            }
        }
        report.setWarningMap(warningMap);
        log.info(report.toString());
        return report;
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

	public File getUpload() {
		return upload;
	}

	public void setUpload(File upload) {
		this.upload = upload;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	public JSONService getJsonService() {
		return jsonService;
	}

	public void setJsonService(JSONService jsonService) {
		this.jsonService = jsonService;
	}

	public DeliveringDictPool getDeliveringDictPool() {
		return deliveringDictPool;
	}

    public void setDeliveringDictPool(DeliveringDictPool deliveringDictPool) {
        this.deliveringDictPool = deliveringDictPool;
    }

	public DeliveryReport getReport() {
		return report;
	}

	public void setReport(DeliveryReport report) {
		this.report = report;
	}

	public Boolean getAutoCreateLang() {
		return autoCreateLang;
	}

	public void setAutoCreateLang(Boolean autoCreateLang) {
		this.autoCreateLang = autoCreateLang;
	}

	public Boolean getTest() {
		return test;
	}

	public void setTest(Boolean test) {
		this.test = test;
	}

}
