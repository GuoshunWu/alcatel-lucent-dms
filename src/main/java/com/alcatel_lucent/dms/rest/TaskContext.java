package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.alcatel_lucent.dms.model.Context;

public class TaskContext {
	
	private Context context;
	private int total;
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

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
	
	public void count() {
		total = 0;
		if (s != null) {
			Collection<int[]> values = s.values();
			for (int[] value : values) {
				total += value[0] + value[1];
			}
		}
	}

}
