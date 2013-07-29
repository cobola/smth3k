package com.jimidigi.smth3k.api;

import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.bean.*;



/**
 * API客户端接口：用于访问网络数据
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ApiClient {

    public static final String UTF_8 = "UTF-8";
    public static final String DESC = "descend";
    public static final String ASC = "ascend";

    private final static int TIMEOUT_CONNECTION = 20000;
    private final static int TIMEOUT_SOCKET = 20000;
    private final static int RETRY_TIME = 3;

    private static String appCookie;
    private static String appUserAgent;

    public static void cleanCookie() {
        appCookie = "";
    }

    private static String getCookie(AppContext appContext) {
        if (appCookie == null || appCookie == "") {
            appCookie = appContext.getProperty("cookie");
        }
        return appCookie;
    }







    /**
     * post请求URL
     *
     * @param url
     * @param params
     * @param files
     * @throws AppException
     * @throws IOException
     * @throws
     */
    private static Result http_post(AppContext appContext, String url, Map<String, Object> params, Map<String, File> files) throws AppException, IOException {
        return new Result();
    }



    /**
     * 检查版本更新
     *
     * @return
     */
    public static Update checkVersion(AppContext appContext) throws AppException {
        try {
            Update update = new Update();
            return update;

        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }




    /**
     * 清空通知消息
     *
     * @param uid
     * @param type 1:@我的信息 2:未读消息 3:评论个数 4:新粉丝个数
     * @return
     * @throws AppException
     */
    public static Result noticeClear(AppContext appContext, String uid, int type) throws AppException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("uid", uid);
        params.put("type", type);

        try {

            return Result.getError(1,"");
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }





}
