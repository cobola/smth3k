package com.jimidigi.smth3k.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.jimidigi.smth3k.*;
import com.jimidigi.smth3k.adapter.GridViewFaceAdapter;
import com.jimidigi.smth3k.bean.*;
import com.jimidigi.smth3k.ui.*;
import com.umeng.fb.FeedbackAgent;
import greendroid.widget.MyQuickAction;
import greendroid.widget.QuickAction;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UIHelper {

    static String TAG = "UIHelper";

    public final static int LISTVIEW_ACTION_INIT = 0x01;
    public final static int LISTVIEW_ACTION_REFRESH = 0x02;
    public final static int LISTVIEW_ACTION_SCROLL = 0x03;
    public final static int LISTVIEW_ACTION_CHANGE_CATALOG = 0x04;

    public final static int LISTVIEW_DATA_MORE = 0x01;
    public final static int LISTVIEW_DATA_LOADING = 0x02;
    public final static int LISTVIEW_DATA_FULL = 0x03;
    public final static int LISTVIEW_DATA_EMPTY = 0x04;

    public final static int LISTVIEW_DATATYPE_SUBJECT_HOT = 0x01;
    public final static int LISTVIEW_DATATYPE_SUBJECT_BOARD = 0x02;
    public final static int LISTVIEW_DATATYPE_POST_BOARD = 0x03;
    public final static int LISTVIEW_DATATYPE_BOARD = 0x04;
    public final static int LISTVIEW_DATATYPE_FAV_BOARD = 0x05;
    public final static int LISTVIEW_DATATYPE_MAIL = 0x06;
    public final static int LISTVIEW_DATATYPE_REF = 0x07;
    public final static int LISTVIEW_DATATYPE_USER = 0x08;


    public final static int LISTVIEW_BOARD_TYPE_CLASSIC = 0x0;
    public final static int LISTVIEW_BOARD_TYPE_SUBJECT = 0x10;
    public final static int LISTVIEW_BOARD_TYPE_G = 0x01;
    public final static int LISTVIEW_BOARD_TYPE_M = 0x03;

    public final static int LISTVIEW_BOARD_TYPE_HOT = 0x06;


    public final static int LISTVIEW_DATATYPE_COMMENT = 0x07;


    public final static int REQUEST_CODE_FOR_RESULT = 0x01;
    public final static int REQUEST_CODE_FOR_REPLY = 0x02;

    public final static String HOST = "http://m.newsmth.net";//192.168.1.213  www.jimidigi.net

    private final static String URL_SPLITTER = "/";

    public final static String POST_DETAIL = HOST + "/article";
    public final static String POST_SINGLE = HOST + "/article";

    public final static String BOARD_LIST = HOST + "/section";

    public final static String HOT_BOARD_LIST = HOST + "/hot";
    public final static String POST_BY_BOARD_LIST = HOST + "/board";

    public final static String UPDATE_VERSION = HOST + "MobileAppVersion.xml";

    /**
     * 表情图片匹配
     */
    private static Pattern facePattern = Pattern.compile("\\[{1}([0-9]\\d*)\\]{1}");

    /**
     * 全局web样式
     */
    public final static String WEB_STYLE = "<style>* {font-size:16px;line-height:20px;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} " +
            "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} " +
            "pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;} " +
            "a.tag {font-size:15px;text-decoration:none;background-color:#bbd6f3;border-bottom:2px solid #3E6D8E;border-right:2px solid #7F9FB6;color:#284a7b;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;}</style>";

    /**
     * 显示首页
     *
     * @param activity
     */
    public static void showHome(Activity activity) {
        Intent intent = new Intent(activity, Main.class);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * 显示登录页面
     */
    public static void showLoginDialog(Context context) {
        Intent intent = new Intent(context, LoginDialog.class);
        if (context instanceof Main)
            intent.putExtra("LOGINTYPE", LoginDialog.LOGIN_MAIN);
        else if (context instanceof Setting)
            intent.putExtra("LOGINTYPE", LoginDialog.LOGIN_SETTING);
        else
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 显示登录页面
     */
    public static void showSearchBoardDialog(Context context) {
        Intent intent = new Intent(context, LoginDialog.class);
        if (context instanceof Main)
            intent.putExtra("LOGINTYPE", LoginDialog.LOGIN_MAIN);
        else if (context instanceof Setting)
            intent.putExtra("LOGINTYPE", LoginDialog.LOGIN_SETTING);
        else
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 显示新闻详情
     *
     * @param context
     * @param subjectID
     */
    public static void showSubjectDetail(Context context, String boardEngName, String subjectID) {
        Intent intent = new Intent(context, SubjectDetail.class);
        intent.putExtra("boardEngName", boardEngName);
        intent.putExtra("subjectID", subjectID);
        context.startActivity(intent);
    }

    /**
     * 显示POST详情
     *
     * @param context
     * @param postID
     */
    public static void showPostDetail(Context context, String boardEngName, String postID) {
        String url = URLs.HOST + "/article/" + boardEngName + "/single/" + postID + "/0";
        showPostDetail(context, url);
    }

    /**
     * 显示POST详情
     *
     * @param context
     */
    public static void showPostDetail(Context context, String url) {
        Intent intent = new Intent(context, PostDetail.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }


    /**
     * @param context
     * @param boardEngName
     * @param boardType
     * @param pages
     */
    public static void showBoardDetail(Context context, String boardEngName, int boardType, int pages) {
        Intent intent = new Intent(context, BoardDetail.class);
        intent.putExtra("boardEngName", boardEngName);
        intent.putExtra("boardType", boardType);
        intent.putExtra("pages", pages);
        context.startActivity(intent);
    }


    /**
     * 显示我要提问页面
     *
     * @param context
     */
    public static void showPostPub(Context context) {
        Intent intent = new Intent(context, PostPub.class);
        context.startActivity(intent);
    }

    public static void showSearch(Context context) {
        Intent intent = new Intent(context, Search.class);
        context.startActivity(intent);
    }


    /**
     * @param context
     * @param boxtype
     * @param number
     */
    public static void showMailDetail(Context context, String boxtype, int number) {
        Intent intent = new Intent(context, MailDetail.class);
        intent.putExtra("boxtype", boxtype);
        intent.putExtra("number", number);
        context.startActivity(intent);
    }

    /**
     * 显示留言回复界面
     *
     * @param context
     * @param friendId 对方id
     */
    public static void showMailSend(Activity context, String friendId, String title, String content) {
        Intent intent = new Intent();
        intent.putExtra("user_id", ((AppContext) context.getApplication()).getLoginUserID());
        intent.putExtra("friend_id", friendId);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.setClass(context, MailPub.class);
        context.startActivityForResult(intent, 1);
    }

    /**
     * 显示留言回复界面
     *
     * @param context
     */
    public static void showMailPub(Activity context) {
        Intent intent = new Intent();
        intent.setClass(context, MailPub.class);
        context.startActivityForResult(intent, 1);
    }

    /**
     * 显示转发留言界面
     *
     * @param context
     * @param friendName     对方名称
     * @param messageContent 留言内容
     */
    public static void showMessageForward(Activity context, String friendName, String messageContent) {
        Intent intent = new Intent();
        intent.putExtra("user_id", ((AppContext) context.getApplication()).getLoginUserID());
        intent.putExtra("friend_name", friendName);
        intent.putExtra("message_content", messageContent);
        intent.setClass(context, MailForward.class);
        context.startActivity(intent);
    }

    /**
     * 调用系统安装了的应用分享
     *
     * @param context
     * @param title
     * @param url
     */
    public static void showShareMore(Activity context, final String title, final String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享：" + title);
        intent.putExtra(Intent.EXTRA_TEXT, title + " " + url);
        context.startActivity(Intent.createChooser(intent, "选择分享"));
    }

    /**
     * 分享到'新浪微博'或'腾讯微博'的对话框
     *
     * @param context 当前Activity
     * @param title   分享的标题
     * @param url     分享的链接
     */
    public static void showShareDialog(final Activity context, final String title, final String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.btn_star);
        builder.setTitle(context.getString(R.string.share));
        builder.setItems(R.array.app_share_items, new DialogInterface.OnClickListener() {
            AppConfig cfgHelper = AppConfig.getAppConfig(context);
            AccessInfo access = cfgHelper.getAccessInfo();

            public void onClick(DialogInterface arg0, int arg1) {
                switch (arg1) {
                    case 0://新浪微博
                        //分享的内容
                        final String shareMessage = title + " " + url;


                        break;
                    case 1://腾讯微博
                        QQWeiboHelper.shareToQQ(context, title, url);
                        break;
                    case 2://更多
                        showShareMore(context, title, url);
                        break;
                }
            }
        });
        builder.create().show();
    }

    /**
     * 收藏操作选择框
     *
     * @param context
     * @param thread
     */
    public static void showFavoriteOptionDialog(final Activity context, final Thread thread) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_dialog_menu);
        builder.setTitle(context.getString(R.string.select));
        builder.setItems(R.array.favorite_options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                switch (arg1) {
                    case 0://删除
                        thread.start();
                        break;
                }
            }
        });
        builder.create().show();
    }

    /**
     * 消息列表操作选择框
     *
     * @param context
     * @param msg
     * @param thread
     */
    public static void showMailListOptionDialog(final Activity context, final Mail msg, final Thread thread) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_dialog_menu);
        builder.setTitle(context.getString(R.string.select));
        builder.setItems(R.array.mail_list_options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                switch (arg1) {
                    case 0://回复
                        showMailSend(context, msg.getSenderID(), msg.getTitle(), msg.getContent());
                        break;
                    case 1://删除
                        thread.start();
                        break;
                }
            }
        });
        builder.create().show();
    }

    /**
     * 消息详情操作选择框
     *
     * @param context
     * @param msg
     * @param thread
     */
    public static void showRefOptionDialog(final Activity context, final Mail msg, final Thread thread) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_dialog_menu);
        builder.setTitle(context.getString(R.string.select));
        builder.setItems(R.array.ref_list_options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                switch (arg1) {
                    case 0://删除
                        thread.start();
                        break;
                }
            }
        });
        builder.create().show();
    }


    public final static Board getRootFavBoard() {
        Board board = new Board();
        board.setUrl("/favor");
        board.setDirectory(true);
        board.setEngName("favor");
        board.setChsName("收藏夹");
        return board;
    }

    public final static Board getRootBoard() {
        Board board = new Board();
        board.setUrl("/section");
        board.setEngName("section");
        board.setDirectory(true);
        board.setChsName("讨论区列表");
        return board;
    }

    /**
     * 评论操作选择框
     *
     * @param context
     * @param id      某条新闻，帖子，动弹的id 或者某条消息的 friendid
     * @param catalog 该评论所属类型：1新闻  2帖子  3动弹  4动态
     * @param comment 本条评论对象，用于获取评论id&评论者authorid
     * @param thread  处理删除评论的线程，若无删除操作传null
     */
    public static void showCommentOptionDialog(final Activity context, final String id, final String catalog, final Post comment, final Thread thread) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_dialog_menu);
        builder.setTitle(context.getString(R.string.select));
        if (thread != null) {
            builder.setItems(R.array.comment_options_2, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    switch (arg1) {
                        case 0://回复
//                            showCommentReply(context, id, catalog, comment.getId(), comment.getAuthorId(), comment.getAuthor(), comment.getContent());
                            break;
                        case 1://删除
                            thread.start();
                            break;
                    }
                }
            });
        } else {
            builder.setItems(R.array.comment_options_1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    switch (arg1) {
                        case 0://回复
//                            showCommentReply(context, id, catalog, comment.getId(), comment.getAuthorId(), comment.getAuthor(), comment.getContent());
                            break;
                    }
                }
            });
        }
        builder.create().show();
    }

    /**
     * 博客列表操作
     *
     * @param context
     * @param thread
     */
    public static void showBlogOptionDialog(final Context context, final Thread thread) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(context.getString(R.string.delete_blog))
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (thread != null)
                            thread.start();
                        else
                            ToastMessage(context, R.string.msg_noaccess_delete);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    /**
     * 动弹操作选择框
     *
     * @param context
     * @param thread
     */
    public static void showTweetOptionDialog(final Context context, final Thread thread) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(context.getString(R.string.delete_tweet))
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (thread != null)
                            thread.start();
                        else
                            ToastMessage(context, R.string.msg_noaccess_delete);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    /**
     * 是否重新发布动弹操对话框
     *
     * @param context
     * @param thread
     */
    public static void showResendTweetDialog(final Context context, final Thread thread) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(context.getString(R.string.republish_tweet))
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        if (context == TweetPub.mContext && TweetPub.mMessage != null)
//                            TweetPub.mMessage.setVisibility(View.VISIBLE);
                        thread.start();
                    }
                })
                .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    /**
     * 显示图片对话框
     *
     * @param context
     * @param imgUrl
     */
    public static void showImageDialog(Context context, String imgUrl) {
        Intent intent = new Intent(context, ImageDialog.class);
        intent.putExtra("img_url", imgUrl);
        context.startActivity(intent);
    }

    public static void showImageZoomDialog(Context context, String imgUrl) {
        Intent intent = new Intent(context, ImageZoomDialog.class);
        intent.putExtra("img_url", imgUrl);
        context.startActivity(intent);
    }

    /**
     * 显示系统设置界面
     *
     * @param context
     */
    public static void showSetting(Context context) {
        Intent intent = new Intent(context, Setting.class);
        context.startActivity(intent);
    }


    /**
     * 显示我的资料
     *
     * @param context
     */
    public static void showUserInfo(Activity context) {
        AppContext ac = (AppContext) context.getApplicationContext();
        if (!ac.isLogin()) {
            showLoginDialog(context);
        } else {
            Intent intent = new Intent(context, UserInfo.class);
            context.startActivity(intent);
        }
    }

    /**
     * 显示用户动态
     *
     * @param context
     */
    public static void showUserCenter(Context context, String userid) {
        Intent intent = new Intent(context, UserCenter.class);
        intent.putExtra("userid", userid);
        context.startActivity(intent);
    }


    /**
     * 加载显示用户头像
     *
     * @param imgFace
     * @param faceURL
     */
    public static void showUserFace(final ImageView imgFace, final String faceURL) {
        showLoadImage(imgFace, faceURL, imgFace.getContext().getString(R.string.msg_load_userface_fail));
    }

    /**
     * 加载显示图片
     *
     * @param errMsg
     */
    public static void showLoadImage(final ImageView imgView, final String imgURL, final String errMsg) {
        //读取本地图片
        if (StringUtility.isEmpty(imgURL) || imgURL.endsWith("portrait.gif")) {
            Bitmap bmp = BitmapFactory.decodeResource(imgView.getResources(), R.drawable.widget_dface);
            imgView.setImageBitmap(bmp);
            return;
        }

        //是否有缓存图片
        final String filename = FileUtils.getFileName(imgURL);
        //Environment.getExternalStorageDirectory();返回/sdcard
        String filepath = imgView.getContext().getFilesDir() + File.separator + filename;
        File file = new File(filepath);
        if (file.exists()) {
            Bitmap bmp = ImageUtils.getBitmap(imgView.getContext(), filename);
            imgView.setImageBitmap(bmp);
            return;
        }

        //从网络获取&写入图片缓存
        String _errMsg = imgView.getContext().getString(R.string.msg_load_image_fail);
        if (!StringUtility.isEmpty(errMsg))
            _errMsg = errMsg;
        final String ErrMsg = _errMsg;
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1 && msg.obj != null) {
                    imgView.setImageBitmap((Bitmap) msg.obj);
                    try {
                        //写图片缓存
                        ImageUtils.saveImage(imgView.getContext(), filename, (Bitmap) msg.obj);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastMessage(imgView.getContext(), ErrMsg);
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    Bitmap bmp = SmthSupport.getNetBitmap(imgURL);
                    msg.what = 1;
                    msg.obj = bmp;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * url跳转
     *
     * @param context
     * @param url
     */
    public static void showUrlRedirect(Context context, String url) {
//        URLs urls = URLs.parseURL(url);
//        if (urls != null) {
////            showLinkRedirect(context, urls.getObjType(), urls.getObjId(), urls.getObjKey());
//        } else {
//            openBrowser(context, url);
//        }
    }

//    public static void showLinkRedirect(Context context, int objType, int objId, String objKey) {
//        switch (objType) {
//            case URLs.URL_OBJ_TYPE_NEWS:
//                showNewsDetail(context, objId);
//                break;
//            case URLs.URL_OBJ_TYPE_QUESTION:
//                showQuestionDetail(context, objId);
//                break;
//            case URLs.URL_OBJ_TYPE_QUESTION_TAG:
//                showQuestionListByTag(context, objKey);
//                break;
//            case URLs.URL_OBJ_TYPE_SOFTWARE:
//                showSoftwareDetail(context, objKey);
//                break;
//            case URLs.URL_OBJ_TYPE_ZONE:
//                showUserCenter(context, objId, objKey);
//                break;
//            case URLs.URL_OBJ_TYPE_TWEET:
//                showTweetDetail(context, objId);
//                break;
//            case URLs.URL_OBJ_TYPE_BLOG:
//                showBlogDetail(context, objId);
//                break;
//            case URLs.URL_OBJ_TYPE_OTHER:
//                openBrowser(context, objKey);
//                break;
//        }
//    }

    /**
     * 打开浏览器
     *
     * @param context
     * @param url
     */
    public static void openBrowser(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(it);
        } catch (Exception e) {
            e.printStackTrace();
            ToastMessage(context, "无法浏览此网页", 500);
        }
    }

    /**
     * 获取webviewClient对象
     *
     * @return
     */
    public static WebViewClient getWebViewClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                showUrlRedirect(view.getContext(), url);
                return true;
            }
        };
    }

    /**
     * 获取TextWatcher对象
     *
     * @param context
     * @return
     */
    public static TextWatcher getTextWatcher(final Activity context, final String temlKey) {
        return new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //保存当前EditText正在编辑的内容
                ((AppContext) context.getApplication()).setProperty(temlKey, s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        };
    }

    /**
     * 编辑器显示保存的草稿
     *
     * @param context
     * @param editer
     * @param temlKey
     */
    public static void showTempEditContent(Activity context, EditText editer, String _content, String temlKey) {
        String tempContent = ((AppContext) context.getApplication()).getProperty(temlKey);
        if (!StringUtility.isEmpty(tempContent)) {
            SpannableStringBuilder builder = parseFaceByText(context, tempContent);
            editer.setText(builder);
            editer.setSelection(tempContent.length());//设置光标位置
        } else {
            if (StringUtility.isNotEmpty(_content)) {
                editer.setText(_content);
            }
        }
    }


    /**
     * 编辑器显示保存的草稿
     *
     * @param context
     * @param editer
     * @param temlKey
     */
    public static void showTempEditContent(Activity context, EditText editer, String temlKey) {
        String tempContent = ((AppContext) context.getApplication()).getProperty(temlKey);
        if (!StringUtility.isEmpty(tempContent)) {
            SpannableStringBuilder builder = parseFaceByText(context, tempContent);
            editer.setText(builder);
            editer.setSelection(tempContent.length());//设置光标位置
        }
    }

    /**
     * 将[12]之类的字符串替换为表情
     *
     * @param context
     * @param content
     */
    public static SpannableStringBuilder parseFaceByText(Context context, String content) {
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        Matcher matcher = facePattern.matcher(content);
        while (matcher.find()) {
            //使用正则表达式找出其中的数字
            int position = StringUtility.toInt(matcher.group(1));
            int resId = 0;
            try {
                if (position > 65 && position < 102)
                    position = position - 1;
                else if (position > 102)
                    position = position - 2;
                resId = GridViewFaceAdapter.getImageIds()[position];
                Drawable d = context.getResources().getDrawable(resId);
                d.setBounds(0, 0, 35, 35);//设置表情图片的显示大小
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                builder.setSpan(span, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
            }
        }
        return builder;
    }

    /**
     * 清除文字
     *
     * @param cont
     * @param editer
     */
    public static void showClearWordsDialog(final Context cont, final EditText editer, final TextView numwords) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setTitle(R.string.clearwords);
        builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //清除文字
                editer.setText("");
                numwords.setText("160");
            }
        });
        builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 发送通知广播
     *
     * @param context
     * @param result
     */
    public static void sendBroadCast(Context context, Result result) {
        if (!((AppContext) context.getApplicationContext()).isLogin() || result == null) return;
        Intent intent = new Intent("com.jimidigi.smth3k.action.APPWIDGET_UPDATE");
        intent.putExtra("atmeCount", result.getAtmeCount());
        intent.putExtra("replyCount", result.getReplyCount());
        intent.putExtra("newMail", result.isNewMail());
        context.sendBroadcast(intent);
    }

    /**
     * 发送广播-发布动弹
     *
     * @param context
     */
    public static void sendBroadCastTweet(Context context, int what, Result res) {
        if (res == null) return;
        Intent intent = new Intent("com.jimidigi.smth3k.action.APP_TWEETPUB");
        intent.putExtra("MSG_WHAT", what);
        if (what == 1)
            intent.putExtra("RESULT", res);
        context.sendBroadcast(intent);
    }


    /**
     * 弹出Toast消息
     *
     * @param msg
     */
    public static void ToastMessage(Context cont, String msg) {
        Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
    }

    public static void ToastMessage(Context cont, int msg) {
        Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
    }

    public static void ToastMessage(Context cont, String msg, int time) {
        Toast.makeText(cont, msg, time).show();
    }

    /**
     * 点击返回监听事件
     *
     * @param activity
     * @return
     */
    public static View.OnClickListener finish(final Activity activity) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                activity.finish();
            }
        };
    }

    /**
     * 显示关于我们
     *
     * @param context
     */
    public static void showAbout(Context context) {
        Intent intent = new Intent(context, About.class);
        context.startActivity(intent);
    }

    /**
     * 显示用户反馈
     *
     * @param context
     */
    public static void showFeedBack(Context context) {
        FeedbackAgent agent = new FeedbackAgent(context);
        agent.startFeedbackActivity();
    }

    /**
     * 菜单显示登录或登出
     *
     * @param activity
     * @param menu
     */
    public static void showMenuLoginOrLogout(Activity activity, Menu menu) {
        if (((AppContext) activity.getApplication()).isLogin()) {
            menu.findItem(R.id.main_menu_user).setTitle(R.string.main_menu_logout);
            menu.findItem(R.id.main_menu_user).setIcon(R.drawable.ic_menu_logout);
        } else {
            menu.findItem(R.id.main_menu_user).setTitle(R.string.main_menu_login);
            menu.findItem(R.id.main_menu_user).setIcon(R.drawable.ic_menu_login);
        }
    }

    /**
     * 快捷栏显示登录与登出
     *
     * @param activity
     * @param qa
     */
    public static void showSettingLoginOrLogout(Activity activity, QuickAction qa) {
        if (((AppContext) activity.getApplication()).isLogin()) {
            qa.setIcon(MyQuickAction.buildDrawable(activity, R.drawable.ic_menu_logout));
            qa.setTitle(activity.getString(R.string.main_menu_logout));
        } else {
            qa.setIcon(MyQuickAction.buildDrawable(activity, R.drawable.ic_menu_login));
            qa.setTitle(activity.getString(R.string.main_menu_login));
        }
    }

    /**
     * 快捷栏是否显示文章图片
     *
     * @param activity
     * @param qa
     */
    public static void showSettingIsLoadImage(Activity activity, QuickAction qa) {
        if (((AppContext) activity.getApplication()).isLoadImage()) {
            qa.setIcon(MyQuickAction.buildDrawable(activity, R.drawable.ic_menu_picnoshow));
            qa.setTitle(activity.getString(R.string.main_menu_picnoshow));
        } else {
            qa.setIcon(MyQuickAction.buildDrawable(activity, R.drawable.ic_menu_picshow));
            qa.setTitle(activity.getString(R.string.main_menu_picshow));
        }
    }

    /**
     * 用户登录或注销
     *
     * @param activity
     */
    public static void loginOrLogout(Activity activity) {
        AppContext ac = (AppContext) activity.getApplication();
        if (ac.isLogin()) {
            ac.Logout();
            ToastMessage(activity, "已退出登录");
        } else {
            showLoginDialog(activity);
        }
    }

    /**
     * 文章是否加载图片显示
     *
     * @param activity
     */
    public static void changeSettingIsLoadImage(Activity activity) {
        AppContext ac = (AppContext) activity.getApplication();
        if (ac.isLoadImage()) {
            ac.setConfigLoadimage(false);
            ToastMessage(activity, "已设置文章不加载图片");
        } else {
            ac.setConfigLoadimage(true);
            ToastMessage(activity, "已设置文章加载图片");
        }
    }

    public static void changeSettingIsLoadImage(Activity activity, boolean b) {
        AppContext ac = (AppContext) activity.getApplication();
        ac.setConfigLoadimage(b);
    }

    /**
     * 清除app缓存
     *
     * @param activity
     */
    public static void clearAppCache(Activity activity) {
        final AppContext ac = (AppContext) activity.getApplication();
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    ToastMessage(ac, "缓存清除成功");
                } else {
                    ToastMessage(ac, "缓存清除失败");
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    ac.clearAppCache();
                    msg.what = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 发送App异常崩溃报告
     *
     * @param cont
     * @param crashReport
     */
    public static void sendAppCrashReport(final Context cont, final String crashReport) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.app_error);
        builder.setMessage(R.string.app_error_message);
        builder.setPositiveButton(R.string.submit_report, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //发送异常报告
                Intent i = new Intent(Intent.ACTION_SEND);
                //i.setType("text/plain"); //模拟器
                i.setType("message/rfc822"); //真机
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"cobola@126.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "水木三千Android客户端 - 错误报告");
                i.putExtra(Intent.EXTRA_TEXT, crashReport);
                cont.startActivity(Intent.createChooser(i, "发送错误报告"));
                //退出
                AppManager.getAppManager().AppExit(cont);
            }
        });
        builder.setNegativeButton(R.string.sure, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //退出
                AppManager.getAppManager().AppExit(cont);
            }
        });
        builder.show();
    }

    /**
     * 退出程序
     *
     * @param cont
     */
    public static void Exit(final Context cont) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.app_menu_surelogout);
        builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //退出
//                OffersManager.getInstance(cont).onAppExit();
                AppManager.getAppManager().AppExit(cont);
            }
        });
        builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    class UMENGCONSTANTS {
        public final static String login = "smth_login";
    }
}
