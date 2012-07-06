package com.alcatel_lucent.dms.model;

public class Context extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1417207278044645451L;
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return String.format("Context [name=%s]", name);
	}
	
}
