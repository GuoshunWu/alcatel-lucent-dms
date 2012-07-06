package com.alcatel_lucent.dms.model;

import java.io.Serializable;

public abstract class BaseEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3756598069703614852L;
	private Long id;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return String.format("BaseEntity [id=%s]", id);
	}
	
}
