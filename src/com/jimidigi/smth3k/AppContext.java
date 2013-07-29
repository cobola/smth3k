package com.jimidigi.smth3k;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import com.jimidigi.smth3k.api.ApiClient;
import com.jimidigi.smth3k.bean.*;
import com.jimidigi.smth3k.common.*;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.webkit.CacheManager;
import com.jimidigi.smth3k.common.SmthSupport;
import com.jimidigi.smth3k.common.StringUtility;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class AppContext extends Application {

    public static final int NETTYPE_WIFI = 0x01;
    public static final int NETTYPE_CMWAP = 0x02;
    public static final int NETTYPE_CMNET = 0x03;

    private static final int CACHE_TIME = 60 * 60000;//缓存失效时间


    public static final int PAGE_SIZE = 10;
    private boolean login = false;    //登录状态
    private String loginUserID = "guest";    //登录用户的id
    private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();

    private SmthSupport smthSupport;

    private Handler unLoginHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                UIHelper.ToastMessage(AppContext.this, getString(R.string.msg_login_error));
                UIHelper.showLoginDialog(AppContext.this);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //注册App异常崩溃处理器
        smthSupport = SmthSupport.getInstance();
        Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
    }

    /**
     * 检测当前系统声音是否为正常模式
     *
     * @return
     */
    public boolean isAudioNormal() {
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    /**
     * 应用程序是否发出提示音
     *
     * @return
     */
    public boolean isAppSound() {
        return isAudioNormal() && isVoice();
    }

    /**
     * 检测网络是否可用
     *
     * @return
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * 获取当前网络类型
     *
     * @return 0：没有网络   1：WIFI网络   2：WAP网络    3：NET网络
     */
    public int getNetworkType() {
        int netType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!StringUtility.isEmpty(extraInfo)) {
                if (extraInfo.toLowerCase().equals("cmnet")) {
                    netType = NETTYPE_CMNET;
                } else {
                    netType = NETTYPE_CMWAP;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NETTYPE_WIFI;
        }
        return netType;
    }

    /**
     * 判断当前版本是否兼容目标版本的方法
     *
     * @param VersionCode
     * @return
     */
    public static boolean isMethodsCompat(int VersionCode) {
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        return currentVersion >= VersionCode;
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null) info = new PackageInfo();
        return info;
    }

    /**
     * 获取App唯一标识
     *
     * @return
     */
    public String getAppId() {
        String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
        if (StringUtility.isEmpty(uniqueID)) {
            uniqueID = UUID.randomUUID().toString();
            setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
        }
        return uniqueID;
    }

    /**
     * 用户是否登录
     *
     * @return
     */
    public boolean isLogin() {
        return login;
    }

    /**
     * 获取登录用户id
     *
     * @return
     */
    public String getLoginUserID() {
        return this.loginUserID;
    }

    /**
     * 用户注销
     */
    public void Logout() {
        ApiClient.cleanCookie();
        this.cleanCookie();
        this.login = false;
        this.loginUserID = "guest";
    }

    /**
     * 未登录或修改密码后的处理
     */
    public Handler getUnLoginHandler() {
        return this.unLoginHandler;
    }

    /**
     * 初始化用户登录信息
     */
    public List initLoginInfo() {
        User loginUser = getLoginInfo();

        if (loginUser != null && loginUser.getUserID() != null && loginUser.isRemberme()) {
            this.loginUserID = loginUser.getUserID();
            smthSupport.setUserid(loginUser.getUserID());
            smthSupport.setPasswd(loginUser.getPassword());
            List list = smthSupport.login();
            if (!list.isEmpty()) {
                this.login = true;
            }
            return list;
        } else {
            this.Logout();
        }
        return Collections.emptyList();
    }

    /**
     * 用户登录验证
     *
     * @param userid
     * @param passwd
     * @return
     * @throws AppException
     */
    public List loginVerify(String userid, String passwd) throws AppException {
        smthSupport.setUserid(userid);
        smthSupport.setPasswd(passwd);
        return smthSupport.login();
    }

    /**
     * 我的个人资料
     *
     * @param isRefresh 是否主动刷新
     * @return
     * @throws AppException
     */
    public User getMyInformation(boolean isRefresh) throws AppException {
        User myinfo = null;
        String key = "myinfo_" + loginUserID;
        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                myinfo = smthSupport.getUser(loginUserID);
                if (myinfo != null && myinfo.getNickName().length() > 0) {
                    myinfo.setCacheKey(key);
                    saveObject(myinfo, key);
                }
            } catch (AppException e) {
                myinfo = (User) readObject(key);
                if (myinfo == null)
                    throw e;
            }
        } else {
            myinfo = (User) readObject(key);
            if (myinfo == null)
                myinfo = new User();
        }
        return myinfo;
    }


    /**
     * 更新用户头像
     *
     * @param portrait 新上传的头像
     * @return
     * @throws AppException
     */
    public Result updatePortrait(File portrait) throws AppException {
        return null;
    }

    /**
     * 清空通知消息
     *
     * @param uid
     * @param type 1:@我的信息 2:未读消息 3:评论个数 4:新粉丝个数
     * @return
     * @throws AppException
     */
    public Result noticeClear(String uid, int type) throws AppException {
        return ApiClient.noticeClear(this, uid, type);
    }


    /**
     * 帖子列表
     *
     * @return
     */
    public SubjectList getHotSubjectList(boolean isRefresh) throws AppException {
        SubjectList list = null;
        String key = "hostsubjectlist_";
        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                list = smthSupport.getHotSubjectList(this);
                if (list != null) {
                    list.setCacheKey(key);
                    saveObject(list, key);
                }
            } catch (AppException e) {
                list = (SubjectList) readObject(key);
                if (list == null)
                    throw e;
            }
        } else {
            list = (SubjectList) readObject(key);
            if (list == null)
                list = new SubjectList();
        }
        return list;
    }

    /**
     * 帖子列表
     *
     * @return
     */
    public SubjectList getSearchResult(int type,String str) throws AppException {
        SubjectList list= smthSupport.getSearchResult(type,str);
        return list;
    }


    /**
     * 帖子列表
     *
     * @param pageIndex
     * @return
     */
    public MailList getRefList(String boxType, int pageIndex, boolean isRefresh) throws AppException {
        MailList list = null;
        String key = "subjectlist_" + StringUtility.filterUrl(boxType) + pageIndex;

        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                list = smthSupport.getRefList(this, boxType, pageIndex);
                if (list != null && pageIndex == 0) {
                    list.setCacheKey(key);
                    saveObject(list, key);
                }
            } catch (AppException e) {
                list = (MailList) readObject(key);
                if (list == null)
                    throw e;
            }
        } else {
            list = (MailList) readObject(key);
            if (list == null)
                list = new MailList();
        }
        return list;
    }




    /**
     * 读取帖子详情
     *
     * @param subjectID
     * @return
     */
    public Subject getSubject(String boardEngName, String subjectID, int pageIndex, boolean isRefresh) throws AppException {

        Subject subject = null;
        String key = "postkey_board_" + boardEngName + "post_" + subjectID + "_" + pageIndex;
        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                subject = smthSupport.getSubject(this, boardEngName, subjectID, null, pageIndex);
                if (subject != null) {
                    subject.setCacheKey(key);
                    saveObject(subject, key);
                }
            } catch (AppException e) {
                subject = (Subject) readObject(key);
                if (subject == null)
                    throw e;
            }
        } else {
            subject = (Subject) readObject(key);
            if (subject == null)
                subject = new Subject();
        }
        return subject;
    }


    /**
     * 读取帖子详情
     *
     * @return
     */
    public Post getPost(String url, boolean isRefresh) throws AppException {

        Post post = null;
        String key = "post_" + StringUtility.filterUrl(url);
        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                post = smthSupport.getPostSingle(this, url);
                if (post != null) {
                    post.setCacheKey(key);
                    saveObject(post, key);
                }
            } catch (AppException e) {
                post = (Post) readObject(key);
                if (post == null)
                    throw e;
            }
        } else {
            post = (Post) readObject(key);
            if (post == null)
                post = new Post();
        }
        return post;
    }

    /**
     * 读取帖子详情
     *
     * @return
     */
    public String getURL(String url, boolean isRefresh) throws AppException {

        String str = null;
        String key = "post_" + StringUtility.filterUrl(url);
        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                str = smthSupport.getUrlContent(url);
                if (str != null) {
                    saveObject(str, key);
                }
            } catch (Exception e) {
                str = (String) readObject(key);
            }
        } else {
            str = (String) readObject(key);
        }
        return str;
    }


    /**
     * @return
     * @throws AppException
     */
    public SubjectList getSubjectListByBoard(String boardEngName, int type, int page, boolean isRefresh) throws AppException {
        SubjectList list = null;
        String key = "subjectlist_" + boardEngName + "_" + type + "_" + page;
        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                list = smthSupport.getSubjectList(this, boardEngName, type, page, null);
                if (list != null) {
                    list.setCacheKey(key);
                    saveObject(list, key);
                }
            } catch (Exception e) {
                list = (SubjectList) readObject(key);
            }
        } else {
            list = (SubjectList) readObject(key);
            if (list == null)
                list = new SubjectList();
        }
        return list;
    }

    /**
     * @return
     * @throws AppException
     */
    public Board getFavBoardList(Board board, boolean isRefresh) throws AppException {
        String key = "boardlist_" + getLoginUserID() + "_" + board.getEngName();
        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                board = smthSupport.getBoardList(board);
                if (board != null) {

                    board.setCacheKey(key);
                    saveObject(board, key);
                }
            } catch (AppException e) {
                board = (Board) readObject(key);
                if (board == null)
                    throw e;
            }
        } else {
            board = (Board) readObject(key);
            if (board == null)
                board = new Board();
        }
        return board;
    }


    /**
     * @return
     * @throws AppException
     */
    public Board getBoardList(Board parent, boolean isRefresh) throws AppException {
        String key = "boardlist_" + parent.getEngName();
        Board board = null;
        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                board = smthSupport.getBoardList(parent);
                if (board != null) {

                    board.setCacheKey(key);
                    saveObject(board, key);
                }
            } catch (AppException e) {
                board = (Board) readObject(key);
                if (board == null)
                    throw e;
            }
        } else {
            board = (Board) readObject(key);
            if (board == null)
                board = new Board();
        }
        return board;
    }

    /**
     * 邮箱列表
     *
     * @param pageIndex
     * @return
     * @throws AppException
     */
    public MailList getMailList(String type, int pageIndex, boolean isRefresh) throws AppException {
        MailList list = null;
        String key = "maillist" + loginUserID + "_" + StringUtility.filterUrl(type) + pageIndex;
        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                list = smthSupport.getMailList(type, pageIndex);
                if (list != null && pageIndex == 0) {
                    list.setCacheKey(key);
                    saveObject(list, key);
                }
            } catch (AppException e) {
                list = (MailList) readObject(key);
                if (list == null)
                    throw e;
            }
        } else {
            list = (MailList) readObject(key);
            if (list == null)
                list = new MailList();
        }
        return list;
    }

    public Mail getMail(String boxType, int number, boolean isRefresh) throws AppException {
        Mail mail = null;
        String key = "mail" + StringUtility.filterUrl(boxType) + "_" + number;
        if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
            try {
                mail = smthSupport.getMail(boxType, number);
                if (mail != null && number == 0) {
                    mail.setCacheKey(key);
                    saveObject(mail, key);
                }
            } catch (Exception e) {
                mail = (Mail) readObject(key);
            }
        } else {
            mail = (Mail) readObject(key);
            if (mail == null)
                mail = new Mail();
        }
        return mail;
    }


    /**
     * 发帖子
     *
     * @param post （uid、title、catalog、content、isNoticeMe）
     * @return
     * @throws AppException
     */
    public Result pubPost(Post post) throws AppException {
        return null;
    }


    public Result getResult() {
        return null;
    }

    /**
     * 发送留言
     *
     * @return
     * @throws AppException
     */
    public Result sendMail(String mailUrl, String mailTitle, String userid, String backup, String mailContent) throws AppException {
        return smthSupport.sendMail(mailUrl, mailTitle, userid, backup, mailContent);
    }



    /**
     * 转发留言
     *
     * @param uid      登录用户uid
     * @param receiver 接受者的用户名
     * @param content  消息内容，注意不能超过250个字符
     * @return
     * @throws AppException
     */
    public Result forwardMessage(int uid, String receiver, String content) throws AppException {
        return null;
    }

    /**
     * 删除留言
     *
     * @return
     * @throws AppException
     */
    public Result delMail(String url) throws AppException {
        return smthSupport.delMail(url);
    }

    public Result delRef(String url) throws AppException {
        return smthSupport.delRef(url);
    }


    /**
     * 发表评论
     *
     * @return
     * @throws AppException
     */
    public Result pubComment(String boardEngName, String subjectID, String subjectTitle, String content) throws AppException {

        String url = URLs.HOST + "/article/" + boardEngName + "/post/" + subjectID;

        return smthSupport.sendQuickReply(url, subjectTitle, content);

    }

    /**
     * @param id       表示被评论的某条新闻，帖子，动弹的id 或者某条消息的 friendid
     * @param catalog  表示该评论所属什么类型：1新闻  2帖子  3动弹  4动态
     * @param replyid  表示被回复的单个评论id
     * @param authorid 表示该评论的原始作者id
     * @param uid      用户uid 一般都是当前登录用户uid
     * @param content  发表评论的内容
     * @return
     * @throws AppException
     */
    public Result replyComment(int id, int catalog, int replyid, int authorid, int uid, String content) throws AppException {
        return null;
    }

    /**
     * 删除评论
     *
     * @param id       表示被评论对应的某条新闻,帖子,动弹的id 或者某条消息的 friendid
     * @param catalog  表示该评论所属什么类型：1新闻  2帖子  3动弹  4动态&留言
     * @param replyid  表示被回复的单个评论id
     * @param authorid 表示该评论的原始作者id
     * @return
     * @throws AppException
     */
    public Result delComment(String id, String catalog, String replyid, String authorid) throws AppException {
        return null;
    }


    /**
     * 发表博客评论
     *
     * @param blog     博客id
     * @param uid      登陆用户的uid
     * @param content  评论内容
     * @param reply_id 评论id
     * @param objuid   被评论的评论发表者的uid
     * @return
     * @throws AppException
     */
    public Result replyBlogComment(int blog, int uid, String content, int reply_id, int objuid) throws AppException {
        return null;
    }

    /**
     * 删除博客评论
     *
     * @param uid      登录用户的uid
     * @param blogid   博客id
     * @param replyid  评论id
     * @param authorid 评论发表者的uid
     * @param owneruid 博客作者uid
     * @return
     * @throws AppException
     */
    public Result delBlogComment(int uid, int blogid, int replyid, int authorid, int owneruid) throws AppException {
        return null;
    }


    /**
     * 用户添加收藏
     *
     * @param uid   用户UID
     * @param objid 比如是新闻ID 或者问答ID 或者动弹ID
     * @param type  1:软件 2:话题 3:博客 4:新闻 5:代码
     * @return
     * @throws AppException
     */
    public Result addFavorite(String uid, String objid, int type) throws AppException {
        return null;
    }

    /**
     * 用户删除收藏
     *
     * @param uid   用户UID
     * @param objid 比如是新闻ID 或者问答ID 或者动弹ID
     * @param type  1:软件 2:话题 3:博客 4:新闻 5:代码
     * @return
     * @throws AppException
     */
    public Result delFavorite(String uid, String objid, int type) throws AppException {
        return null;
    }

    /**
     * 保存登录信息
     */
    public void saveLoginInfo(final User user) {
        this.loginUserID = user.getUserID();
        this.login = true;
        setProperties(new Properties() {{
            setProperty("user.userid", user.getUserID());
            setProperty("user.password", CyptoUtils.encode("SMTH3K", user.getPassword()));
            setProperty("user.isRemberme", String.valueOf(user.isRemberme()));
        }});
    }

    /**
     * 清除登录信息
     */
    public void cleanLoginInfo() {
        this.loginUserID = "guest";
        this.login = false;
        removeProperty("user.userid", "user.password", "user.isRemberme", "user.postNumber");
    }

    /**
     * 获取登录信息
     *
     * @return
     */
    public User getLoginInfo() {
        User lu = new User();
        try {
            lu.setUserID(getProperty("user.userid"));
            lu.setNickName(getProperty("user.nickname"));
            lu.setPassword(CyptoUtils.decode("SMTH3K", getProperty("user.password")));
            lu.setRemberme(StringUtility.toBool(getProperty("user.isRemberme")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lu;
    }

    /**
     * 保存用户头像
     *
     * @param fileName
     * @param bitmap
     */
    public void saveUserFace(String fileName, Bitmap bitmap) {
        try {
            ImageUtils.saveImage(this, fileName, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取用户头像
     *
     * @param key
     * @return
     * @throws AppException
     */
    public Bitmap getUserFace(String key) throws AppException {
        FileInputStream fis = null;
        try {
            fis = openFileInput(key);
            return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            throw AppException.run(e);
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 是否加载显示文章图片
     *
     * @return
     */
    public boolean isLoadImage() {
        String perf_loadimage = getProperty(AppConfig.CONF_LOAD_IMAGE);
        //默认是加载的
        if (StringUtility.isEmpty(perf_loadimage))
            return true;
        else
            return StringUtility.toBool(perf_loadimage);
    }

    /**
     * 是否加载显示文章图片
     *
     * @return
     */
    public boolean isLoadAds() {
        String perf_loadimage = getProperty(AppConfig.CONF_ADS);
        //默认是加载的
        if (StringUtility.isEmpty(perf_loadimage))
            return true;
        else
            return StringUtility.toBool(perf_loadimage);
    }



    /**
     * 设置是否加载文章图片
     *
     * @param b
     */
    public void setConfigLoadimage(boolean b) {
        setProperty(AppConfig.CONF_LOAD_IMAGE, String.valueOf(b));
    }

    /**
     * 设置是否加载文章图片
     *
     * @param b
     */
    public void setConfigLoadads(boolean b) {
        setProperty(AppConfig.CONF_ADS, String.valueOf(b));
    }

    /**
     * 是否发出提示音
     *
     * @return
     */
    public boolean isVoice() {
        String perf_voice = getProperty(AppConfig.CONF_VOICE);
        //默认是开启提示声音
        if (StringUtility.isEmpty(perf_voice))
            return true;
        else
            return StringUtility.toBool(perf_voice);
    }

    /**
     * 设置是否发出提示音
     *
     * @param b
     */
    public void setConfigVoice(boolean b) {
        setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
    }

    /**
     * 是否启动检查更新
     *
     * @return
     */
    public boolean isCheckUp() {
        String perf_checkup = getProperty(AppConfig.CONF_CHECKUP);
        //默认是开启
        if (StringUtility.isEmpty(perf_checkup))
            return true;
        else
            return StringUtility.toBool(perf_checkup);
    }

    /**
     * 设置启动检查更新
     *
     * @param b
     */
    public void setConfigCheckUp(boolean b) {
        setProperty(AppConfig.CONF_CHECKUP, String.valueOf(b));
    }

    /**
     * 是否左右滑动
     *
     * @return
     */
    public boolean isScroll() {
        String perf_scroll = getProperty(AppConfig.CONF_SCROLL);
        //默认是关闭左右滑动
        if (StringUtility.isEmpty(perf_scroll))
            return false;
        else
            return StringUtility.toBool(perf_scroll);
    }

    /**
     * 设置是否左右滑动
     *
     * @param b
     */
    public void setConfigScroll(boolean b) {
        setProperty(AppConfig.CONF_SCROLL, String.valueOf(b));
    }


    /**
     * 清除保存的缓存
     */
    public void cleanCookie() {
        removeProperty(AppConfig.CONF_COOKIE);
    }

    /**
     * 判断缓存数据是否可读
     *
     * @param cachefile
     * @return
     */
    private boolean isReadDataCache(String cachefile) {
        return readObject(cachefile) != null;
    }

    /**
     * 判断缓存是否存在
     *
     * @param cachefile
     * @return
     */
    private boolean isExistDataCache(String cachefile) {
        boolean exist = false;
        File data = getFileStreamPath(cachefile);
        if (data.exists())
            exist = true;
        return exist;
    }

    /**
     * 判断缓存是否失效
     *
     * @param cachefile
     * @return
     */
    public boolean isCacheDataFailure(String cachefile) {
        boolean failure = false;
        File data = getFileStreamPath(cachefile);
        if (data.exists() && (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
            failure = true;
        else if (!data.exists())
            failure = true;
        return failure;
    }

    /**
     * 清除app缓存
     */
    public void clearAppCache() {
        //清除webview缓存
        File file = CacheManager.getCacheFileBaseDir();
        if (file != null && file.exists() && file.isDirectory()) {
            for (File item : file.listFiles()) {
                item.delete();
            }
            file.delete();
        }
        deleteDatabase("webview.db");
        deleteDatabase("webview.db-shm");
        deleteDatabase("webview.db-wal");
        deleteDatabase("webviewCache.db");
        deleteDatabase("webviewCache.db-shm");
        deleteDatabase("webviewCache.db-wal");
        //清除数据缓存
        clearCacheFolder(getFilesDir(), System.currentTimeMillis());
        clearCacheFolder(getCacheDir(), System.currentTimeMillis());
        //2.2版本才有将应用缓存转移到sd卡的功能
        if (isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
            clearCacheFolder(MethodsCompat.getExternalCacheDir(this), System.currentTimeMillis());
        }
        //清除编辑器保存的临时内容
        Properties props = getProperties();
        for (Object key : props.keySet()) {
            String _key = key.toString();
            if (_key.startsWith("temp"))
                removeProperty(_key);
        }
    }

    /**
     * 清除缓存目录
     *
     * @param dir 目录
     * @return
     */
    private int clearCacheFolder(File dir, long curTime) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, curTime);
                    }
                    if (child.lastModified() < curTime) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }

    /**
     * 将对象保存到内存缓存中
     *
     * @param key
     * @param value
     */
    public void setMemCache(String key, Object value) {
        memCacheRegion.put(key, value);
    }

    /**
     * 从内存缓存中获取对象
     *
     * @param key
     * @return
     */
    public Object getMemCache(String key) {
        return memCacheRegion.get(key);
    }

    /**
     * 保存磁盘缓存
     *
     * @param key
     * @param value
     * @throws IOException
     */
    public void setDiskCache(String key, String value) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("cache_" + key + ".data", Context.MODE_PRIVATE);
            fos.write(value.getBytes());
            fos.flush();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 获取磁盘缓存数据
     *
     * @param key
     * @return
     * @throws IOException
     */
    public String getDiskCache(String key) throws IOException {
        FileInputStream fis = null;
        try {
            fis = openFileInput("cache_" + key + ".data");
            byte[] datas = new byte[fis.available()];
            fis.read(datas);
            return new String(datas);
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 保存对象
     *
     * @param ser
     * @param file
     * @throws IOException
     */
    public boolean saveObject(Serializable ser, String file) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = openFileOutput(file, MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 读取对象
     *
     * @param file
     * @return
     * @throws IOException
     */
    public Serializable readObject(String file) {
        if (!isExistDataCache(file))
            return null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = openFileInput(file);
            ois = new ObjectInputStream(fis);
            return (Serializable) ois.readObject();
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
            //反序列化失败 - 删除缓存文件
            if (e instanceof InvalidClassException) {
                File data = getFileStreamPath(file);
                data.delete();
            }
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
            }
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return null;
    }


    public boolean containsProperty(String key) {
        Properties props = getProperties();
        return props.containsKey(key);
    }

    public void setProperties(Properties ps) {
        AppConfig.getAppConfig(this).set(ps);
    }

    public Properties getProperties() {
        return AppConfig.getAppConfig(this).get();
    }

    public void setProperty(String key, String value) {
        AppConfig.getAppConfig(this).set(key, value);
    }

    public String getProperty(String key) {
        return AppConfig.getAppConfig(this).get(key);
    }

    public void removeProperty(String... key) {
        AppConfig.getAppConfig(this).remove(key);
    }
}
