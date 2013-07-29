package com.jimidigi.smth3k.bean;

import java.util.ArrayList;
import java.util.Date;

public class Subject extends Entity {


    private static final long serialVersionUID = 7351647738651826553L;
    private String subjectID;
    private String title;
    private String author;
    private String boardEngName;
    private String boardChsName;
    private Date date;


    private boolean asPost;

    private int datatype;

    private boolean top;

    private int totalPageNo;
    private int currentPageNo;

    private String lastAuthor;
    private Date lastDate;
    private String url;
    private int replys;
    private String floor;


    private ArrayList<Post> replylist= new ArrayList<Post>();

    public String getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(String subjectID) {
        this.subjectID = subjectID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getTotalPageNo() {
        return totalPageNo;
    }

    public void setTotalPageNo(int totalPageNo) {
        this.totalPageNo = totalPageNo;
    }

    public int getCurrentPageNo() {
        return currentPageNo;
    }

    public void setCurrentPageNo(int currentPageNo) {
        this.currentPageNo = currentPageNo;
    }

    public String getLastAuthor() {
        return lastAuthor;
    }

    public void setLastAuthor(String lastAuthor) {
        this.lastAuthor = lastAuthor;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getReplys() {
        return replys;
    }

    public void setReplys(int replys) {
        this.replys = replys;
    }

    public ArrayList<Post> getReplylist() {
        return replylist;
    }

    public void setReplylist(ArrayList<Post> replylist) {
        this.replylist = replylist;
    }

    public String getBoardEngName() {
        return boardEngName;
    }

    public void setBoardEngName(String boardEngName) {
        this.boardEngName = boardEngName;
    }

    public String getBoardChsName() {
        return boardChsName;
    }

    public void setBoardChsName(String boardChsName) {
        this.boardChsName = boardChsName;
    }

    public boolean isAsPost() {
        return asPost;
    }

    public void setAsPost(boolean asPost) {
        this.asPost = asPost;
    }


    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }


    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getFloor() {
        return floor;
    }

    public int getDatatype() {
        return datatype;
    }

    public void setDatatype(int datatype) {
        this.datatype = datatype;
    }
}
