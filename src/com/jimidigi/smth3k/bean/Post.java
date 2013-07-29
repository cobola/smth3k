package com.jimidigi.smth3k.bean;

import java.util.Date;

public class Post extends Entity {

    private String subjectID;
    private String postID;
    private String title;
    private String author;
    private String boardEngName;
    private Date date;
    private String content;
    private String floor;
    private String url;


    private boolean loadImage;


    @Override
    public String toString() {
        return this.title + " : " + this.content;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(String subjectid) {
        this.subjectID = subjectid;
    }


    public String getTitle() {
        return this.title;
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

    public String getBoardEngName() {
        return boardEngName;
    }

    public void setBoardEngName(String boardEngName) {
        this.boardEngName = boardEngName;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public boolean isLoadImage() {
        return loadImage;
    }

    public void setLoadImage(boolean loadImage) {
        this.loadImage = loadImage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
