package com.jimidigi.smth3k.ui;

import com.jimidigi.smth3k.AppConfig;
import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.bean.Result;

import com.jimidigi.smth3k.bean.URLs;
import com.jimidigi.smth3k.common.UIHelper;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.jimidigi.smth3k.common.StringUtility;

/**
 * 发表留言
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class MailPub extends BaseActivity {

    private ImageView mBack;

    private EditText mReceiver;
    private EditText mTitle;

    private EditText mContent;
    private Button mPublish;
    private ProgressDialog mProgress;
    private InputMethodManager imm;
    private String tempMessageKey = AppConfig.TEMP_MESSAGE;

    private String _friendid;
    private String _title;
    private String _content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_send);
        this.initView();
    }

    //初始化视图控件
    private void initView() {
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        _friendid = getIntent().getStringExtra("friend_id");
        _title = getIntent().getStringExtra("title");
        _content = getIntent().getStringExtra("content");

//		if(_friendid > 0) tempMessageKey = AppConfig.TEMP_MESSAGE + "_" + _friendid;

        mBack = (ImageView) findViewById(R.id.mail_pub_back);
        mPublish = (Button) findViewById(R.id.mail_pub_publish);
        mReceiver = (EditText) findViewById(R.id.mail_pub_receiver);
        mReceiver.setText(_friendid);
        mTitle = (EditText) findViewById(R.id.mail_pub_title);
        mTitle.setText(_title);
        mContent = (EditText) findViewById(R.id.message_pub_content);


        mBack.setOnClickListener(UIHelper.finish(this));
        mPublish.setOnClickListener(publishClickListener);
        //编辑器添加文本监
        mContent.addTextChangedListener(UIHelper.getTextWatcher(this, tempMessageKey));

        //显示临时编辑内容
        UIHelper.showTempEditContent(this, mContent, _content,tempMessageKey);

    }

    private View.OnClickListener publishClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            //隐藏软键盘
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            _friendid = mReceiver.getText().toString();

            if (StringUtility.isEmpty(_friendid)) {
                UIHelper.ToastMessage(v.getContext(), "请输入收信人");
                return;
            }
            _title = mTitle.getText().toString();

            if (StringUtility.isEmpty(_title)) {
                UIHelper.ToastMessage(v.getContext(), "请输入标题");
                return;
            }

            _content = mContent.getText().toString();
            if (StringUtility.isEmpty(_content)) {
                UIHelper.ToastMessage(v.getContext(), "请输入内容");
                return;
            }


            final AppContext ac = (AppContext) getApplication();
            if (!ac.isLogin()) {
                UIHelper.showLoginDialog(MailPub.this);
                return;
            }

            mProgress = ProgressDialog.show(v.getContext(), null, "发送中···", true, true);

            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {

                    if (mProgress != null) mProgress.dismiss();

                    if (msg.what == 1) {
                        Result res = (Result) msg.obj;
                        UIHelper.ToastMessage(MailPub.this, res.getErrorMessage());
                        if (res.isOk()) {
                            //发送通知广播
                            UIHelper.sendBroadCast(MailPub.this, res);
                            //清除之前保存的编辑内容
                            ac.removeProperty(tempMessageKey);
                            //返回刚刚发表的评论
                            Intent intent = new Intent();
//							intent.putExtra("COMMENT_SERIALIZABLE", res.getComment());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    } else {
                        ((AppException) msg.obj).makeToast(MailPub.this);
                    }
                }
            };
            new Thread() {
                public void run() {
                    Message msg = new Message();
                    try {
                        Result res = ac.sendMail(URLs.HOST + "/mail/send", _title, _friendid, "true", _content);
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
