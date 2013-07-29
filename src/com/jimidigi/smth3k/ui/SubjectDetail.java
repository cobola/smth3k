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
import android.widget.*;
import com.jimidigi.smth3k.AppConfig;
import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.adapter.ListViewCommentAdapter;
import com.jimidigi.smth3k.bean.Post;
import com.jimidigi.smth3k.bean.Result;
import com.jimidigi.smth3k.bean.Subject;
import com.jimidigi.smth3k.common.StringUtility;
import com.jimidigi.smth3k.common.UIHelper;
import com.jimidigi.smth3k.widget.PullToRefreshListView;
import net.youmi.android.diy.banner.DiyAdSize;
import net.youmi.android.diy.banner.DiyBanner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 新闻详情
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class SubjectDetail extends BaseActivity {

    private FrameLayout mHeader;
    private LinearLayout mFooter;
    private ImageView mBack;
    private ImageView mFavorite;
    private ImageView mRefresh;
    private TextView mHeadTitle;
    private ProgressBar mProgressbar;
    private ImageView mPre;
    private ImageView mNext;
    private ImageView mHome;


    private Handler subjectHandler;
    private Subject subject;
    private String subjectID;
    private String boardEngName;
    private String _content;


    private boolean isLoadImage = true;
    private final static int VIEWSWITCH_TYPE_DETAIL = 0x001;
    private final static int VIEWSWITCH_TYPE_COMMENTS = 0x002;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;
    private final static int DATA_LOAD_FAIL = 0x003;

    private PullToRefreshListView lvPost;
    private ListViewCommentAdapter lvCommentAdapter;
    private List<Post> lvPostData = new ArrayList<Post>();
    private View lvPost_footer;
    private TextView lvPost_foot_more;
    private ProgressBar lvPost_foot_progress;

    private int curPage;
    private int curLvDataState;
    private int curLvPosition;//当前listview选中的item位置

    private ViewSwitcher mFootViewSwitcher;
    private ImageView mFootEditebox;
    private EditText mFootEditer;
    private Button mFootPubcomment;
    private ProgressDialog mProgress;
    private String tempCommentKey = AppConfig.TEMP_COMMENT;

    private GestureDetector gd;
    private boolean isFullScreen;

    private InputMethodManager imm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_detail);

        this.initView();
        this.initCommentView();
        this.initData();

        this.ads();

//        this.initCommentData();

    }

    private void ads() {
        AppContext ac = (AppContext) getApplication();
        if (ac.isLoadAds()) {
            //获取要嵌入迷你广告条的布局
            RelativeLayout adLayout = (RelativeLayout) findViewById(R.id.AdLayout);
            //demo 1 迷你Banner : 宽满屏，高32dp
            DiyBanner banner = new DiyBanner(this, DiyAdSize.SIZE_MATCH_SCREENx32);//传入高度为32dp的AdSize来定义迷你Banner
            //demo 2 迷你Banner : 宽320dp，高32dp
          //将积分Banner加入到布局中
            adLayout.addView(banner);
        }
    }

    //初始化视图控件
    private void initView() {
        boardEngName = getIntent().getStringExtra("boardEngName");
        subjectID = getIntent().getStringExtra("subjectID");

        curPage = 1;
        if (subjectID != null)
            tempCommentKey = AppConfig.TEMP_COMMENT + "_" + boardEngName + "_" + subjectID;

        mHeader = (FrameLayout) findViewById(R.id.subject_detail_header);
        mFooter = (LinearLayout) findViewById(R.id.subject_detail_footer);
        mBack = (ImageView) findViewById(R.id.subject_detail_back);
        mRefresh = (ImageView) findViewById(R.id.subject_detail_refresh);
        mHeadTitle = (TextView) findViewById(R.id.subject_detail_head_title);
        mProgressbar = (ProgressBar) findViewById(R.id.subject_detail_head_progress);

        mPre = (ImageView) findViewById(R.id.subject_detail_footbar_pre);
        mNext = (ImageView) findViewById(R.id.subject_detail_footbar_next);
        mHome = (ImageView) findViewById(R.id.subject_detail_footbar_home);


        mBack.setOnClickListener(UIHelper.finish(this));

        mPre.setOnClickListener(preClickListener);
        mNext.setOnClickListener(nextClickListener);
        mRefresh.setOnClickListener(refreshClickListener);
        mHome.setOnClickListener(homeClickListener);


        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mFootViewSwitcher = (ViewSwitcher) findViewById(R.id.subject_detail_foot_viewswitcher);
        mFootPubcomment = (Button) findViewById(R.id.subject_detail_foot_pubcomment);
        mFootPubcomment.setOnClickListener(commentpubClickListener);
        mFootEditebox = (ImageView) findViewById(R.id.subject_detail_footbar_editebox);
        mFootEditebox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFootViewSwitcher.showNext();
                mFootEditer.setVisibility(View.VISIBLE);
                mFootEditer.requestFocus();
                mFootEditer.requestFocusFromTouch();
            }
        });
        mFootEditer = (EditText) findViewById(R.id.subject_detail_foot_editer);
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
        subjectHandler = new Handler() {
            public void handleMessage(Message msg) {

                headButtonSwitch(DATA_LOAD_COMPLETE);

                if (msg.what == 1) {
                    subject = (Subject) msg.obj;
                    curPage = subject.getCurrentPageNo();

                    //显示评论数
                    if (subject.getTotalPageNo() > 1) {
                        mHeadTitle.setText("帖子正文" + subject.getCurrentPageNo() + "/" + subject.getTotalPageNo());
                    }


                    lvPostData.clear();
                    lvPostData.addAll(subject.getReplylist());
                    lvPost.setSelection(0);


                    //发送通知广播
                    if (subject.getResult() != null) {
                        UIHelper.sendBroadCast(SubjectDetail.this, subject.getResult());
                    }

                    if (subject.getCurrentPageNo() == subject.getTotalPageNo()) {
                        lvPost.setTag(UIHelper.LISTVIEW_DATA_FULL);
                        lvCommentAdapter.notifyDataSetChanged();
                        lvPost_foot_more.setText(R.string.load_full);
                    } else {
                        lvPost.setTag(UIHelper.LISTVIEW_DATA_MORE);
                        lvCommentAdapter.notifyDataSetChanged();
                        lvPost_foot_more.setText(R.string.load_more);
                    }

                    lvPost_foot_progress.setVisibility(ProgressBar.GONE);

                    if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH)
                        lvPost.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());

                } else if (msg.what == 0) {
                    headButtonSwitch(DATA_LOAD_FAIL);
                    UIHelper.ToastMessage(SubjectDetail.this, R.string.msg_load_is_null);
                } else if (msg.what == -1 && msg.obj != null) {
                    headButtonSwitch(DATA_LOAD_FAIL);

                    ((AppException) msg.obj).makeToast(SubjectDetail.this);
                }
            }
        };

        loadSubject(boardEngName, subjectID, 1, UIHelper.LISTVIEW_ACTION_INIT);

    }

    private void loadSubject(final String boardEngName, final String subjectID, final int pageIndex, final int action) {
        headButtonSwitch(DATA_LOAD_ING);

        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    subject = ((AppContext) getApplication()).getSubject(boardEngName, subjectID, pageIndex, isRefresh);
                    msg.what = (subject != null && subject.getSubjectID() != null) ? 1 : 0;
                    msg.obj = (subject != null) ? subject : null;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;
                subjectHandler.sendMessage(msg);
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
            loadSubject(boardEngName, subjectID, 1000, UIHelper.LISTVIEW_ACTION_REFRESH);
        }
    };

    private View.OnClickListener preClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            loadSubject(boardEngName, subjectID, curPage - 1, UIHelper.LISTVIEW_ACTION_REFRESH);
        }
    };

    private View.OnClickListener nextClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            loadSubject(boardEngName, subjectID, curPage + 1, UIHelper.LISTVIEW_ACTION_REFRESH);
        }
    };


    private View.OnClickListener homeClickListener = new View.OnClickListener() {
        public void onClick(View v) {
//            if (subject == null) {
//                UIHelper.ToastMessage(v.getContext(), R.string.msg_read_detail_fail);
//                return;
//            }
//            //分享到
//            UIHelper.showShareDialog(SubjectDetail.this, subject.getTitle(), subject.getUrl());
            loadSubject(boardEngName, subjectID, 1, UIHelper.LISTVIEW_ACTION_REFRESH);
        }
    };


    //初始化视图控件
    private void initCommentView() {
        lvPost_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
        lvPost_foot_more = (TextView) lvPost_footer.findViewById(R.id.listview_foot_more);
        lvPost_foot_progress = (ProgressBar) lvPost_footer.findViewById(R.id.listview_foot_progress);

        lvCommentAdapter = new ListViewCommentAdapter(this, lvPostData, R.layout.comment_listitem);
        lvPost = (PullToRefreshListView) findViewById(R.id.subject_list_listview);
        lvPost.addFooterView(lvPost_footer);//添加底部视图  必须在setAdapter前
        lvPost.setAdapter(lvCommentAdapter);
        lvPost.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击头部、底部栏无效
                if (position == 0 || view == lvPost_footer) return;

                Post com = null;
                //判断是否是TextView
                if (view instanceof TextView) {
                    com = (Post) view.getTag();
                }
                if (com == null) return;

                //跳转--回复评论界面
//                UIHelper.showCommentReply(PostDetail.this, curBoard, curId, com.getSubjectID(), com.getAuthor(), com.getAuthor(), com.getContent());
            }
        });
        lvPost.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                lvPost.onScrollStateChanged(view, scrollState);

                //数据为空--不用继续下面代码了
                if (lvPostData.size() == 0) return;

                //判断是否滚动到底部
                boolean scrollEnd = false;
                try {
                    if (view.getPositionForView(lvPost_footer) == view.getLastVisiblePosition())
                        scrollEnd = true;
                } catch (Exception e) {
                    scrollEnd = false;
                }

                int lvDataState = StringUtility.toInt(lvPost.getTag());
                if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
                    lvPost.setTag(UIHelper.LISTVIEW_DATA_LOADING);
                    lvPost_foot_more.setText(R.string.pull_to_refresh_pull_label);
                    lvPost_foot_progress.setVisibility(View.VISIBLE);
                    //当前pageIndex
                    loadSubject(boardEngName, subjectID, curPage + 1, UIHelper.LISTVIEW_ACTION_SCROLL);
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lvPost.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });
        lvPost.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //点击头部、底部栏无效
                if (position == 0 || view == lvPost_footer) return false;

                Post _com = null;
                //判断是否是TextView
                if (view instanceof TextView) {
                    _com = (Post) view.getTag();
                }
                if (_com == null) return false;

                final Post com = _com;

                curLvPosition = lvPostData.indexOf(com);

                final AppContext ac = (AppContext) getApplication();
                //操作--回复 & 删除
                String uid = ac.getLoginUserID();
                //判断该评论是否是当前登录用户发表的：true--有删除操作  false--没有删除操作
                if (uid.equals(com.getAuthor())) {
                    final Handler handler = new Handler() {
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                Result res = (Result) msg.obj;
                                if (res.isOk()) {
                                    lvPostData.remove(com);
                                    lvCommentAdapter.notifyDataSetChanged();
                                }
                                UIHelper.ToastMessage(SubjectDetail.this, res.getErrorMessage());
                            } else {
                                ((AppException) msg.obj).makeToast(SubjectDetail.this);
                            }
                        }
                    };
                    final Thread thread = new Thread() {
                        public void run() {
                            Message msg = new Message();
                            try {
//                                Result res = ac.delComment(curBoard, curId, com.getSubjectID(), com.getAuthor());
                                msg.what = 1;
//                                msg.obj = res;
                            } catch (Exception e) {
                                e.printStackTrace();
                                msg.what = -1;
                                msg.obj = e;
                            }
                            handler.sendMessage(msg);
                        }
                    };
//                    UIHelper.showCommentOptionDialog(PostDetail.this, curBoard, curId, com, thread);
                } else {
//                    UIHelper.showCommentOptionDialog(PostDetail.this, curBoard, curId, com, null);
                }
                return true;
            }
        });
        lvPost.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                loadSubject(boardEngName, subjectID, 1000, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (data == null) return;
        loadSubject(boardEngName, subjectID, 1000, UIHelper.LISTVIEW_ACTION_SCROLL);
    }


    private View.OnClickListener commentpubClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            if (subjectID == null) {
                return;
            }


            _content = mFootEditer.getText().toString();
            if (StringUtility.isEmpty(_content)) {
                UIHelper.ToastMessage(v.getContext(), "请输入回帖内容");
                return;
            }

            final AppContext ac = (AppContext) getApplication();
            if (!ac.isLogin()) {
                UIHelper.showLoginDialog(SubjectDetail.this);
                return;
            }

            mProgress = ProgressDialog.show(v.getContext(), null, "发表中···", true, true);

            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {

                    if (mProgress != null) mProgress.dismiss();

                    if (msg.what == 1) {
                        Result res = (Result) msg.obj;
                        UIHelper.ToastMessage(SubjectDetail.this, res.getErrorMessage());
                        if (res.isOk()) {
                            //发送通知广播
                            UIHelper.sendBroadCast(SubjectDetail.this, res);
                            //恢复初始底部栏
                            mFootViewSwitcher.setDisplayedChild(0);
                            mFootEditer.clearFocus();
                            mFootEditer.setText("");
                            mFootEditer.setVisibility(View.GONE);
                            //跳到评论列表
//                            viewSwitch(VIEWSWITCH_TYPE_COMMENTS);
                            //跳转到最后一页
                            loadSubject(boardEngName, subjectID, 1000, UIHelper.LISTVIEW_ACTION_SCROLL);
                            //清除之前保存的编辑内容
                            ac.removeProperty(tempCommentKey);
                        }
                    } else {
                        ((AppException) msg.obj).makeToast(SubjectDetail.this);
                    }
                }
            };
            new Thread() {
                public void run() {
                    Message msg = new Message();
                    try {
                        Result res = ac.pubComment(boardEngName, subjectID, subject.getTitle(), _content);
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