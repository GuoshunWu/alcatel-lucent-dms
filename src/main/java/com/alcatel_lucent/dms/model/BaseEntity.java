package com.alcatel_lucent.dms.model;

import java.io.Serializable;

public abstract class BaseEntity implements Serializable {
	private Long id;
	public Long getId() {
		return id;
	}
}
