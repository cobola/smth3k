package com.jimidigi.smth3k.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.adapter.ListViewBoardAdapter;
import com.jimidigi.smth3k.adapter.ListViewMailAdapter;
import com.jimidigi.smth3k.adapter.ListViewSubjectAdapter;
import com.jimidigi.smth3k.bean.*;
import com.jimidigi.smth3k.common.StringUtility;
import com.jimidigi.smth3k.common.UIHelper;
import com.jimidigi.smth3k.widget.BadgeView;
import com.jimidigi.smth3k.widget.PullToRefreshListView;
import com.jimidigi.smth3k.widget.ScrollLayout;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengDownloadListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import greendroid.widget.MyQuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
import net.youmi.android.AdManager;
import net.youmi.android.diy.banner.DiyAdSize;
import net.youmi.android.diy.banner.DiyBanner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 应用程序首页
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Main extends BaseActivity {

    public static final int QUICKACTION_LOGIN_OR_LOGOUT = 0;
    public static final int QUICKACTION_USERINFO = 1;
    public static final int QUICKACTION_SETTING = 2;
    public static final int QUICKACTION_EXIT = 3;

    private ScrollLayout mScrollLayout;


    private RadioButton[] mButtons;
    private String[] mHeadTitles;
    private int mViewCount;
    private int mCurSel;


    private ImageView mHeadLogo;
    private TextView mHeadTitle;
    private ProgressBar mHeadProgress;
    private Button mHeadSearch;
    private EditText mHeadSearchBox;
    private ImageButton mHeadPub_post;
    private ImageButton mHeadPub_mail;


    private PullToRefreshListView lvSubject;
    private ListView lvBoard;
    private ListView lvFavBoard;

    private PullToRefreshListView lvMail;

    private ListViewSubjectAdapter lvSubjectAdapter;
    private ListViewBoardAdapter lvBoardAdapter;
    private ListViewBoardAdapter lvFavBoardAdapter;
    private ListViewMailAdapter lvMailAdapter;

    private List<Board> lvBoardata = new ArrayList<Board>();
    private List<Board> lvFavBoardata = new ArrayList<Board>();
    private List<Subject> lvSubjectData = new ArrayList<Subject>();
    private List<Mail> lvMailData = new ArrayList<Mail>();

    private Handler lvBoardHandler;
    private Handler lvFavBoardHandler;
    private Handler lvSubjectHandler;
    private Handler lvMailHandler;


    private RadioButton fbPost;
    private RadioButton fbBoard;
    private RadioButton fbFav;
    private RadioButton fbMail;

    private ImageView fbSetting;

    private int curPage = 0;
    private Board curBoard;

    private String curBox = Mail.INBOX;


    private Button framebtn_Fav_board;
    private Button framebtn_Fav_box;
    private Button framebtn_Mail_inbox;
    private Button framebtn_Mail_outbox;
    private Button framebtn_Mail_deleted;
    private Button framebtn_Refer_at;
    private Button framebtn_Refer_reply;


    private View lvSubject_footer;
    private View lvMail_footer;

    private TextView lvSubject_foot_more;
    private TextView lvMail_foot_more;

    private ProgressBar lvSubject_foot_progress;
    private ProgressBar lvMail_foot_progress;

    public static BadgeView bv_message;

    private QuickActionWidget mGrid;//快捷栏控件

    private boolean isClearNotice = false;
    private int curClearNoticeType = 0;

    private MailReceiver mailReceiver;//动弹发布接收器
    private AppContext appContext;//全局Context


    private InputMethodManager imm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //注册广播接收器
        mailReceiver = new MailReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.jimidigi.smth3k.action.APP_TWEETPUB");
        registerReceiver(mailReceiver, filter);

        appContext = (AppContext) getApplication();
        //网络连接判断
        if (!appContext.isNetworkConnected())
            UIHelper.ToastMessage(this, R.string.network_not_connected);
        //初始化登录

        appContext.initLoginInfo();


        this.initHeadView();
        this.initFootBar();
        this.initPageScroll();
        this.initFrameButton();
        this.initBadgeView();
        this.initQuickActionGrid();
        this.initFrameListView();

        //检查新版本
        if (appContext.isCheckUp()) {
            UmengUpdateAgent.update(this);
            UmengUpdateAgent.setUpdateAutoPopup(false);
            UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
                @Override
                public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                    switch (updateStatus) {
                        case 0: // has update
                            UmengUpdateAgent.showUpdateDialog(Main.this, updateInfo);
                            break;
                        case 1: // has no update
                            Toast.makeText(getApplicationContext(), "没有更新", Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        case 2: // none wifi
                            Toast.makeText(getApplicationContext(), "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        case 3: // time out
                            Toast.makeText(getApplicationContext(), "超时", Toast.LENGTH_SHORT)
                                    .show();
                            break;
                    }
                }
            });

            UmengUpdateAgent.setDownloadListener(new UmengDownloadListener() {
                @Override
                public void OnDownloadEnd(int result) {
                    Toast.makeText(getApplicationContext(), "下载完成 : " + result, Toast.LENGTH_SHORT).show();
                }
            });
        }


        AdManager.getInstance(this).init("14ab017831bb686c", "6789f4956be67a6e", false);
//        OffersManager.getInstance(this).onAppLaunch();

        //启动轮询通知信息
        this.foreachUserNotice();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mViewCount == 0) mViewCount = 4;
        if (mCurSel == 0 && !fbPost.isChecked()) {
            fbPost.setChecked(true);
            fbBoard.setChecked(false);
            fbFav.setChecked(false);
            fbMail.setChecked(false);
        }
        //读取左右滑动配置
        mScrollLayout.setIsScroll(appContext.isScroll());


        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mailReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("LOGIN", false)) {
            //加载十大吧
            if (lvSubjectData.isEmpty()) {
                this.loadLvTop10Data(lvSubjectHandler, 0, UIHelper.LISTVIEW_ACTION_INIT);
            }

        } else {
            //查看最新信息
            mScrollLayout.scrollToScreen(2);

        }
    }

    public class MailReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            int what = intent.getIntExtra("MSG_WHAT", 0);
            if (what == 1) {
                Result res = (Result) intent.getSerializableExtra("RESULT");
                UIHelper.ToastMessage(context, res.getErrorMessage(), 1000);
                if (res.isOk()) {
                    //发送通知广播

                    UIHelper.sendBroadCast(context, res);
                    loadLvMailData(Mail.INBOX, 1, lvMailHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
                }
            }
        }
    }

    /**
     * 初始化快捷栏
     */
    private void initQuickActionGrid() {
        mGrid = new QuickActionGrid(this);
        mGrid.addQuickAction(new MyQuickAction(this, R.drawable.ic_menu_login, R.string.main_menu_login));
        mGrid.addQuickAction(new MyQuickAction(this, R.drawable.ic_menu_myinfo, R.string.main_menu_myinfo));
        mGrid.addQuickAction(new MyQuickAction(this, R.drawable.ic_menu_setting, R.string.main_menu_setting));
        mGrid.addQuickAction(new MyQuickAction(this, R.drawable.ic_menu_exit, R.string.main_menu_exit));

        mGrid.setOnQuickActionClickListener(mActionListener);
    }

    /**
     * 快捷栏item点击事件
     */
    private OnQuickActionClickListener mActionListener = new OnQuickActionClickListener() {
        public void onQuickActionClicked(QuickActionWidget widget, int position) {
            switch (position) {
                case QUICKACTION_LOGIN_OR_LOGOUT://用户登录-注销
                    UIHelper.loginOrLogout(Main.this);
                    break;
                case QUICKACTION_USERINFO://我的资料
                    UIHelper.showUserInfo(Main.this);
                    break;
                case QUICKACTION_SETTING://设置
                    UIHelper.showSetting(Main.this);
                    break;
                case QUICKACTION_EXIT://退出
                    UIHelper.Exit(Main.this);
                    break;
            }
        }
    };

    /**
     * 初始化所有ListView
     */
    private void initFrameListView() {
        //初始化listview控件
        this.initSubjectListView();
        this.initBoardListView();
        this.initFavBoardListView();
        this.initMailListView();
        //加载listview数据
        this.initFrameListViewData();
    }

    /**
     * 初始化所有ListView数据
     */
    private void initFrameListViewData() {
        //初始化Handler
        lvSubjectHandler = this.getLvHandler(lvSubject, lvSubjectAdapter, lvSubject_foot_more, lvSubject_foot_progress);
        lvBoardHandler = this.getLvHandler(lvBoard, lvBoardAdapter);
        lvFavBoardHandler = this.getLvHandler(lvFavBoard, lvFavBoardAdapter);
        lvMailHandler = this.getLvHandler(lvMail, lvMailAdapter, lvMail_foot_more, lvMail_foot_progress);

        //加载资讯数据
        if (lvSubjectData.isEmpty()) {
            loadLvTop10Data(lvSubjectHandler, 0, UIHelper.LISTVIEW_ACTION_INIT);
        }
    }

    /**
     * 初始化十大列表
     */
    private void initSubjectListView() {
        lvSubjectAdapter = new ListViewSubjectAdapter(this, lvSubjectData, R.layout.subject_listitem);
        lvSubject_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
        lvSubject_foot_more = (TextView) lvSubject_footer.findViewById(R.id.listview_foot_more);
        lvSubject_foot_progress = (ProgressBar) lvSubject_footer.findViewById(R.id.listview_foot_progress);

        lvSubject = (PullToRefreshListView) findViewById(R.id.frame_listview_subject);
        lvSubject.addFooterView(lvSubject_footer);//添加底部视图  必须在setAdapter前
        lvSubject.setAdapter(lvSubjectAdapter);

        lvSubject.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                lvSubject.onScrollStateChanged(view, scrollState);

                //数据为空--不用继续下面代码了
                if (lvSubjectData.isEmpty()) return;

                //判断是否滚动到底部
                boolean scrollEnd = false;
                try {
                    if (view.getPositionForView(lvSubject_footer) == view.getLastVisiblePosition())
                        scrollEnd = true;
                } catch (Exception e) {
                    scrollEnd = false;
                }

                int lvDataState = StringUtility.toInt(lvSubject.getTag());
                if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
                    lvSubject.setTag(UIHelper.LISTVIEW_DATA_LOADING);

                    lvSubject_foot_more.setText(R.string.load_ing);
                    lvSubject_foot_progress.setVisibility(View.VISIBLE);
                    //当前pageIndex
                    loadLvTop10Data(lvSubjectHandler, curPage + 1, UIHelper.LISTVIEW_ACTION_REFRESH);
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lvSubject.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });
        lvSubject.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                loadLvTop10Data(lvSubjectHandler, curPage, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });


    }


    /**
     * 初始收藏版面列表
     */
    private void initFavBoardListView() {
        lvFavBoardAdapter = new ListViewBoardAdapter(this, lvFavBoardata, R.layout.board_listitem);
        lvFavBoard = (ListView) findViewById(R.id.frame_listview_favboard);
        lvFavBoard.setAdapter(lvFavBoardAdapter);

        lvFavBoard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView name = (TextView) view.findViewById(R.id.board_listitem_name);
                Board board = (Board) name.getTag();
                if (board == null) return;
                curBoard = board;
                if (board.isDirectory()) {
                    loadLvBoardData(board, lvFavBoardHandler, UIHelper.LISTVIEW_ACTION_REFRESH, UIHelper.LISTVIEW_DATATYPE_FAV_BOARD);
                } else {
                    UIHelper.showBoardDetail(view.getContext(), board.getEngName(), UIHelper.LISTVIEW_BOARD_TYPE_SUBJECT, 1);
                }
            }
        });

    }


    /**
     * 初始版面列表
     */
    private void initBoardListView() {
        lvBoardAdapter = new ListViewBoardAdapter(this, lvBoardata, R.layout.board_listitem);
        lvBoard = (ListView) findViewById(R.id.frame_listview_board);
        lvBoard.setAdapter(lvBoardAdapter);

        lvBoard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView name = (TextView) view.findViewById(R.id.board_listitem_name);
                Board board = (Board) name.getTag();
                if (board == null) return;
                curBoard = board;
                if (board.isDirectory()) {
                    loadLvBoardData(board, lvBoardHandler, UIHelper.LISTVIEW_ACTION_REFRESH, UIHelper.LISTVIEW_DATATYPE_BOARD);
                } else {
                    UIHelper.showBoardDetail(view.getContext(), board.getEngName(), UIHelper.LISTVIEW_BOARD_TYPE_SUBJECT, 1);
                }
            }
        });

    }


    /**
     * 初始化信箱列表
     */
    private void initMailListView() {
        lvMailAdapter = new ListViewMailAdapter(this, lvMailData, R.layout.mail_listitem);
        lvMail_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
        lvMail_foot_more = (TextView) lvMail_footer.findViewById(R.id.listview_foot_more);
        lvMail_foot_progress = (ProgressBar) lvMail_footer.findViewById(R.id.listview_foot_progress);
        lvMail = (PullToRefreshListView) findViewById(R.id.frame_listview_mail);
        lvMail.addFooterView(lvMail_footer);//添加底部视图  必须在setAdapter前
        lvMail.setAdapter(lvMailAdapter);
        lvMail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击头部、底部栏无效
                if (position == 0 || view == lvMail_footer) return;

                Mail mail = null;
                //判断是否是TextView
                if (view instanceof TextView) {
                    mail = (Mail) view.getTag();
                } else {
                    TextView tv = (TextView) view.findViewById(R.id.message_listitem_title);
                    mail = (Mail) tv.getTag();
                }
                if (mail == null) return;

                if (mail.getUrl() != null) {

                    UIHelper.showPostDetail(view.getContext(), mail.getUrl());
                } else {
                    //跳转到留言详情
                    UIHelper.showMailDetail(view.getContext(), mail.getBoxType(), mail.getId());
                }

            }
        });
        lvMail.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                lvMail.onScrollStateChanged(view, scrollState);

                //数据为空--不用继续下面代码了
                if (lvMailData.isEmpty()) return;

                //判断是否滚动到底部
                boolean scrollEnd = false;
                try {
                    if (view.getPositionForView(lvMail_footer) == view.getLastVisiblePosition())
                        scrollEnd = true;
                } catch (Exception e) {
                    scrollEnd = false;
                }

                int lvDataState = StringUtility.toInt(lvMail.getTag());
                if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
                    lvMail.setTag(UIHelper.LISTVIEW_DATA_LOADING);
                    lvMail_foot_more.setText(R.string.load_ing);
                    lvMail_foot_progress.setVisibility(View.VISIBLE);

                    loadLvMailData(curBox, curPage + 1, lvMailHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lvMail.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });
        lvMail.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //点击头部、底部栏无效
                if (position == 0 || view == lvMail_footer) return false;

                Mail _msg = null;
                //判断是否是TextView
                if (view instanceof TextView) {
                    _msg = (Mail) view.getTag();
                } else {
                    TextView tv = (TextView) view.findViewById(R.id.message_listitem_title);
                    _msg = (Mail) tv.getTag();
                }
                if (_msg == null) return false;

                final Mail mail = _msg;

                //选择操作
                final Handler handler = new Handler() {
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            Result res = (Result) msg.obj;
                            if (res.isOk()) {
                                lvMailData.remove(mail);
                                lvMailAdapter.notifyDataSetChanged();
                            }
                            UIHelper.ToastMessage(Main.this, res.getErrorMessage());
                        } else {
                            ((AppException) msg.obj).makeToast(Main.this);
                        }
                    }
                };
                Thread thread = new Thread() {
                    public void run() {
                        Message msg = new Message();
                        try {


                            if (mail.getType() == Mail.TypeRef) {
                                Result res = appContext.delRef(mail.getUrl());
                                msg.what = 1;
                                msg.obj = res;
                            }

                            if (mail.getType() == Mail.TypeMail) {
                                String delurl = URLs.HOST + mail.getBoxType() + "/delete/" + mail.getId();
                                Result res = appContext.delMail(delurl);
                                msg.what = 1;
                                msg.obj = res;
                            }

                        } catch (AppException e) {
                            e.printStackTrace();
                            msg.what = -1;
                            msg.obj = e;
                        }
                        handler.sendMessage(msg);
                    }
                };

                if (mail.getType() == Mail.TypeMail) {

                    UIHelper.showMailListOptionDialog(Main.this, mail, thread);
                }

                if (mail.getType() == Mail.TypeRef) {

                    UIHelper.showRefOptionDialog(Main.this, mail, thread);
                }
                return true;
            }
        });
        lvMail.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                //清除通知信息
                if (bv_message.isShown()) {
                    isClearNotice = true;
                    curClearNoticeType = Result.typeAtme;
                }
                //刷新数据
                loadLvMailData(curBox, curPage - 1, lvMailHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }

    /**
     * 初始化头部视图
     */
    private void initHeadView() {


        mHeadLogo = (ImageView) findViewById(R.id.main_head_logo);
        mHeadTitle = (TextView) findViewById(R.id.main_head_title);
        mHeadProgress = (ProgressBar) findViewById(R.id.main_head_progress);
        mHeadPub_post = (ImageButton) findViewById(R.id.main_head_pub_post);
        mHeadPub_mail = (ImageButton) findViewById(R.id.main_head_pub_mail);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);


        mHeadSearch = (Button) findViewById(R.id.main_head_search_btn);
        mHeadSearchBox = (EditText) findViewById(R.id.main_head_search_box);

        mHeadSearchBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mHeadSearchBox.setFocusable(true);
                mHeadSearchBox.requestFocus();
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                mHeadSearchBox.requestFocusFromTouch();
            }
        });


        mHeadSearchBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                } else {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });


        mHeadSearchBox.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mHeadSearchBox.clearFocus();

                    return true;
                }
                return false;
            }
        });
        //编辑器添加文本监听
        mHeadSearchBox.addTextChangedListener(UIHelper.getTextWatcher(this, "main_search_box"));

        //显示临时编辑内容
        UIHelper.showTempEditContent(this, mHeadSearchBox, "main_search_box");

        mHeadSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (StringUtility.isEmpty(mHeadSearchBox.getText().toString())) {
                    UIHelper.ToastMessage(Main.this, "请输入需要检索的版面ID");
                    return;
                }
                Board board = new Board();
                board.setDirectory(true);
                board.setChsName("版面搜索结果");
                board.setUrl("/go?name=" + mHeadSearchBox.getText());
                mScrollLayout.snapToScreen(1);
                loadLvBoardData(board, lvBoardHandler, UIHelper.LISTVIEW_ACTION_REFRESH, UIHelper.LISTVIEW_DATATYPE_BOARD);

            }
        });


        mHeadPub_post.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UIHelper.showPostPub(v.getContext());
            }
        });
        mHeadPub_mail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UIHelper.showMailPub(Main.this);
            }
        });

    }

    /**
     * 初始化底部栏
     */
    private void initFootBar() {
        fbPost = (RadioButton) findViewById(R.id.main_footbar_post);
        fbBoard = (RadioButton) findViewById(R.id.main_footbar_board);
        fbFav = (RadioButton) findViewById(R.id.main_footbar_fav);
        fbMail = (RadioButton) findViewById(R.id.main_footbar_mail);
        fbSetting = (ImageView) findViewById(R.id.main_footbar_setting);
        fbSetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //展示快捷栏&判断是否登录&是否加载文章图片
                UIHelper.showSettingLoginOrLogout(Main.this, mGrid.getQuickAction(0));
                mGrid.show(v);
            }
        });
    }

    /**
     * 初始化通知信息标签控件
     */
    private void initBadgeView() {

        bv_message = new BadgeView(this);
        bv_message.setBackgroundResource(R.drawable.widget_count_bg);
        bv_message.setIncludeFontPadding(false);
        bv_message.setGravity(Gravity.CENTER);
        bv_message.setTextSize(8f);
        bv_message.setTextColor(Color.WHITE);
    }

    /**
     * 初始化水平滚动翻页
     */
    private void initPageScroll() {
        mScrollLayout = (ScrollLayout) findViewById(R.id.main_scrolllayout);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main_linearlayout_footer);
        mHeadTitles = getResources().getStringArray(R.array.head_titles);
        mViewCount = mScrollLayout.getChildCount();
        mButtons = new RadioButton[mViewCount];

        for (int i = 0; i < mViewCount; i++) {
            mButtons[i] = (RadioButton) linearLayout.getChildAt(i * 2);
            mButtons[i].setTag(i);
            mButtons[i].setChecked(false);
            mButtons[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int pos = (Integer) (v.getTag());
                    //点击当前项刷新
                    if (mCurSel == pos) {
                        switch (pos) {
                            case 0://post
                                lvSubject.clickRefresh();
                                break;
                            case 1://board
                                loadLvBoardData(UIHelper.getRootBoard(), lvBoardHandler, UIHelper.LISTVIEW_ACTION_REFRESH, UIHelper.LISTVIEW_DATATYPE_BOARD);
                                break;
                            case 2://board
                                loadLvBoardData(UIHelper.getRootFavBoard(), lvFavBoardHandler, UIHelper.LISTVIEW_ACTION_REFRESH, UIHelper.LISTVIEW_DATATYPE_FAV_BOARD);
                                break;

                        }
                    }
                    mScrollLayout.snapToScreen(pos);
                }
            });
        }

        //设置第一显示屏
        mCurSel = 0;
        mButtons[mCurSel].setChecked(true);

        mScrollLayout.SetOnViewChangeListener(new ScrollLayout.OnViewChangeListener() {
            public void OnViewChange(int viewIndex) {
                //切换列表视图-如果列表数据为空：加载数据
                switch (viewIndex) {
                    case 0://十大
                        if (lvSubjectData.isEmpty()) {
                            loadLvTop10Data(lvSubjectHandler, 0, UIHelper.LISTVIEW_ACTION_INIT);
                        }
                        break;
                    case 1://版面
                        loadLvBoardData(UIHelper.getRootBoard(), lvBoardHandler, UIHelper.LISTVIEW_ACTION_INIT, UIHelper.LISTVIEW_DATATYPE_BOARD);
                        break;
                    case 2://fav board
                        if (!appContext.isLogin()) {
                            UIHelper.showLoginDialog(Main.this);
                            break;
                        }
                        loadLvBoardData(UIHelper.getRootFavBoard(), lvFavBoardHandler, UIHelper.LISTVIEW_ACTION_INIT, UIHelper.LISTVIEW_DATATYPE_FAV_BOARD);
                        break;
                    case 3://邮箱
                        //判断登录
                        if (!appContext.isLogin()) {
                            if (lvMail.getVisibility() == View.VISIBLE && lvMailData.isEmpty()) {
                                lvMail_foot_more.setText(R.string.load_empty);
                                lvMail_foot_progress.setVisibility(View.GONE);
                            }
                            UIHelper.showLoginDialog(Main.this);
                            break;
                        }
                        //处理通知信息
                        loadLvMailData(curBox, 1, lvMailHandler, UIHelper.LISTVIEW_ACTION_REFRESH);

                }
                setCurPoint(viewIndex);
            }
        });
    }

    /**
     * 设置底部栏当前焦点
     *
     * @param index
     */
    private void setCurPoint(int index) {
        if (index < 0 || index > mViewCount - 1 || mCurSel == index)
            return;

        mButtons[mCurSel].setChecked(false);
        mButtons[index].setChecked(true);
        mHeadTitle.setText(mHeadTitles[index]);
        mCurSel = index;
        //头部logo、发帖、发动弹按钮显示
        switch (index) {
            case 0:
            case 1:
            case 2:
                mHeadPub_mail.setVisibility(View.GONE);
                mHeadSearch.setVisibility(View.VISIBLE);
                mHeadSearchBox.setVisibility(View.VISIBLE);
                break;
            case 3:
                mHeadPub_mail.setVisibility(View.VISIBLE);
                mHeadSearch.setVisibility(View.GONE);
                mHeadSearchBox.setVisibility(View.GONE);

                break;

        }

    }


    /**
     *
     */
    private void initFrameButton() {
        //初始化按钮控件

        framebtn_Fav_board = (Button) findViewById(R.id.frame_btn_fav_board);
        framebtn_Fav_box = (Button) findViewById(R.id.frame_btn_fav_box);
        framebtn_Refer_at = (Button) findViewById(R.id.frame_btn_refer_at);
        framebtn_Refer_reply = (Button) findViewById(R.id.frame_btn_refer_reply);
        framebtn_Mail_inbox = (Button) findViewById(R.id.frame_btn_mail_inbox);
        framebtn_Mail_outbox = (Button) findViewById(R.id.frame_btn_mail_outbox);
        framebtn_Mail_deleted = (Button) findViewById(R.id.frame_btn_mail_deleted);

        framebtn_Refer_at.setOnClickListener(frameReferBtnClick(framebtn_Refer_at, Mail.AT));
        framebtn_Refer_reply.setOnClickListener(frameReferBtnClick(framebtn_Refer_reply, Mail.REPLY));

        framebtn_Mail_inbox.setOnClickListener(frameMailBtnClick(framebtn_Mail_inbox, Mail.INBOX));
        framebtn_Mail_outbox.setOnClickListener(frameMailBtnClick(framebtn_Mail_outbox, Mail.OUTBOX));
        framebtn_Mail_deleted.setOnClickListener(frameMailBtnClick(framebtn_Mail_deleted, Mail.DELETED));


    }


    private View.OnClickListener frameReferBtnClick(final Button btn, final String type) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                loadLvRefData(type, 1, lvMailHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
                if (btn == framebtn_Refer_at) {
                    framebtn_Refer_at.setEnabled(false);
                } else {
                    framebtn_Refer_at.setEnabled(true);
                }

                if (btn == framebtn_Refer_reply) {
                    framebtn_Refer_reply.setEnabled(false);
                } else {
                    framebtn_Refer_reply.setEnabled(true);
                }

                framebtn_Mail_inbox.setEnabled(true);
                framebtn_Mail_outbox.setEnabled(true);
                framebtn_Mail_deleted.setEnabled(true);
            }
        };
    }


    private View.OnClickListener frameMailBtnClick(final Button btn, final String type) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                loadLvMailData(type, 1, lvMailHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
                if (btn == framebtn_Mail_inbox) {
                    framebtn_Mail_inbox.setEnabled(false);
                } else {
                    framebtn_Mail_inbox.setEnabled(true);
                }

                if (btn == framebtn_Mail_outbox) {
                    framebtn_Mail_outbox.setEnabled(false);
                } else {
                    framebtn_Mail_outbox.setEnabled(true);
                }

                if (btn == framebtn_Mail_deleted) {
                    framebtn_Mail_deleted.setEnabled(false);
                } else {
                    framebtn_Mail_deleted.setEnabled(true);
                }
                framebtn_Refer_at.setEnabled(true);
                framebtn_Refer_reply.setEnabled(true);

            }
        };
    }


    private Handler getLvHandler(final ListView lv, final BaseAdapter adapter) {
        return new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what >= 0) {
                    //listview数据处理
                    Result notice = handleLvData(msg.what, msg.obj, msg.arg2, msg.arg1);
                    lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
                    adapter.notifyDataSetChanged();
                    //发送通知广播
                    if (notice != null) {
                        UIHelper.sendBroadCast(lv.getContext(), notice);
                    }
                    //是否清除通知信息
                    if (isClearNotice) {
                        ClearNotice(curClearNoticeType);
                        isClearNotice = false;//重置
                        curClearNoticeType = 0;
                    }
                } else if (msg.what == -1) {
                    //有异常--显示加载出错 & 弹出错误消息
                    ((AppException) msg.obj).makeToast(Main.this);
                }
                mHeadProgress.setVisibility(ProgressBar.GONE);

                if (!lv.isStackFromBottom()) {
                    lv.setStackFromBottom(true);
                }
                lv.setStackFromBottom(false);

            }
        };
    }

    /**
     * 获取listview的初始化Handler
     *
     * @param lv
     * @param adapter
     * @return
     */
    private Handler getLvHandler(final PullToRefreshListView lv, final BaseAdapter adapter, final TextView more, final ProgressBar progress) {
        return new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what >= 0) {
                    //listview数据处理

                    Result notice = handleLvData(msg.what, msg.obj, msg.arg2, msg.arg1);


                    if (msg.what == curPage || msg.arg2 == UIHelper.LISTVIEW_DATATYPE_SUBJECT_HOT) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_full);
                    }
                    if (msg.what > curPage) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_more);
                    }
                    //发送通知广播
                    if (notice != null) {
                        UIHelper.sendBroadCast(lv.getContext(), notice);
                    }
                    //是否清除通知信息
                    if (isClearNotice) {
                        ClearNotice(curClearNoticeType);
                        isClearNotice = false;//重置
                        curClearNoticeType = 0;
                    }
                } else if (msg.what == -1) {
                    //有异常--显示加载出错 & 弹出错误消息
                    lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
                    more.setText(R.string.load_error);
                    ((AppException) msg.obj).makeToast(Main.this);
                }
                if (adapter.getCount() == 0) {
                    lv.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
                    more.setText(R.string.load_empty);
                }
                progress.setVisibility(ProgressBar.GONE);
                mHeadProgress.setVisibility(ProgressBar.GONE);

                if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
                    lv.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());


                    if (!lv.isStackFromBottom()) {
                        lv.setStackFromBottom(true);
                    }
                    lv.setStackFromBottom(false);
                } else if (msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG) {
                    lv.onRefreshComplete();

                    if (!lv.isStackFromBottom()) {
                        lv.setStackFromBottom(true);
                    }
                    lv.setStackFromBottom(false);
                }

            }
        };
    }

    /**
     * listview数据处理
     *
     * @param what       数量
     * @param obj        数据
     * @param objtype    数据类型
     * @param actiontype 操作类型
     * @return notice 通知信息
     */
    private Result handleLvData(int what, Object obj, int objtype, int actiontype) {
        Result notice = null;
        switch (actiontype) {
            case UIHelper.LISTVIEW_ACTION_INIT:
            case UIHelper.LISTVIEW_ACTION_REFRESH:
            case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
                switch (objtype) {
                    case UIHelper.LISTVIEW_DATATYPE_SUBJECT_HOT:
                        SubjectList nlist = (SubjectList) obj;
                        curPage = nlist.getPageNow();
                        notice = nlist.getResult();
                        mHeadTitle.setText(nlist.getBoardChsName());
                        lvSubjectData.clear();//先清除原有数据
                        lvSubjectData.addAll(nlist.getSubjectList());

                        if (!lvSubject.isStackFromBottom()) {
                            lvSubject.setStackFromBottom(true);
                        }
                        lvSubject.setStackFromBottom(false);

                        break;
                    case UIHelper.LISTVIEW_DATATYPE_BOARD:
                        Board board = (Board) obj;
                        notice = board.getResult();
                        mHeadTitle.setText(board.getChsName());
                        lvBoardata.clear();//先清除原有数据
                        lvBoardata.addAll(board.getChildBoards());
                        break;
                    case UIHelper.LISTVIEW_DATATYPE_FAV_BOARD:
                        board = (Board) obj;
                        notice = board.getResult();
                        mHeadTitle.setText(board.getChsName());
                        lvFavBoardata.clear();//先清除原有数据
                        lvFavBoardata.addAll(board.getChildBoards());
                        break;
                    case UIHelper.LISTVIEW_DATATYPE_MAIL:
                        MailList mlist = (MailList) obj;
                        notice = mlist.getResult();
                        curPage = mlist.getPageNow();
                        curBox = mlist.getBoxType();
                        mHeadTitle.setText(mlist.getTitle());
                        lvMailData.clear();
                        lvMailData.addAll(mlist.getMailList());
                        break;
                    case UIHelper.LISTVIEW_DATATYPE_REF:
                        mlist = (MailList) obj;
                        notice = mlist.getResult();
                        curPage = mlist.getPageNow();
                        curBox = mlist.getBoxType();
                        mHeadTitle.setText(mlist.getTitle());
                        lvMailData.clear();
                        lvMailData.addAll(mlist.getMailList());
                        break;

                }

            case UIHelper.LISTVIEW_ACTION_SCROLL:
                switch (objtype) {
                    case UIHelper.LISTVIEW_DATATYPE_SUBJECT_HOT:
                        SubjectList nlist = (SubjectList) obj;
                        curPage = nlist.getPageNow();
                        notice = nlist.getResult();
                        mHeadTitle.setText(nlist.getBoardChsName());
                        lvSubjectData.clear();//先清除原有数据
                        lvSubjectData.addAll(nlist.getSubjectList());

                        if (!lvSubject.isStackFromBottom()) {
                            lvSubject.setStackFromBottom(true);
                        }
                        lvSubject.setStackFromBottom(false);

                        break;
                    case UIHelper.LISTVIEW_DATATYPE_BOARD:
                        Board board = (Board) obj;
                        notice = board.getResult();
                        mHeadTitle.setText(board.getChsName());
                        lvBoardata.clear();//先清除原有数据
                        lvBoardata.addAll(board.getChildBoards());
                        break;
                    case UIHelper.LISTVIEW_DATATYPE_FAV_BOARD:
                        board = (Board) obj;
                        notice = board.getResult();
                        mHeadTitle.setText(board.getChsName());
                        lvFavBoardata.clear();//先清除原有数据
                        lvFavBoardata.addAll(board.getChildBoards());
                        break;
                    case UIHelper.LISTVIEW_DATATYPE_MAIL:
                        MailList mlist = (MailList) obj;
                        notice = mlist.getResult();
                        curPage = mlist.getPageNow();
                        curBox = mlist.getBoxType();
                        mHeadTitle.setText(mlist.getTitle());
                        lvMailData.addAll(mlist.getMailList());
                        break;
                    case UIHelper.LISTVIEW_DATATYPE_REF:
                        mlist = (MailList) obj;
                        notice = mlist.getResult();
                        curPage = mlist.getPageNow();
                        curBox = mlist.getBoxType();
                        mHeadTitle.setText(mlist.getTitle());
                        lvMailData.addAll(mlist.getMailList());
                        break;
                }
//                if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
//                    //提示新加载数据
//                    if (newdata > 0) {
//                        NewDataToast.makeText(this, getString(R.string.new_data_toast_message, newdata), appContext.isAppSound()).show();
//                    }
//                    if (newdata == 0) {
//                        NewDataToast.makeText(this, getString(R.string.new_data_toast_none), false).show();
//                    }
//                }
                break;
        }
        return notice;
    }


    /**
     * 线程加载十大数据
     *
     * @param handler 处理器
     * @param action  动作标识
     */
    private void loadLvTop10Data(final Handler handler, final int page, final int action) {
        mHeadProgress.setVisibility(ProgressBar.VISIBLE);
        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    SubjectList list = appContext.getHotSubjectList(isRefresh);
                    curPage = list.getPageNow();
                    msg.what = list.getPageCount();
                    msg.obj = list;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;
                msg.arg2 = UIHelper.LISTVIEW_DATATYPE_SUBJECT_HOT;

                handler.sendMessage(msg);
            }
        }.start();
    }


    /**
     * 加载版面列表
     *
     * @param parent
     * @param handler
     * @param action
     * @param type
     */
    private void loadLvBoardData(final Board parent, final Handler handler, final int action, final int type) {
        mHeadProgress.setVisibility(ProgressBar.VISIBLE);
        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    Board board = appContext.getBoardList(parent, isRefresh);
                    curPage = 1;
                    msg.what = 1;
                    msg.obj = board;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;
                msg.arg2 = type;
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 线程加载留言数据
     *
     * @param pageIndex 当前页数
     * @param handler
     * @param action
     */
    private void loadLvMailData(final String boxType, final int pageIndex, final Handler handler, final int action) {
        mHeadProgress.setVisibility(ProgressBar.VISIBLE);
        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    MailList list = appContext.getMailList(boxType, pageIndex, isRefresh);
                    curPage = list.getPageNow();
                    curBox = list.getBoxType();
                    msg.what = list.getPageCount();
                    msg.obj = list;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;
                msg.arg2 = UIHelper.LISTVIEW_DATATYPE_MAIL;
                handler.sendMessage(msg);
            }
        }.start();
    }


    /**
     * 线程加载Ref数据
     *
     * @param pageIndex 当前页数
     * @param handler
     * @param action
     */
    private void loadLvRefData(final String refType, final int pageIndex, final Handler handler, final int action) {
        mHeadProgress.setVisibility(ProgressBar.VISIBLE);
        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    MailList list = appContext.getRefList(refType, pageIndex, isRefresh);
                    curPage = list.getPageNow();
                    curBox = list.getBoxType();
                    msg.what = list.getPageCount();
                    msg.obj = list;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;
                msg.arg2 = UIHelper.LISTVIEW_DATATYPE_REF;
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 轮询通知信息
     */
    private void foreachUserNotice() {
        final String uid = appContext.getLoginUserID();
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    UIHelper.sendBroadCast(Main.this, (Result) msg.obj);
                }
                foreachUserNotice();//回调
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    sleep(60 * 1000);
                    if (uid != null && uid != "guest") {
                        Result notice = appContext.getResult();
                        msg.what = 1;
                        msg.obj = notice;
                    } else {
                        msg.what = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 通知信息处理
     *
     * @param type 1:@我的信息 2:未读消息 3:评论个数 4:新粉丝个数
     */
    private void ClearNotice(final int type) {
        final String uid = appContext.getLoginUserID();
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1 && msg.obj != null) {
                    Result res = (Result) msg.obj;
                    if (res.isOk()) {
                        UIHelper.sendBroadCast(Main.this, res);
                    }
                } else {
                    ((AppException) msg.obj).makeToast(Main.this);
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    Result res = appContext.noticeClear(uid, type);
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

    /**
     * 创建menu TODO 停用原生菜单
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.main_menu, menu);
        //return true;
    }

    /**
     * 菜单被显示之前的事件
     */
    public boolean onPrepareOptionsMenu(Menu menu) {
        UIHelper.showMenuLoginOrLogout(this, menu);
        return true;
    }

    /**
     * 处理menu的事件
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        switch (item_id) {
            case R.id.main_menu_user:
                UIHelper.loginOrLogout(this);
                break;
            case R.id.main_menu_about:
                UIHelper.showAbout(this);
                break;
            case R.id.main_menu_setting:
                UIHelper.showSetting(this);
                break;
            case R.id.main_menu_exit:
                UIHelper.Exit(this);
                break;
        }
        return true;
    }

    /**
     * 监听返回--是否退出程序
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean flag = true;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //是否退出应用
            UIHelper.Exit(this);
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            //展示快捷栏&判断是否登录
            UIHelper.showSettingLoginOrLogout(Main.this, mGrid.getQuickAction(0));
            mGrid.show(fbSetting, true);
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            //展示搜索页
//            UIHelper.showSearch(Main.this);
        } else {
            flag = super.onKeyDown(keyCode, event);
        }
        return flag;
    }
}
