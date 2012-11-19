package com.alcatel_lucent.dms.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.io.Serializable;

public abstract class BaseEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3756598069703614852L;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name="id_app_seq",sequenceName = "ID_APPLICATION", allocationSize = 100)
    private Long id;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
