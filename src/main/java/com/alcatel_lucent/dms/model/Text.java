package com.alcatel_lucent.dms.model;

public class Text extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4205533860022959713L;
	
	private Context context;
	private String reference;
	private int status;
	
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
}
