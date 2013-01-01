package com.alcatel_lucent.dms.model;

import javax.persistence.*;

@Entity
@Table(name = "CHARSET")
public class Charset extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7987985981036760543L;


    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_CHARSET", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

	private Integer no;
	private String name;



    @Column(name = "NO")
	public Integer getNo() {
		return no;
	}
	public void setNo(Integer no) {
		this.no = no;
	}

    @Column(name = "NAME", nullable = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return String.format("Charset [no=%s, name=%s]", no, name);
	}
	
	
}
