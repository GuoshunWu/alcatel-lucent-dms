package com.alcatel_lucent.dms.model;

public class Charset extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7987985981036760543L;
	
	private int no;
	private String name;
	
	
	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
