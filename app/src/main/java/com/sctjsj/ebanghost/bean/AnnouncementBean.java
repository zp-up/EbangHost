package com.sctjsj.ebanghost.bean;

/**
 * Created by mayikang on 17/2/24.
 */

public class AnnouncementBean {
    private int id;
    private String content;
    private String date;
    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
