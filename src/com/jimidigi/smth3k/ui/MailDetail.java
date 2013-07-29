package com.jimidigi.smth3k.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.*;
import com.jimidigi.smth3k.AppConfig;
import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.bean.Mail;
import com.jimidigi.smth3k.bean.Result;
import com.jimidigi.smth3k.bean.URLs;
import com.jimidigi.smth3k.common.DateUtils;
import com.jimidigi.smth3k.common.StringUtility;
import com.jimidigi.smth3k.common.UIHelper;

/**
 * 留言详情
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class MailDetail extends BaseActivity {

    private ImageView mBack;
    private ImageView mRefresh;
    private TextView mHeadTitle;
    private TextView mTitle;
    private TextView mSenderid;
    private TextView mDate;
    private WebView mContent;
    private ProgressBar mProgressbar;

    private ProgressDialog mProgress;
    private Handler mHandler;

    private String boxtype;
    private int number;

    private ViewSwitcher mFootViewSwitcher;
    private ImageView mFootEditebox;
    private EditText mFootEditer;
    private Button mFootSendMail;
    private ImageView mFooterDelMail;
    private InputMethodManager imm;
    private String tempMessageKey = AppConfig.TEMP_MESSAGE;

    private Mail mail;

    private String _url;
    private String _title;
    private String _user;
    private String _backup;
    private String _content;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_detail);

        this.initView();
        this.initData();
    }

    /**
     * 头部加载展示
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
        }
    }

    //初始化视图控件
    private void initView() {
        boxtype = getIntent().getStringExtra("boxtype");
        number = getIntent().getIntExtra("number", 0);

        if (StringUtility.isNotEmpty(boxtype) && number > 0)
            tempMessageKey = AppConfig.TEMP_MESSAGE + "_" + boxtype.replace("/", "") + "_" + number;

        mBack = (ImageView) findViewById(R.id.mail_detail_back);
        mRefresh = (ImageView) findViewById(R.id.mail_detail_refresh);
        mHeadTitle = (TextView) findViewById(R.id.mail_detail_head_title);
        mProgressbar = (ProgressBar) findViewById(R.id.mail_detail_head_progress);

        mTitle = (TextView) findViewById(R.id.mail_detail_title);
        mSenderid = (TextView) findViewById(R.id.mail_detail_senderid);
        mDate = (TextView) findViewById(R.id.mail_detail_date);
        mContent = (WebView) findViewById(R.id.mail_detail_content);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mFootViewSwitcher = (ViewSwitcher) findViewById(R.id.mail_detail_foot_viewswitcher);
        mFootSendMail = (Button) findViewById(R.id.mail_detail_foot_sendmail);
        mFootSendMail.setOnClickListener(mailSendClickListener);
        mFooterDelMail = (ImageView) findViewById(R.id.mail_detail_footbar_del);
        mFooterDelMail.setOnClickListener(mailDelClickListener);
        mFootEditebox = (ImageView) findViewById(R.id.mail_detail_footbar_editebox);
        mFootEditebox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFootViewSwitcher.showNext();
                mFootEditer.setVisibility(View.VISIBLE);
                mFootEditer.requestFocus();
                mFootEditer.requestFocusFromTouch();
            }
        });
        mFootEditer = (EditText) findViewById(R.id.mail_detail_foot_editer);
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
        mFootEditer.addTextChangedListener(UIHelper.getTextWatcher(this, tempMessageKey));

        //显示临时编辑内容
        UIHelper.showTempEditContent(this, mFootEditer, tempMessageKey);
        mHeadTitle.setText(R.string.message_detail_head_title);
        mBack.setOnClickListener(UIHelper.finish(this));
        mRefresh.setOnClickListener(refreshClickListener);
    }

    //初始化控件数据
    private void initData() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {

                headButtonSwitch(DATA_LOAD_COMPLETE);


                if (msg.what >= 0) {
                    mail = (Mail) msg.obj;
                    Result notice = mail.getResult();

                    mTitle.setText(mail.getTitle());
                    mSenderid.setText(mail.getSenderID());
                    mDate.setText(DateUtils.niceDay(mail.getDate()));


                    _url = URLs.HOST + boxtype + "/send/" + number;
                    _title = mail.getTitle();
                    _user = mail.getSenderID();
                    _backup = "true";


                    String body = mail.getContent();
                    if (StringUtility.isNotEmpty(body)) {

                        mContent.getSettings().setJavaScriptEnabled(false);
                        mContent.getSettings().setDefaultFontSize(15);


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

                        mContent.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
                        mContent.setWebViewClient(UIHelper.getWebViewClient());
                    }

                    //发送通知广播
                    if (notice != null) {
                        UIHelper.sendBroadCast(MailDetail.this, notice);
                    }
                }


            }
        };
        this.loadMailData(boxtype, number, mHandler, UIHelper.LISTVIEW_ACTION_INIT);
    }

    /**
     * 线程加载评论数据
     *
     * @param boxtype
     * @param number
     * @param handler
     * @param action
     */
    private void loadMailData(final String boxtype, final int number, final Handler handler, final int action) {

        this.headButtonSwitch(DATA_LOAD_ING);

        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    Mail mail = ((AppContext) getApplication()).getMail(boxtype, number, isRefresh);
                    if (mail != null) {
                        msg.what = mail.getId();
                        msg.obj = mail;
                    }
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;//告知handler当前action
                handler.sendMessage(msg);
            }
        }.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (data == null) return;
        if (requestCode == UIHelper.REQUEST_CODE_FOR_RESULT) {
            Mail comm = (Mail) data.getSerializableExtra("COMMENT_SERIALIZABLE");
        }
    }

    private View.OnClickListener refreshClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            loadMailData(boxtype, number, mHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
        }
    };

    private View.OnClickListener mailDelClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final AppContext ac = (AppContext) getApplication();
            if (!ac.isLogin()) {
                UIHelper.showLoginDialog(MailDetail.this);
                return;
            }

            mProgress = ProgressDialog.show(v.getContext(), null, "删除ing···", true, true);

            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {

                    if (mProgress != null) mProgress.dismiss();

                    if (msg.what == 1) {
                        Result res = (Result) msg.obj;
                        if (res.isOk()) {
                            //发送通知广播
                            UIHelper.ToastMessage(MailDetail.this, "删除成功！");

                            UIHelper.sendBroadCast(MailDetail.this, res);
                            finish();

                        } else {
                            UIHelper.ToastMessage(MailDetail.this, "失败了！");

                        }
                    } else {
                        ((AppException) msg.obj).makeToast(MailDetail.this);
                    }
                }
            };
            new Thread() {
                public void run() {
                    Message msg = new Message();
                    try {
                        Result res = ac.delMail(URLs.HOST + boxtype + "/delete/" + number);
                        msg.what = 1;
                        msg.obj = res;
                    } catch (AppException e) {
                        e.printStackTrace();
                        msg.what = -1;
                        msg.obj = e;
                    }
                    handler.sendMessage(msg);
                }
            }.start();

        }
    };

    private View.OnClickListener mailSendClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            final AppContext ac = (AppContext) getApplication();
            if (!ac.isLogin()) {
                UIHelper.showLoginDialog(MailDetail.this);
                return;
            }


            if (mail.getSenderID() == null || ac.getLoginUserID() == null) return;

            _content = mFootEditer.getText().toString();
            if (StringUtility.isEmpty(_content)) {
                UIHelper.ToastMessage(v.getContext(), "请输入邮件内容");
                return;
            }

            mProgress = ProgressDialog.show(v.getContext(), null, "发送中···", true, true);

            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {

                    if (mProgress != null) mProgress.dismiss();

                    if (msg.what == 1) {
                        Result res = (Result) msg.obj;
                        if (res.isOk()) {
                            //发送通知广播
                            UIHelper.ToastMessage(MailDetail.this, "发送好咧！");

                            UIHelper.sendBroadCast(MailDetail.this, res);
                            //恢复初始底部栏
                            mFootViewSwitcher.setDisplayedChild(0);
                            mFootEditer.clearFocus();
                            mFootEditer.setText("");
                            mFootEditer.setVisibility(View.GONE);
                            //显示刚刚发送的留言
                            //清除之前保存的编辑内容
                            ac.removeProperty(tempMessageKey);
                        } else {
                            UIHelper.ToastMessage(MailDetail.this, "失败了！");

                        }
                    } else {
                        ((AppException) msg.obj).makeToast(MailDetail.this);
                    }
                }
            };
            new Thread() {
                public void run() {
                    Message msg = new Message();
                    try {
                        Result res = ac.sendMail(_url, _title, _user, _backup, _content);
                        msg.what = 1;
                        msg.obj = res;
                    } catch (AppException e) {
                        e.printStackTrace();
                        msg.what = -1;
                        msg.obj = e;
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        }
    };
}
