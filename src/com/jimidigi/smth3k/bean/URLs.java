package com.jimidigi.smth3k.bean;

import java.io.Serializable;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;


/**
 * 接口URL实体类
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class URLs implements Serializable {

    public final static String HOST = "http://m.newsmth.net";//192.168.1.213  www.jimidigi.net

    private final static String URL_SPLITTER = "/";

    public final static String HOTSUBJECT = "http://www.newsmth.net/mainpage.html";

    public final static String BOARDSEARCH = "http://m.newsmth.net/go?name=";
    public final static String POST_DETAIL = HOST + "/article";
    public final static String POST_SINGLE = HOST + "/article";

    public final static String BOARD_LIST = HOST + "/section";

    public final static String HOT_BOARD_LIST = HOST + "/hot";
    public final static String POST_BY_BOARD_LIST = HOST + "/board";


}
