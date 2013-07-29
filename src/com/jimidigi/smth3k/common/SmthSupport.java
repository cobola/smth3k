package com.jimidigi.smth3k.common;

import android.graphics.Bitmap;
import android.util.Log;
import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.bean.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SmthSupport {
    private String userid;
    private String passwd;
    private boolean loginned;
    SmthCrawler crawler;

    private static class Holder {
        private static SmthSupport instance = new SmthSupport();
    }

    public static SmthSupport getInstance() {
        return Holder.instance;
    }

    private SmthSupport() {
        crawler = SmthCrawler.getIntance();
    }

    public void restore() {
        if (crawler.isDestroy()) {
            crawler.init();
        }
    }

    public boolean getLoginStatus() {
        return loginned;
    }


    /**
     * 登录.
     */
    public List login() {
        loginned = true;
        List list = crawler.login(userid, passwd);
        if (list.isEmpty()) {
            loginned = false;
        } else {
            loginned = false;
        }
        return list;
    }

    /**
     * 退出登录.
     */
    public void logout() {
        if (loginned) {
            crawler.getUrlContent("http://m.newsmth.net/user/logout");
            loginned = false;
        }
    }

    /**
     * 退出登录，并关闭httpclient
     */
    public void destory() {
        logout();
        crawler.destroy();
    }

    /**
     * @param mailUrl
     * @param mailTitle
     * @param userid
     * @param backup
     * @param mailContent
     * @return
     */
    public Result sendMail(String mailUrl, String mailTitle, String userid, String backup, String mailContent) {
        return crawler.sendMail(mailUrl, mailTitle, userid, backup, mailContent);
    }

    public Result delMail(String url) {
        String content = crawler.getUrlContent(url);
        if (content == null) {
            Result.getError(1, "null");
        }

        org.jsoup.nodes.Document doc = Jsoup.parse(content);

        if (doc.text().contains("邮件删除成功")) {
            Result result = new Result();
            result.setOk(true);
            result.parseNotice(doc.select("[class=sp hl f]").text());
            return result;
        }

        return Result.getError(1, "error");
    }

    public Result delRef(String url) {
        url = URLs.HOST + url.replace("read", "delete");
        String content = crawler.getUrlContent(url);
        if (content == null) {
            Result.getError(1, "null");
        }

        org.jsoup.nodes.Document doc = Jsoup.parse(content);

        if (doc.text().contains("删除成功")) {
            Result result = new Result();
            result.setOk(true);
            result.parseNotice(doc.select("[class=sp hl f]").text());
            return result;
        }

        return Result.getError(Result.NOTEXIT, Result.NOTEXITSTR);
    }


    public Result sendPost(String postUrl, String postTitle, String postContent) {
        return crawler.sendPost(postUrl, postTitle, postContent);
    }

    public Result sendQuickReply(String postUrl, String postTitle, String postContent) {
        return crawler.sendQuickReply(postUrl, postTitle, postContent);
    }

    public String getUrlContent(String urlString) {
        return crawler.getUrlContent(urlString);
    }


    public static Bitmap getNetBitmap(String url) throws AppException {
        //System.out.println("image_url==> "+url);

        Bitmap bitmap = null;

        return bitmap;
    }


    public static PostList parseHot(String html, int page) throws IOException, AppException {
        PostList postlist = new PostList();
        Post post = null;
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        Elements newsList = doc.select("[class=slist sec]").first().getElementsByTag("li");
        int i = 0;

        for (Element e : newsList) {
            post = new Post();
            if (e.attr("class") != null && e.attr("class").equals("f")) {
                post.setTitle(e.text());
                postlist.getPostlist().add(post);
            }
            Element title = e.select("a").first();
            if (title != null) {
                post.setTitle(title.text());

                try {
//                    post.setReplys(Integer.parseInt(title.getElementsByTag("span").text()));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                String href = e.select("a").attr("href");
                String tmp[] = href.split("/");
                if (tmp != null && tmp.length == 4) {
                    post.setSubjectID(tmp[3]);
                    post.setBoardEngName(tmp[2]);
                }
                post.setFloor(StringUtility.substring(e.text(), e.text().indexOf("\"") + 1, e.text().indexOf("|")));
                postlist.getPostlist().add(post);
                i++;
            }


        }
        postlist.setPageNow(i);
        postlist.setPageSize(page);
        return postlist;
    }


    public Post getPostSingle(AppContext appContext, final String boardEngName, final String postID) throws AppException {
        String newUrl = URLs.POST_SINGLE + "/" + boardEngName + "/single/" + postID;
        try {
            String result = crawler.getUrlContent(newUrl);
            if (result == null) {
                return null;
            }
            Post post = new Post();
            post.setPostID(postID);
            post.setBoardEngName(boardEngName);
            //解析post 这个其实是按照主题解析的 所以他的回复也会一块解析出来 所以这儿出来的 包括post和所有的reply 当前页面
            org.jsoup.nodes.Document doc = Jsoup.parse(result);


            Elements tmp = doc.select(".f");
            if (StringUtility.isNotEmpty(tmp)) {
                post.setTitle(tmp.first().text());
            }

            tmp = doc.select("[class=nav hl] > a");
            if (StringUtility.isNotEmpty(tmp)) {
                post.setAuthor(tmp.get(0).text());
                post.setDate(StringUtility.parseDate(tmp.get(1).text()));

            }


            tmp = doc.select("[class=sec nav] > a");

            if (StringUtility.isNotEmpty(tmp)) {
                post.setSubjectID(tmp.get(3).text());
            }
            post.setContent(doc.select("[class=sp]").html());


            return post;
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public Post getPostSingle(AppContext appContext, final String url) throws AppException {
        try {
            String result = crawler.getUrlContent(URLs.HOST + url);
            if (result == null) {
                return null;
            }

            org.jsoup.nodes.Document doc = Jsoup.parse(result);

            Post post = new Post();
            Elements tmp = doc.select("a[href]:contains(展开)");

            if (StringUtility.isNotEmpty(tmp)) {
                String href = tmp.get(0).attr("href");
                post.setPostID(href.substring(href.indexOf("=") + 1));
                post.setBoardEngName(href.split("/")[2]);
                post.setSubjectID(href.substring(href.lastIndexOf("/") + 1, href.lastIndexOf("?")));

            }
            tmp = doc.select("li.f");
            if (StringUtility.isNotEmpty(tmp)) {
                post.setTitle(tmp.text());
            }

            tmp = doc.select("[class=nav hl] a");


            if (StringUtility.isNotEmpty(tmp) && tmp.size() > 2) {
                post.setAuthor(tmp.get(0).text());
                post.setDate(StringUtility.parseDate(tmp.get(1).text()));
            }

            post.setContent(doc.select("[class=sp]").html());


            return post;
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }


    public SubjectList getSearchResult(int type, String str) throws AppException {
        SubjectList subjectList;

        switch (type) {
            case UIHelper.LISTVIEW_DATATYPE_BOARD:
                String
                        newUrl = URLs.BOARDSEARCH + str;


                String result = crawler.getUrlContent(newUrl);
                if (result == null) {
                    return null;
                }

                try {
                    subjectList = new SubjectList();
                    org.jsoup.nodes.Document doc = Jsoup.parse(result);

                    Elements tmp = doc.select("li");
                    for (Element e : tmp) {

                        Subject subject = new Subject();
                        subject.setAsPost(false);
                        subject.setDatatype(UIHelper.LISTVIEW_DATATYPE_BOARD);
                        subject.setTitle(e.text());
                        Element a = e.select("a").first();
                        subject.setUrl(a.attr("href"));
                        subjectList.getSubjectList().add(subject);

                    }


                    subjectList.setPageNow(1);
                    subjectList.setPageCount(1);


                } catch (Exception e) {
                    if (e instanceof AppException)
                        throw (AppException) e;
                    throw AppException.network(e);
                }

                break;

            case UIHelper.LISTVIEW_DATATYPE_USER:


        }

        return null;


    }


    public SubjectList getHotSubjectList(AppContext appContext) throws AppException {

        String newUrl = URLs.HOTSUBJECT;

        String result = crawler.getUrlContent(newUrl, SmthCrawler.GBK);
        if (result == null) {
            return null;
        }


        try {
            SubjectList subjectList = new SubjectList();
            org.jsoup.nodes.Document doc = Jsoup.parse(result);

            Elements tmp = doc.select(".HotTitle");
            subjectList.setBoardChsName("十大热门");

            for (Element e : tmp) {

                Subject subject = new Subject();
                subject.setAsPost(false);
                subject.setDatatype(UIHelper.LISTVIEW_DATATYPE_SUBJECT_HOT);

                Element a = e.select("a").first();
                subject.setBoardChsName(a.text());
                a = e.select("a").get(1);

                subject.setTitle(a.text());
                String href = a.attr("href");
                subject.setSubjectID(href.substring(href.indexOf("gid=") + 4));

                subject.setBoardEngName(StringUtility.substring(href, href.indexOf("board=") + 6, href.indexOf("&")));
                subjectList.getSubjectList().add(subject);

            }


            tmp = doc.select(".SectionItem");

            for (Element e : tmp) {

                Subject subject = new Subject();
                subject.setAsPost(false);
                subject.setDatatype(UIHelper.LISTVIEW_DATATYPE_SUBJECT_HOT);

                Element a = e.select("a").first();
                subject.setBoardChsName(a.text());
                a = e.select("a").get(1);

                subject.setTitle(a.text());
                String href = a.attr("href");
                subject.setSubjectID(href.substring(href.indexOf("gid=") + 4));

                subject.setBoardEngName(StringUtility.substring(href, href.indexOf("board=") + 6, href.indexOf("&")));
                subjectList.getSubjectList().add(subject);

            }


            subjectList.setPageNow(1);
            subjectList.setPageCount(1);
            return subjectList;


        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }


    public Mail getMail(String boxType, int id) {

        try {
            String url = URLs.HOST + boxType + "/" + id;
            String crawlerUrlContent = crawler.getUrlContent(url);
            if (crawlerUrlContent == null) {
                return null;
            }

            org.jsoup.nodes.Document doc = Jsoup.parse(crawlerUrlContent);

            Elements tmp = doc.select("li.f");
            if (StringUtility.isNotEmpty(tmp)) {

                Mail mail = new Mail();
                mail.setTitle(tmp.text().replace("标题:", ""));

                mail.setContent(doc.select(".list .sp").html());
                mail.setBoxType(boxType);
                mail.setDate(StringUtility.parseDate(doc.select(".plant").text()));
                mail.setSenderID(doc.select("a").first().text());
                mail.setId(id);

                return mail;
            }
        } catch (Exception e) {

            Log.e("smth3k", e.toString());
        }
        return null;

    }

    public MailList getMailList(String boxType, int pageIndex) throws AppException {

        String url = URLs.HOST + boxType + "?p=" + pageIndex;

        String crawlerUrlContent = crawler.getUrlContent(url);
        if (crawlerUrlContent == null) {
            return null;
        }


        org.jsoup.nodes.Document doc = Jsoup.parse(crawlerUrlContent);


        Elements tmp = doc.select("[class=menu sp]");
        if (StringUtility.isNotEmpty(tmp)) {

            MailList mailList = new MailList();
            mailList.setTitle(tmp.text());

            mailList.setBoxType(boxType);
            tmp = doc.select(".plant");
            if (StringUtility.isNotEmpty(tmp)) {

                mailList.setTitle(mailList.getTitle() + " " + tmp.first().text());
                String tmp2[] = tmp.first().text().split("/");
                if (tmp2 != null && tmp2.length == 2) {
                    mailList.setPageNow(Integer.parseInt(tmp2[0]));
                    mailList.setPageCount(Integer.parseInt(tmp2[1]));
                }
            }


            for (Element e : doc.select("li")) {
                Mail mail = new Mail();
                mail.setBoxType(boxType);
                mail.setType(Mail.TypeMail);

                Element firstA = e.select("a").first();
                mail.setTitle(firstA.text());

                mail.setFloor((mailList.getPageNow() - 1) * 10 + StringUtility.filterUnNumber(e.text().substring(0, 3)));
                mail.setId(StringUtility.filterUnNumber(firstA.attr("href")));

                mail.setSenderID(e.select("a").get(1).text());
                mail.setDate(StringUtility.parseDate(e.text()));
                mailList.getMailList().add(mail);

            }

            Result result = new Result();
            result.parseNotice(doc);
            mailList.setResult(result);

            return mailList;
        }
        return null;
    }

    public MailList getRefList(AppContext appContext, String type, int pageIndex) throws AppException {


        String url = URLs.HOST + type + "?p=" + pageIndex;

        String crawlerUrlContent = crawler.getUrlContent(url);
        if (crawlerUrlContent == null) {
            return null;
        }


        org.jsoup.nodes.Document doc = Jsoup.parse(crawlerUrlContent);


        Elements tmp = doc.select(".menu");
        if (StringUtility.isNotEmpty(tmp)) {

            MailList mailList = new MailList();
            mailList.setTitle(tmp.first().text());

            mailList.setBoxType(type);
            tmp = doc.select(".plant");
            if (StringUtility.isNotEmpty(tmp)) {

                mailList.setTitle(mailList.getTitle() + " " + tmp.first().text());
                String tmp2[] = tmp.first().text().split("/");
                if (tmp2 != null && tmp2.length == 2) {
                    mailList.setPageNow(Integer.parseInt(tmp2[0]));
                    mailList.setPageCount(Integer.parseInt(tmp2[1]));
                }
            }


            for (Element e : doc.select("li")) {
                Mail mail = new Mail();
                mail.setBoxType(type);
                mail.setType(Mail.TypeRef);

                Element firstA = e.select("a").first();
                mail.setTitle(firstA.text());

                mail.setFloor((mailList.getPageNow() - 1) * 10 + e.siblingIndex() + 1);
                mail.setId(StringUtility.filterUnNumber(firstA.attr("href")));

                mail.setSenderID(e.select("a").get(2).text());
                mail.setDate(StringUtility.parseDate(e.text()));
                mail.setUrl(firstA.attr("href"));

                mailList.getMailList().add(mail);

            }

            Result result = new Result();
            result.parseNotice(doc);
            mailList.setResult(result);

            return mailList;
        }
        return null;
    }

//

    /**
     * 获取分类讨论区列表
     *
     * @return
     */
    public Board getBoardList(Board parent) throws AppException {
        String newUrl = URLs.HOST + parent.getUrl();
        try {
            String result = crawler.getUrlContent(newUrl);
            if (result == null) {
                return null;
            }


            org.jsoup.nodes.Document doc = Jsoup.parse(result);

            Elements tmp = doc.select("[class=sec sp] a");
            if (StringUtility.isNotEmpty(tmp)) {
                parent.setParentUrl(tmp.attr("href"));
            } else {
                parent.setParentUrl(parent.getUrl());
            }
            tmp = doc.select("[class=menu sp]");
            if (StringUtility.isNotEmpty(tmp)) {
                parent.setParentChsName(tmp.text().replace("讨论区-", ""));
            } else {
                parent.setParentChsName(parent.getChsName());
            }

            tmp = doc.select("li a:not([style])");
            for (Element li : tmp) {
                Board bd = new Board();
                bd.setChsName(li.text());
                bd.setUrl(li.attr("href"));
                String tmps[] = bd.getUrl().split("/");
                if (tmps != null && tmps.length == 3) {
                    bd.setEngName(tmps[2]);
                    if (tmps[1] != null && tmps[1].equals("section")) {
                        bd.setDirectory(true);
                    }
                    if (tmps[1] != null && tmps[1].equals("board")) {
                        bd.setDirectory(false);
                    }
                }

                parent.getChildBoards().add(bd);
            }


            return parent;

        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }


    /**
     * 获取版面主题列表.
     *
     * @return
     */
    public SubjectList getSubjectList(AppContext context, String boardEngName, int boardType, int pageIndex, ArrayList<String> blackList) {

        if (pageIndex < 1) pageIndex = 1;

        String newUrl = URLs.POST_BY_BOARD_LIST + "/" + boardEngName + "/" + boardType + "/?p=" + pageIndex;
        if (boardType == UIHelper.LISTVIEW_BOARD_TYPE_SUBJECT) {
            newUrl = URLs.POST_BY_BOARD_LIST + "/" + boardEngName + "/?p=" + pageIndex;
        }

        String result = crawler.getUrlContent(newUrl);
        if (result == null) {
            return null;
        }

        SubjectList subjectList = new SubjectList();
        subjectList.setBoardType(boardType);
        org.jsoup.nodes.Document doc = Jsoup.parse(result);

        Elements plant = doc.select(".plant");
        if (StringUtility.isNotEmpty(plant)) {
            String tmp[] = plant.first().text().split("/");
            if (tmp != null && tmp.length == 2) {
                subjectList.setPageNow(Integer.parseInt(tmp[0]));
                subjectList.setPageCount(Integer.parseInt(tmp[1]));
            }
        }

        plant = doc.select("[class=menu sp]");
        if (plant != null) {
            subjectList.setBoardChsName(plant.text().replace("版面-", ""));
        }

        plant = doc.select("li");

        int i = 0;
        for (Element e : plant) {


            i++;
            Subject subject = new Subject();

            Element title = e.select("a").first();
            if (title != null) {
                subject.setTitle(title.text());
                String href = title.attr("href");
                subject.setUrl(href);
                if (StringUtility.isNotEmpty(e.attr("class")) && e.attr("class").equals("top")) {
                    subject.setTop(true);
                }

            }


            switch (boardType) {
                case UIHelper.LISTVIEW_BOARD_TYPE_CLASSIC:
                case UIHelper.LISTVIEW_BOARD_TYPE_G:
                case UIHelper.LISTVIEW_BOARD_TYPE_M:
                    //这儿的subject的ID 就是single后面的那串
                    subject.setAsPost(true);
//
                    subject.setAuthor(e.select("a").get(1).text());

                    String t2 = e.select("div").get(1).text();
                    subject.setFloor(StringUtility.filterUnNumber(StringUtility.substring(t2, 0, t2.indexOf((char) 160))) + "");
                    subject.setDate(StringUtility.parseDate(e.select("div").get(1).text()));
                    subject.setDatatype(UIHelper.LISTVIEW_DATATYPE_POST_BOARD);

                    break;
                case UIHelper.LISTVIEW_BOARD_TYPE_SUBJECT:
                    String tmp[] = subject.getUrl().split("/");
                    if (tmp != null && tmp.length == 4) {
                        subject.setSubjectID(tmp[3]);
                        subject.setBoardEngName(tmp[2]);
                    }
                    String t = e.select("div:first-child").text();
                    t = StringUtility.substring(t, t.lastIndexOf("(") + 1, t.lastIndexOf(")"));
                    subject.setReplys(Integer.parseInt(t));

                    subject.setAuthor(e.select("a").get(1).text());
                    t = e.select("div").get(1).text();
                    subject.setDate(StringUtility.parseDate(t.substring(0, t.indexOf("|"))));

                    if (e.select("a").size() == 3) {
                        subject.setLastAuthor(e.select("a").get(2).text());
                        subject.setLastDate(StringUtility.parseDate(t.substring(t.indexOf("|") + 1)));
                    }

                    subject.setFloor(((pageIndex - 1) * 30 + i) + "");
                    subject.setDatatype(UIHelper.LISTVIEW_DATATYPE_SUBJECT_BOARD);
                    break;
            }


            subjectList.getSubjectList().add(subject);

        }


        return subjectList;
    }

    /**
     * 获取同主题帖子列表.
     *
     * @return
     */
    public Subject getSubject(AppContext content, String boardEngName, String subjectID, ArrayList<String> blackList, int pageIndex) throws AppException {
        String newUrl = URLs.POST_DETAIL + "/" + boardEngName + "/" + subjectID + "?p=" + pageIndex;

        String result = crawler.getUrlContent(newUrl);
        if (result == null) {
            return null;
        }
        org.jsoup.nodes.Document doc = Jsoup.parse(result);

        Subject subject = new Subject();
        subject.setSubjectID(subjectID);

        Elements tmp = doc.select("[class=plant]");

        if (StringUtility.isNotEmpty(tmp)) {
            String tmps[] = tmp.first().text().split("/");
            if (tmps != null && tmps.length == 2) {
                subject.setCurrentPageNo(Integer.parseInt(tmps[0]));
                subject.setTotalPageNo(Integer.parseInt(tmps[1]));
            }
        }

        //

        tmp = doc.select("li[class=f]");
        subject.setTitle(tmp.text());

        tmp = doc.select("li:not(.f)");
        for (Element e : tmp) {


            //这就是帖子 如果plant为楼主 则是楼主

//            Element t = e.select("[class=plant]").first();

            Post post = new Post();
            post.setSubjectID(subjectID);

            Elements a = e.select("a");
            if (a.size() > 4) {
                post.setFloor(a.get(1).text());
                if (StringUtility.isNotEmpty(post.getFloor()) && "楼主".equals(post.getFloor())) {
                    post.setTitle(subject.getTitle());
                }
                post.setAuthor(a.get(2).text());
                post.setDate(StringUtility.parseDate(a.get(3).text()));
                String link = a.get(4).attr("href");
                post.setPostID(link.substring(link.lastIndexOf("/"), link.lastIndexOf("?")));

            }
            post.setContent(StringUtility.filterNullBr(e.select(".sp").html()));
            subject.getReplylist().add(post);
        }


        return subject;
    }


    public User getUser(String userID) throws AppException {
        User user = new User();
        user.setUserID(userID);
        String url = URLs.HOST + "/user/query/" + userID;
        String html = crawler.getUrlContent(url);
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        Element tmp = doc.select("[class=sec list]").first();
        for (Element e : tmp.getElementsByTag("li")) {
            String tt = e.text();
            String tt2 = StringUtility.substring(tt, tt.indexOf((char) 160) + 1, tt.length());
            if (tt.contains("昵称")) {
                user.setNickName(tt2);
            }
            if (tt.contains("性别")) {
                user.setGender(tt2);
            }
            if (tt.contains("星座")) {
                user.setConstellation(tt2);
            }

            if (tt.contains("等级")) {
                user.setLevel(tt2);
            }


            if (tt.contains("贴数")) {
                try {

                    user.setPostNumber(Integer.parseInt(tt2));
                } catch (Exception e1) {
                    e1.printStackTrace();

                }
            }
            if (tt.contains("登陆次数")) {
                try {
                    user.setLoginTimes(Integer.parseInt(tt2));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            if (tt.contains("生命力")) {
                try {

                    user.setAliveness(Integer.parseInt(tt2));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            if (tt.contains("注册时间")) {
                user.setRegDate(tt2);
            }

            if (tt.contains("上次登录")) {
                user.setLastLoginDate(StringUtility.parseDate(tt2));
            }

            if (tt.contains("最后访问IP")) {
                user.setLastIp(tt2);
            }

            if (tt.contains("当前状态")) {
                user.setStatus(tt2);
            }
        }

        return user;

    }


    public String getPostContent(String boardid, String subjectid) {
        String url = "http://www.newsmth.net/bbscon.php?bid=" + boardid
                + "&id=" + subjectid;
        return crawler.getUrlContent(url);
    }

    private String getPostListContent(String board, String subjectid, int pageno, int startNumber) {
        String url = "http://www.newsmth.net/bbstcon.php?board=" + board
                + "&gid=" + subjectid;
        if (pageno > 0) {
            url += "&pno=" + pageno;
        }
        if (startNumber > 0) {
            url += "&start=" + startNumber;
        }
        return crawler.getUrlContent(url);
    }

    public String getMainSubjectList(String board, int pageno, int type) {
        String url = "";
        if (type == 0) {
            url = "http://www.newsmth.net/bbsdoc.php?board=" + board + "&ftype=6";
        } else if (type == 1) {
            url = "http://www.newsmth.net/bbsdoc.php?board=" + board + "&ftype=0";
        } else if (type == 2) {
            url = "http://www.newsmth.net/bbsdoc.php?board=" + board + "&ftype=1";
        } else {
            url = "http://www.newsmth.net/bbsdoc.php?board=" + board + "&ftype=3";
        }
        if (pageno > 0) {
            url = url + "&page=" + pageno;
        }
        return crawler.getUrlContent(url);
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}
