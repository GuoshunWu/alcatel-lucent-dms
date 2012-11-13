package com.alcatel_lucent.dms.model;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-11-13
 * Time: 上午11:34
 * To change this template use File | Settings | File Templates.
 */
public class User{
    private String loginName;
    private String name;
    private String email;
    private Timestamp lastLoginTime;

    private int role;
    private int status;


    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User() {
    }
}
