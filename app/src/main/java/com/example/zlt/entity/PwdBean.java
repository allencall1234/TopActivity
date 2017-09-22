package com.example.zlt.entity;

/**
 * Created by 524202 on 2017/9/19.
 */

public class PwdBean {
    public String account;
    public String pwd;

    public PwdBean(String account, String pwd) {
        this.account = account;
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return account + "::" + pwd;
    }
}
