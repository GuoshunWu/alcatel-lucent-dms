package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.DeliveringDictPool;

@SuppressWarnings("serial")
public class DeliverUpdateDictLanguageAction extends JSONAction {
	
	private DeliveringDictPool deliveringDictPool;
	private DaoService daoService;
	
	private String handler;
	private Long dict;
	private String code;
	private Long languageId;
	private Long charsetId;
	
	@Override
	protected String performAction() throws Exception {
		log.info("DeliverUpdateDictLanguageAction: handler=" + handler + ", dict=" + dict + ", code=" + code + 
				", languageId=" + languageId + ", charsetId=" + charsetId);
		try {
			Dictionary dictionary = deliveringDictPool.getDictionary(handler, dict);
			DictionaryLanguage dl = dictionary.getDictLanguage(code);
			if (languageId != null) {
	        	dl.setLanguage((Language) daoService.retrieve(Language.class, languageId));
	    	}
	    	if (charsetId != null) {
	        	dl.setCharset((Charset) daoService.retrieve(Charset.class, charsetId));
	    	}		
	    } catch (BusinessException e) {
			setMessage(e.toString());
			setStatus(-1);
		}
		return SUCCESS;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public DeliveringDictPool getDeliveringDictPool() {
		return deliveringDictPool;
	}

	public void setDeliveringDictPool(DeliveringDictPool deliveringDictPool) {
		this.deliveringDictPool = deliveringDictPool;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

	public Long getCharsetId() {
		return charsetId;
	}

	public void setCharsetId(Long charsetId) {
		this.charsetId = charsetId;
	}

	public Long getDict() {
		return dict;
	}

	public void setDict(Long dict) {
		this.dict = dict;
	}

	public DaoService getDaoService() {
		return daoService;
	}

	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
}
