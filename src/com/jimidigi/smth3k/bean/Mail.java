package com.jimidigi.smth3k.bean;

import java.util.Date;

public class Mail extends Entity {

    public final static int TypeMail = 1;
    public final static int TypeRef = 2;

    public final static String INBOX = "/mail/inbox";
    public final static String OUTBOX = "/mail/outbox";
    public final static String DELETED = "/mail/deleted";

    public final static String AT = "/refer/at";
    public final static String REPLY = "/refer/reply";

    private int type;
    private boolean isUnread;
    private int id;
    private String senderID;
    private String title;
    private Date date;
    private String boxType;
    private String content;

    private int floor;//序号

    private String url;

    public void setUnread(boolean isUnread) {
        this.isUnread = isUnread;
    }

    public boolean isUnread() {
        return isUnread;
    }


    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }


    public String getBoxType() {
        return boxType;
    }

    public void setBoxType(String boxType) {
        this.boxType = boxType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
