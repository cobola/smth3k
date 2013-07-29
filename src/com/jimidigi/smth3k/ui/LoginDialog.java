package com.jimidigi.smth3k.ui;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.bean.User;
import com.jimidigi.smth3k.common.StringUtility;
import com.jimidigi.smth3k.common.UIHelper;

import java.io.Serializable;
import java.util.List;

/**
 * 用户登录对话框
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class LoginDialog extends BaseActivity {

    private ViewSwitcher mViewSwitcher;
    private ImageButton btn_close;
    private Button btn_login;
    private AutoCompleteTextView mAccount;
    private EditText mPwd;
    private AnimationDrawable loadingAnimation;
    private View loginLoading;
    private CheckBox chb_remberme;
    private int curLoginType;
    private InputMethodManager imm;

    public final static int LOGIN_OTHER = 0x00;
    public final static int LOGIN_MAIN = 0x01;
    public final static int LOGIN_SETTING = 0x02;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        curLoginType = getIntent().getIntExtra("LOGINTYPE", LOGIN_OTHER);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.logindialog_view_switcher);
        loginLoading = (View) findViewById(R.id.login_loading);
        mAccount = (AutoCompleteTextView) findViewById(R.id.login_account);
        mPwd = (EditText) findViewById(R.id.login_password);
        chb_remberme = (CheckBox) findViewById(R.id.login_checkbox_rememberMe);

        btn_close = (ImageButton) findViewById(R.id.login_close_button);
        btn_close.setOnClickListener(UIHelper.finish(this));

        btn_login = (Button) findViewById(R.id.login_btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //隐藏软键盘
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                String account = mAccount.getText().toString();
                String pwd = mPwd.getText().toString();
                boolean isRememberMe = chb_remberme.isChecked();
                //判断输入
                if (StringUtility.isEmpty(account)) {
                    UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_id_null));
                    return;
                }
                if (StringUtility.isEmpty(pwd)) {
                    UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_pwd_null));
                    return;
                }

                btn_close.setVisibility(View.GONE);
                loadingAnimation = (AnimationDrawable) loginLoading.getBackground();
                loadingAnimation.start();
                mViewSwitcher.showNext();

                login(account, pwd, isRememberMe);
            }
        });

        //是否显示登录信息
        AppContext ac = (AppContext) getApplication();
        User user = ac.getLoginInfo();
        if (user == null || !user.isRemberme()) return;
        if (!StringUtility.isEmpty(user.getUserID())) {
            mAccount.setText(user.getUserID());
            mAccount.selectAll();
            chb_remberme.setChecked(user.isRemberme());
        }
        if (!StringUtility.isEmpty(user.getPassword())) {
            mPwd.setText(user.getPassword());
        }
    }

    //登录验证
    private void login(final String id, final String passwd, final boolean isRememberMe) {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    List user = (List) msg.obj;
                    if (!user.isEmpty()) {
                        //发送通知广播
                        User u = (User) user.get(0);

                        UIHelper.sendBroadCast(LoginDialog.this, u.getResult());
                        //提示登陆成功
                        UIHelper.ToastMessage(LoginDialog.this, R.string.msg_login_success);
                        if (curLoginType == LOGIN_MAIN) {
                            //跳转--加载用户动态
                            Intent intent = new Intent(LoginDialog.this, Main.class);
                            intent.putExtra("LOGIN", true);
                            intent.putExtra("HOTSUBJECT", (Serializable) user.get(1));
                            startActivity(intent);
                        } else if (curLoginType == LOGIN_SETTING) {
                            //跳转--用户设置页面
                            Intent intent = new Intent(LoginDialog.this, Setting.class);
                            intent.putExtra("LOGIN", true);
                            startActivity(intent);
                        }
                        finish();
                    }
                } else if (msg.what == 0) {
                    mViewSwitcher.showPrevious();
                    btn_close.setVisibility(View.VISIBLE);
                    UIHelper.ToastMessage(LoginDialog.this, getString(R.string.msg_login_fail) + msg.obj);
                } else if (msg.what == -1) {
                    mViewSwitcher.showPrevious();
                    btn_close.setVisibility(View.VISIBLE);
                    ((AppException) msg.obj).makeToast(LoginDialog.this);
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    AppContext ac = (AppContext) getApplication();
                    List list = ac.loginVerify(id, passwd);
                    if (!list.isEmpty()) {
                        User user = new User();
                        user.setUserID(id);
                        user.setPassword(passwd);
                        user.setRemberme(isRememberMe);
                        ac.saveLoginInfo(user);//保存登录信息
                        msg.what = 1;//成功
                        msg.obj = list;
                    } else {
                        ac.cleanLoginInfo();//清除登录信息
                        msg.what = 0;//失败
                        msg.obj = "用户名或者密码错误";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.onDestroy();
        }
        return super.onKeyDown(keyCode, event);
    }
}
