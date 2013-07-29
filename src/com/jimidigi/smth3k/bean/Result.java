package com.jimidigi.smth3k.bean;

import java.io.Serializable;


import com.jimidigi.smth3k.common.StringUtility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 数据操作结果实体类
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Result implements Serializable {

    public static final int NOTLOGIN = 1;
    public static final String NOTLOGINSTR = "您没有登录，请重试";

    public static final int NOTEXIT = 4;
    public static final String NOTEXITSTR = "找不到了，你再找找别的吧。";


    private int atmeCount;
    private boolean newMail;
    private int replyCount;

    private int errorCode;
    private String errorMessage;

    public final static int typeAtme = 1;
    public final static int typeNewMail = 2;
    public final static int typeReply = 3;

    private boolean ok;

    public void Result(int atmeCount, boolean newMail, int replyCount) {
        this.atmeCount = atmeCount;
        this.newMail = newMail;
        this.replyCount = replyCount;
        this.ok = true;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void parseNotice(String html) {
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        Element

                tmp = doc.select("[class=menu nav]").first();

        if (tmp != null) {

            Elements a = tmp.getElementsByTag("a");

            if (a != null && a.size() == 9) {
                if (a.get(5) != null && a.get(5).text().contains("新")) {
                    this.newMail = true;
                }
                if (a.get(6) != null) {
                    this.atmeCount = StringUtility.filterUnNumber(a.get(6).text());
                }
                if (a.get(7) != null) {
                    this.replyCount = StringUtility.filterUnNumber(a.get(7).text());
                }
            }
        }


    }


    public void parseNotice(Document doc) {

        Elements

                tmp = doc.select("[class=menu nav] a[accesskey=5]");

        if (StringUtility.isNotEmpty(tmp)) {


            if (tmp.text().contains("新")) {
                this.newMail = true;
            }
        }

        tmp = doc.select("[class=menu nav] a[accesskey=6]");

        if (StringUtility.isNotEmpty(tmp)) {


            this.atmeCount = StringUtility.filterUnNumber(tmp.text());
        }


        tmp = doc.select("[class=menu nav] a[accesskey=7]");

        if (StringUtility.isNotEmpty(tmp)) {


            this.replyCount = StringUtility.filterUnNumber(tmp.text());
        }


    }

    public static Result getError(int code, String errorMessage) {

        Result result = new Result();
        result.setOk(false);
        result.setErrorCode(code);
        result.setErrorMessage(errorMessage);
        return result;
    }

    @Override
    public String toString() {
        return String.format("RESULT: CODE:%d,MSG:%s", errorCode, errorMessage);
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public int getAtmeCount() {
        return atmeCount;
    }

    public void setAtmeCount(int atmeCount) {
        this.atmeCount = atmeCount;
    }

    public boolean isNewMail() {
        return newMail;
    }

    public void setNewMail(boolean newMail) {
        this.newMail = newMail;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }
}
