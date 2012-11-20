package com.alcatel_lucent.dms.action;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.opensymphony.xwork2.ActionSupport;

@SuppressWarnings("serial")
public class BaseAction extends ActionSupport {
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	protected Collection<Long> toIdList(String idStr) {
		String[] ids = idStr.split(",");
		Collection<Long> result = new ArrayList<Long>();
		for (String id : ids) {
			if (!id.trim().isEmpty()) {
				result.add(Long.valueOf(id));
			}
		}
		return result;
	}


}
