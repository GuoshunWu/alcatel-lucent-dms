package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-11-13
 * Time: 上午11:34
 * To change this template use File | Settings | File Templates.
 */


@Entity
@Table(name = "USER")
public class User implements Serializable {
	
	public static final int ROLE_GUEST = 0;
	public static final int ROLE_APPLICATION_OWNER = 1;
	public static final int ROLE_TRANSLATION_MANAGER = 2;
	public static final int ROLE_ADMINISTRATOR = 3;
	
	public static final int ENABLED = 1;
	public static final int DISABLED = 0;
	
    private String loginName;
    private String name;
    private String email;
    private Timestamp lastLoginTime;

    private int role;
    private int status;

    @Id
    @Column(name = "LOGIN_NAME")
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "LAST_LOGIN_Time")
    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    @Column(name = "ROLE")
    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    @Column(name = "STATUS")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Column(name = "EMAIL")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "lastLoginTime=" + lastLoginTime +
                ", loginName='" + loginName + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
