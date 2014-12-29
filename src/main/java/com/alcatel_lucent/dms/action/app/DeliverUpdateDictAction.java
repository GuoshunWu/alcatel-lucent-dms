package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.DeliveringDictPool;

@SuppressWarnings("serial")
public class DeliverUpdateDictAction extends JSONAction {
	
	private DeliveringDictPool deliveringDictPool;
	
	private String handler;
	private Long id;
	private String name;
	private String version;
	private String format;
	private String encoding;
	
	@Override
	protected String performAction() throws Exception {
		log.info("DeliverUpdateDictAction: handler=" + handler + ", id=" + id + ", name=" + name + 
				", version=" + version + ", format=" + format + ", encoding=" + encoding);
		try {
			Dictionary dict = deliveringDictPool.getDictionary(handler, id);
			if (name != null && !name.trim().isEmpty()) {
				dict.setName(name);
			}
			if (version != null && !version.trim().isEmpty()) {
				dict.setVersion(version);
			}
			if (format != null && !format.trim().isEmpty()) {
				dict.setFormat(format);
			}
			if (encoding != null && !encoding.trim().isEmpty()) {
				dict.setEncoding(encoding);
			}
			dict.validate(true);
		} catch (BusinessException e) {
			setMessage(e.toString());
			setStatus(-1);
		}
		return SUCCESS;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public DeliveringDictPool getDeliveringDictPool() {
		return deliveringDictPool;
	}

	public void setDeliveringDictPool(DeliveringDictPool deliveringDictPool) {
		this.deliveringDictPool = deliveringDictPool;
	}
}
