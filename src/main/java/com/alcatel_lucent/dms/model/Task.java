package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@SuppressWarnings("serial")

//@Entity
@Table(name = "TASK")
public class Task extends BaseEntity {


    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_TASK", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    public static final int STATUS_OPEN = 0;
    public static final int STATUS_CLOSED = 1;

    private String name;
    private Product product;
    private Application application;	// can be null if the task is in scope of product
    private Date createTime;
    private Date lastUpdateTime;
    private Date lastApplyTime;
    private Date closeTime;
    private int status;
    private User creator;
    private User lastUpdater;
    private Collection<TaskDetail> details;

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Column(name = "CREATE_TIME")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Column(name = "LAST_UPDATE_TIME")
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Column(name = "STATUS")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @OneToMany
    public Collection<TaskDetail> getDetails() {
        return details;
    }

    public void setDetails(Collection<TaskDetail> details) {
        this.details = details;
    }

    @Column(name = "LAST_APPLY_TIME")
    public Date getLastApplyTime() {
        return lastApplyTime;
    }

    public void setLastApplyTime(Date lastApplyTime) {
        this.lastApplyTime = lastApplyTime;
    }

    @ManyToOne
    @JoinColumn(name = "CREATOR_ID")
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @ManyToOne
    @JoinColumn(name = "LAST_UPDATER_ID")
    public User getLastUpdater() {
        return lastUpdater;
    }

    public void setLastUpdater(User lastUpdater) {
        this.lastUpdater = lastUpdater;
    }

    @Column(name = "CLOSE_TIME")
    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}
}
