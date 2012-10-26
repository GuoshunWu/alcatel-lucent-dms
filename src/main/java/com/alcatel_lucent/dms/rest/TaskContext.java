package com.alcatel_lucent.dms.rest;

import java.util.HashMap;
import java.util.Map;

import com.alcatel_lucent.dms.model.Context;

public class TaskContext {
	
	private Context context;
	private Map<String, int[]> s;
	
	public Long getId() {
		return context.getId();
	}
	
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public Map<String, int[]> getS() {
		return s;
	}
	public void setS(Map<Long, int[]> s) {
		this.s = new HashMap<String, int[]>();
		for (Long id : s.keySet()) {
			this.s.put(id.toString(), s.get(id));
		}
	}

}
