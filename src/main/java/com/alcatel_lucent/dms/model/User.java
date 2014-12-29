package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.Global;
import org.apache.catalina.util.MD5Encoder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-11-13
 * Time: 上午11:34
 * To change this template use File | Settings | File Templates.
 */


@SuppressWarnings("serial")
//@Entity
@Table(name = "USER")
public class User implements Serializable {

    public static final int ROLE_GUEST = 0;
    public static final int ROLE_APPLICATION_OWNER = 1;
    public static final int ROLE_TRANSLATION_MANAGER = 2;
    public static final int ROLE_ADMINISTRATOR = 4;

    public static final int ENABLED = 1;
    public static final int DISABLED = 0;

    private String loginName;
    private String name;
    private String email;
    private Timestamp lastLoginTime;

    private Integer loginCounter = 0;

    private int role = ROLE_GUEST;
    private int status = ENABLED;

    private String passwordDigest;

    private Boolean showTips = true;


    public User(String loginName, String email, String name) {
        this.loginName = loginName;
        this.name = name;
        this.email = email;
    }

    @Column(name = "LOGIN_COUNTER")
    public Integer getLoginCounter() {
        return loginCounter;
    }

    public void setLoginCounter(Integer loginCounter) {
        this.loginCounter = loginCounter;
    }

    /**
     * Local user authenticate
     */
    public boolean authenticate(String password) {
        if (null == password) return false;
        return DigestUtils.md5Hex(password).equals(passwordDigest);
    }

    @Column(name = "PASSWORD_DIGEST")
    public String getPasswordDigest() {
        return passwordDigest;
    }

    public void setPasswordDigest(String passwordDigest) {
        this.passwordDigest = passwordDigest;
    }

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

    @Column(name = "LAST_LOGIN_TIME")
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

    @Column(name = "SHOW_TIPS")
    public Boolean getShowTips() {
        return showTips;
    }

    public void setShowTips(Boolean showTips) {
        this.showTips = showTips;
    }

    @Transient
    public boolean isOnLine() {
        return Global.isOnLine(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return (loginName.equals(user.loginName));
    }

    @Override
    public int hashCode() {
        return loginName.hashCode();
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
