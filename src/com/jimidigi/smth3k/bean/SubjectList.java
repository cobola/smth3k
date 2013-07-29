package com.jimidigi.smth3k.bean;

import com.jimidigi.smth3k.AppException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子列表实体类
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class SubjectList extends Entity {


    private int boardType;
    private int pageCount;
    private int pageNow;
    private String boardChsName;


    private List<Subject> subjectList = new ArrayList<Subject>();




    public int getPageCount() {
        return pageCount;
    }

    public int getPageNow() {
        return pageNow;
    }

    public String getBoardChsName() {
        return boardChsName;
    }

    public List<Subject> getSubjectList() {
        return subjectList;
    }

    public void setSubjectList(List<Subject> subjectList) {
        this.subjectList = subjectList;
    }


    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setPageNow(int pageNow) {
        this.pageNow = pageNow;
    }

    public void setBoardChsName(String boardChsName) {
        this.boardChsName = boardChsName;
    }

    public int getBoardType() {
        return boardType;
    }

    public void setBoardType(int boardType) {
        this.boardType = boardType;
    }
}
