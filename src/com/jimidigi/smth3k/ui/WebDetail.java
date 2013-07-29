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
import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.diy.banner.DiyAdSize;
import net.youmi.android.diy.banner.DiyBanner;

/**
 * 新闻详情
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class WebDetail extends BaseActivity {

    private FrameLayout mHeader;
    private LinearLayout mFooter;
    private ImageView mBack;
    private ImageView mFavorite;
    private ImageView mRefresh;
    private TextView mHeadTitle;
    private ProgressBar mProgressbar;

    private ImageView mFav;
    private ImageView mShare;


    private WebView mWebView;
    private Handler postHandler;
    private String url;
    private String boardEngName;

    private final static int VIEWSWITCH_TYPE_DETAIL = 0x001;
    private final static int VIEWSWITCH_TYPE_COMMENTS = 0x002;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;
    private final static int DATA_LOAD_FAIL = 0x003;


    private ProgressDialog mProgress;
    private String tempCommentKey = AppConfig.TEMP_COMMENT;

    private GestureDetector gd;
    private boolean isFullScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_detail);

        this.initView();

        this.ads();

    }


    private void ads() {
        //实例化广告条
        AdView adView = new AdView(this, AdSize.SIZE_320x50);
        //获取要嵌入广告条的布局
        LinearLayout adLayout=(LinearLayout)findViewById(R.id.adLayout);
        //将广告条加入到布局中
        adLayout.addView(adView);
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


        mWebView = (WebView) findViewById(R.id.post_detail_webview);
        mWebView.getSettings().setDefaultFontSize(15);

        mWebView.loadUrl(url);

        mBack.setOnClickListener(UIHelper.finish(this));
        mRefresh.setOnClickListener(refreshClickListener);


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
            mWebView.reload();
        }
    };




}