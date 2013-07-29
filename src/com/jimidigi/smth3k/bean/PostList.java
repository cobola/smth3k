package com.jimidigi.smth3k.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子列表实体类
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class PostList extends Entity {


    private int pageSize;
    private int pageCount;
    private int pageNow;
    private String boardChsName;


    private List<Post> postlist = new ArrayList<Post>();

    public int getPageSize() {
        return pageSize;
    }


    public List<Post> getPostlist() {
        return postlist;
    }


    public int getPageCount() {
        return pageCount;
    }

    public int getPageNow() {
        return pageNow;
    }

    public String getBoardChsName() {
        return boardChsName;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
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
}
