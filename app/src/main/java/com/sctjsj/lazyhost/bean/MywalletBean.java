package com.sctjsj.lazyhost.bean;

public class MywalletBean {
    /**
     * domainBase : WEB-INF/zh_CN/pc/
     * todayBalance : 452.00
     * todayExpenditure : 2.00
     * balance : 1449.00
     * result : true
     * msg : 成功
     */

    private String domainBase;
    private String todayBalance;
    private String todayExpenditure;
    private String balance;
    private boolean result;
    private String msg;

    public String getDomainBase() {
        return domainBase;
    }

    public void setDomainBase(String domainBase) {
        this.domainBase = domainBase;
    }

    public String getTodayBalance() {
        return todayBalance;
    }

    public void setTodayBalance(String todayBalance) {
        this.todayBalance = todayBalance;
    }

    public String getTodayExpenditure() {
        return todayExpenditure;
    }

    public void setTodayExpenditure(String todayExpenditure) {
        this.todayExpenditure = todayExpenditure;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
