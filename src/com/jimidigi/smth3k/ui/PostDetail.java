package com.jimidigi.smth3k.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.*;
import com.jimidigi.smth3k.AppConfig;
import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.bean.Post;
import com.jimidigi.smth3k.bean.Result;
import com.jimidigi.smth3k.common.DateUtils;
import com.jimidigi.smth3k.common.StringUtility;
import com.jimidigi.smth3k.common.UIHelper;
import net.youmi.android.diy.banner.DiyAdSize;
import net.youmi.android.diy.banner.DiyBanner;

/**
 * 新闻详情
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class PostDetail extends BaseActivity {

    private FrameLayout mHeader;
    private LinearLayout mFooter;
    private ImageView mBack;
    private ImageView mFavorite;
    private ImageView mRefresh;
    private TextView mHeadTitle;
    private ProgressBar mProgressbar;

    private ImageView mFav;
    private ImageView mShare;

    private TextView mTitle;
    private TextView mAuthor;
    private TextView mPubDate;

    private TextView mCommentCount;

    private WebView mWebView;
    private Handler postHandler;
    private Post post;
    private String url;
    private String boardEngName;

    private final static int VIEWSWITCH_TYPE_DETAIL = 0x001;
    private final static int VIEWSWITCH_TYPE_COMMENTS = 0x002;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;
    private final static int DATA_LOAD_FAIL = 0x003;


    private ViewSwitcher mFootViewSwitcher;
    private ImageView mFootEditebox;
    private EditText mFootEditer;
    private Button mFootPubcomment;
    private ProgressDialog mProgress;
    private InputMethodManager imm;
    private String tempCommentKey = AppConfig.TEMP_COMMENT;

    private String _board;
    private String _id;
    private String _uid;
    private String _content;
    private int _isPostToMyZone;

    private GestureDetector gd;
    private boolean isFullScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_detail);

        this.initView();
        this.initData();

        this.ads();

    }


    private void ads() {
        AppContext ac = (AppContext) getApplication();
        if (ac.isLoadAds()) {
            //获取要嵌入迷你广告条的布局
            RelativeLayout adLayout = (RelativeLayout) findViewById(R.id.AdLayout);
            //demo 1 迷你Banner : 宽满屏，高32dp
            DiyBanner banner = new DiyBanner(this, DiyAdSize.SIZE_MATCH_SCREENx32);//传入高度为32dp的AdSize来定义迷你Banner
            //将积分Banner加入到布局中
            adLayout.addView(banner);
        }
    }

    //初始化视图控件
    private void initView() {
        url = getIntent().getStringExtra("url");

        if (url != null)
            tempCommentKey = AppConfig.TEMP_COMMENT + "_" + StringUtility.filterUrl(url);

        mHeader = (FrameLayout) findViewById(R.id.post_detail_header);
        mFooter = (LinearLayout) findViewById(R.id.post_detail_footer);
        mBack = (ImageView) findViewById(R.id.post_detail_back);
        mRefresh = (ImageView) findViewById(R.id.post_detail_refresh);
        mHeadTitle = (TextView) findViewById(R.id.post_detail_head_title);
        mProgressbar = (ProgressBar) findViewById(R.id.post_detail_head_progress);

        mFav = (ImageView) findViewById(R.id.post_detail_footbar_favorite);
        mShare = (ImageView) findViewById(R.id.post_detail_footbar_share);

        mTitle = (TextView) findViewById(R.id.post_detail_title);
        mAuthor = (TextView) findViewById(R.id.post_detail_author);
        mPubDate = (TextView) findViewById(R.id.post_detail_date);


        mWebView = (WebView) findViewById(R.id.post_detail_webview);
        mWebView.getSettings().setDefaultFontSize(15);

        mBack.setOnClickListener(UIHelper.finish(this));
//        mFavorite.setOnClickListener(favoriteClickListener);
        mRefresh.setOnClickListener(refreshClickListener);
//        mShare.setOnClickListener(shareClickListener);
//        mAuthor.setOnClickListener(authorClickListener);


        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mFootViewSwitcher = (ViewSwitcher) findViewById(R.id.post_detail_foot_viewswitcher);
        mFootPubcomment = (Button) findViewById(R.id.post_detail_foot_pubcomment);
//        mFootPubcomment.setOnClickListener(commentpubClickListener);
        mFootEditebox = (ImageView) findViewById(R.id.post_detail_footbar_editebox);
        mFootEditebox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFootViewSwitcher.showNext();
                mFootEditer.setVisibility(View.VISIBLE);
                mFootEditer.requestFocus();
                mFootEditer.requestFocusFromTouch();
            }
        });
        mFootEditer = (EditText) findViewById(R.id.post_detail_foot_editer);
        mFootEditer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imm.showSoftInput(v, 0);
                } else {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        mFootEditer.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mFootViewSwitcher.getDisplayedChild() == 1) {
                        mFootViewSwitcher.setDisplayedChild(0);
                        mFootEditer.clearFocus();
                        mFootEditer.setVisibility(View.GONE);
                    }
                    return true;
                }
                return false;
            }
        });
        //编辑器添加文本监听
        mFootEditer.addTextChangedListener(UIHelper.getTextWatcher(this, tempCommentKey));

        //显示临时编辑内容
        UIHelper.showTempEditContent(this, mFootEditer, tempCommentKey);
    }

    //初始化控件数据
    private void initData() {
        postHandler = new Handler() {
            public void handleMessage(Message msg) {

                headButtonSwitch(DATA_LOAD_COMPLETE);

                if (msg.what == 1) {
                    post = (Post) msg.obj;

                    Result notice = post.getResult();
                    mTitle.setText(post.getTitle());
                    mAuthor.setText(post.getAuthor());
                    mPubDate.setText(DateUtils.niceDay(post.getDate()));

                    String body = UIHelper.WEB_STYLE + post.getContent();
                    //读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
                    boolean isLoadImage;
                    AppContext ac = (AppContext) getApplication();
                    if (AppContext.NETTYPE_WIFI == ac.getNetworkType()) {
                        isLoadImage = true;
                    } else {
                        isLoadImage = ac.isLoadImage();
                    }
                    if (isLoadImage) {
                        body = body.replaceAll("(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
                        body = body.replaceAll("(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");
                    } else {
                        body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
                    }

                    mWebView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
                    mWebView.setWebViewClient(UIHelper.getWebViewClient());

                    if (notice != null) {
                        UIHelper.sendBroadCast(PostDetail.this, notice);
                    }

                } else if (msg.what == 0) {
                    headButtonSwitch(DATA_LOAD_FAIL);

                    UIHelper.ToastMessage(PostDetail.this, R.string.msg_load_is_null);
                } else if (msg.what == -1 && msg.obj != null) {
                    headButtonSwitch(DATA_LOAD_FAIL);

                    ((AppException) msg.obj).makeToast(PostDetail.this);
                }
            }
        };

        initData(url, true);
    }

    private void initData(final String url, final boolean isRefresh) {
        headButtonSwitch(DATA_LOAD_ING);

        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    post = ((AppContext) getApplication()).getPost(url, isRefresh);
                    msg.what = (post != null && post.getSubjectID() != null) ? 1 : 0;
                    msg.obj = post;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                postHandler.sendMessage(msg);
            }
        }.start();
    }


    /**
     * 头部按钮展示
     *
     * @param type
     */
    private void headButtonSwitch(int type) {
        switch (type) {
            case DATA_LOAD_ING:
                mProgressbar.setVisibility(View.VISIBLE);
                mRefresh.setVisibility(View.GONE);
                break;
            case DATA_LOAD_COMPLETE:
                mProgressbar.setVisibility(View.GONE);
                mRefresh.setVisibility(View.VISIBLE);
                break;
            case DATA_LOAD_FAIL:
                mProgressbar.setVisibility(View.GONE);
                mRefresh.setVisibility(View.VISIBLE);
                break;
        }
    }

    private View.OnClickListener refreshClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            initData(url, true);
        }
    };

    private View.OnClickListener authorClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            UIHelper.showUserCenter(v.getContext(), post.getAuthor());
        }
    };

    private View.OnClickListener shareClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (post == null) {
                UIHelper.ToastMessage(v.getContext(), R.string.msg_read_detail_fail);
                return;
            }
            //分享到
            UIHelper.showShareDialog(PostDetail.this, post.getTitle(), post.getUrl());
        }
    };


    private View.OnClickListener favoriteClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (url == null || post == null) {
                return;
            }

            final AppContext ac = (AppContext) getApplication();
            if (!ac.isLogin()) {
                UIHelper.showLoginDialog(PostDetail.this);
                return;
            }
            final String uid = ac.getLoginUserID();

            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        Result res = (Result) msg.obj;
                        if (res.isOk()) {
//                            if(postDetail.getFavorite() == 1){
//                                postDetail.setFavorite(0);
//                                mFavorite.setImageResource(R.drawable.widget_bar_favboard);
//                            }else{
//                                postDetail.setFavorite(1);
//                                mFavorite.setImageResource(R.drawable.widget_bar_favorite);
//                            }
                            //重新保存缓存
                            ac.saveObject(post, post.getCacheKey());
                        }
                        UIHelper.ToastMessage(PostDetail.this, res.getErrorMessage());
                    } else {
                        ((AppException) msg.obj).makeToast(PostDetail.this);
                    }
                }
            };
            new Thread() {
                public void run() {
                    Message msg = new Message();
                    Result res = null;
                    try {
//                        if(postDetail.getFavorite() == 1){
//                            res = ac.delFavorite(uid, postId, FavoriteList.TYPE_POST);
//                        }else{
//                            res = ac.addFavorite(uid, postId, FavoriteList.TYPE_POST);
//                        }
                        msg.what = 1;
                        msg.obj = res;
                    } catch (Exception e) {
                        e.printStackTrace();
                        msg.what = -1;
                        msg.obj = e;
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (data == null) return;

//        viewSwitch(VIEWSWITCH_TYPE_COMMENTS);//跳到评论列表

        if (requestCode == UIHelper.REQUEST_CODE_FOR_RESULT) {
            Post comm = (Post) data.getSerializableExtra("COMMENT_SERIALIZABLE");
            //显示评论数

        } else if (requestCode == UIHelper.REQUEST_CODE_FOR_REPLY) {
            Post comm = (Post) data.getSerializableExtra("COMMENT_SERIALIZABLE");
        }
    }


}