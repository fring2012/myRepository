package com.example.c.been;


import com.example.c.been.code.Column;

public class AccountInfo  {
    @Column(cname = "_id")
    private int id;
    @Column(cname = "account")
    private String account;
    @Column(cname = "password")
    private String password;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
