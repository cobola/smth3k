package com.jimidigi.smth3k.bean;

import java.util.ArrayList;

public class Board extends Entity {
    private static final long serialVersionUID = -4618388540751724819L;

    private String engName;
    private String chsName;
    private boolean isDirectory;
    private String url;
    private String parentChsName;
    private String parentUrl;

    private ArrayList<Board> childBoards = new ArrayList<Board>();

    public Board(String engName) {
        this.setEngName(engName);
    }

    public Board() {

    }

    @Override
    public String toString() {
        return this.getEngName() + "\t" + this.getChsName();
    }


    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParentChsName() {
        return parentChsName;
    }

    public void setParentChsName(String parentChsName) {
        this.parentChsName = parentChsName;
    }

    public ArrayList<Board> getChildBoards() {
        return childBoards;
    }

    public void setChildBoards(ArrayList<Board> childBoards) {
        this.childBoards = childBoards;
    }

    public String getEngName() {
        return engName;
    }

    public void setEngName(String engName) {
        this.engName = engName;
    }

    public String getChsName() {
        return chsName;
    }

    public void setChsName(String chsName) {
        this.chsName = chsName;
    }

    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }
}
