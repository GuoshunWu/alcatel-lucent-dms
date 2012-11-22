package com.alcatel_lucent.dms.model;

import java.util.Collection;
import java.util.Date;

@SuppressWarnings("serial")
public class Task extends BaseEntity {
	
	public static final int STATUS_OPEN = 0;
	public static final int STATUS_CLOSED = 1;
	
	private String name;
	private Product product;
	private Date createTime;
	private Date lastUpdateTime;
	private Date lastApplyTime;
	private Date closeTime;
	private int status;
	private User creator;
	private User lastUpdater;
	private Collection<TaskDetail> details;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Collection<TaskDetail> getDetails() {
		return details;
	}
	public void setDetails(Collection<TaskDetail> details) {
		this.details = details;
	}
	public Date getLastApplyTime() {
		return lastApplyTime;
	}
	public void setLastApplyTime(Date lastApplyTime) {
		this.lastApplyTime = lastApplyTime;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public User getLastUpdater() {
		return lastUpdater;
	}
	public void setLastUpdater(User lastUpdater) {
		this.lastUpdater = lastUpdater;
	}
	public Date getCloseTime() {
		return closeTime;
	}
	public void setCloseTime(Date closeTime) {
		this.closeTime = closeTime;
	}
}
