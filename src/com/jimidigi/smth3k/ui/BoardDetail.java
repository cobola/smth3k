package com.jimidigi.smth3k.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.jimidigi.smth3k.AppConfig;
import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.adapter.ListViewSubjectAdapter;
import com.jimidigi.smth3k.bean.Result;
import com.jimidigi.smth3k.bean.Subject;
import com.jimidigi.smth3k.bean.SubjectList;
import com.jimidigi.smth3k.common.StringUtility;
import com.jimidigi.smth3k.common.UIHelper;
import com.jimidigi.smth3k.widget.PullToRefreshListView;

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
public class BoardDetail extends BaseActivity {

    private FrameLayout mHeader;
    private LinearLayout mFooter;
    private ImageView mBack;
    private ImageView mFavorite;
    private ImageView mRefresh;
    private TextView mHeadTitle;
    private ProgressBar mProgressbar;

    private ImageView mPre;
    private ImageView mNext;

    private List<Subject> lvSubjectData = new ArrayList<Subject>();


    private String boardEngName;
    private int boardType;
    private int pages;

    private final static int VIEWSWITCH_TYPE_DETAIL = 0x001;
    private final static int VIEWSWITCH_TYPE_COMMENTS = 0x002;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;
    private final static int DATA_LOAD_FAIL = 0x003;

    private InputMethodManager imm;


    private ListViewSubjectAdapter lvSubjectAdapter;
    private PullToRefreshListView lvSubject;
    private Handler lvSubjectHandler;
    private View lvSubject_footer;
    private TextView lvSubject_foot_more;
    private ProgressBar lvSubject_foot_progress;


    private int curPage;
    private int curLvDataState;
    private int curLvPosition;//当前listview选中的item位置


    private String tempCommentKey = AppConfig.TEMP_COMMENT;


    private Button framebtn_Board_class;
    private Button framebtn_Board_subject;
    private Button framebtn_Board_g;
    private Button framebtn_Board_m;
    private GestureDetector gd;
    private boolean isFullScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_detail);

        this.initView();
        this.initData();

        //注册双击全屏事件
//        this.regOnDoubleEvent();
    }

    //初始化视图控件
    private void initView() {
        boardEngName = getIntent().getStringExtra("boardEngName");
        boardType = getIntent().getIntExtra("boardType", UIHelper.LISTVIEW_BOARD_TYPE_SUBJECT);
        pages = getIntent().getIntExtra("pages", 1);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        curPage = 1;
        if (boardEngName != null)
            tempCommentKey = AppConfig.TEMP_BOARD + "_" + boardType + "_" + boardEngName + "_" + pages;

        mHeader = (FrameLayout) findViewById(R.id.board_detail_header);
        mFooter = (LinearLayout) findViewById(R.id.board_detail_footer);
        mBack = (ImageView) findViewById(R.id.board_detail_back);
        mRefresh = (ImageView) findViewById(R.id.board_detail_refresh);
        mHeadTitle = (TextView) findViewById(R.id.board_detail_head_title);
        mProgressbar = (ProgressBar) findViewById(R.id.board_detail_head_progress);


        framebtn_Board_class = (Button) findViewById(R.id.frame_board_btn_class);
        framebtn_Board_subject = (Button) findViewById(R.id.frame_board_btn_subject);
        framebtn_Board_g = (Button) findViewById(R.id.frame_board_btn_g);
        framebtn_Board_m = (Button) findViewById(R.id.frame_board_btn_m);

        framebtn_Board_class.setOnClickListener(frameBoardBtnClick(framebtn_Board_class, boardEngName, UIHelper.LISTVIEW_BOARD_TYPE_CLASSIC));

        framebtn_Board_subject.setOnClickListener(frameBoardBtnClick(framebtn_Board_subject, boardEngName, UIHelper.LISTVIEW_BOARD_TYPE_SUBJECT));

        framebtn_Board_subject.setEnabled(false);
        framebtn_Board_g.setOnClickListener(frameBoardBtnClick(framebtn_Board_g, boardEngName, UIHelper.LISTVIEW_BOARD_TYPE_G));

        framebtn_Board_m.setOnClickListener(frameBoardBtnClick(framebtn_Board_m, boardEngName, UIHelper.LISTVIEW_BOARD_TYPE_M));


        lvSubjectAdapter = new ListViewSubjectAdapter(this, lvSubjectData, R.layout.subject_listitem);
        lvSubject_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
        lvSubject_foot_more = (TextView) lvSubject_footer.findViewById(R.id.listview_foot_more);
        lvSubject_foot_progress = (ProgressBar) lvSubject_footer.findViewById(R.id.listview_foot_progress);

        lvSubject = (PullToRefreshListView) findViewById(R.id.frame_listview_subject_board);
        lvSubject.addFooterView(lvSubject_footer);//添加底部视图  必须在setAdapter前
        lvSubject.setAdapter(lvSubjectAdapter);
        lvSubject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击头部、底部栏无效
                if (position == 0 || view == lvSubject_footer) return;

                Subject subject = null;
                //判断是否是TextView
                if (view instanceof TextView) {
                    subject = (Subject) view.getTag();
                } else {
                    TextView tv = (TextView) view.findViewById(R.id.subject_listitem_title);
                    subject = (Subject) tv.getTag();
                }
                if (subject == null) return;


                if (subject.isAsPost()) {
                    UIHelper.showPostDetail(view.getContext(), subject.getUrl());
                } else {

                    UIHelper.showSubjectDetail(view.getContext(), subject.getBoardEngName(), subject.getSubjectID());

                }
            }
        });

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
                    loadLvSubjectListByBoard(lvSubjectHandler, boardEngName, boardType, curPage + 1, UIHelper.LISTVIEW_ACTION_REFRESH);
                }
            }

            ;

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lvSubject.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });
        lvSubject.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                loadLvSubjectListByBoard(lvSubjectHandler, boardEngName, UIHelper.LISTVIEW_BOARD_TYPE_SUBJECT, curPage - 1, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });


        mPre = (ImageView) findViewById(R.id.board_detail_footbar_pre);
        mNext = (ImageView) findViewById(R.id.board_detail_footbar_next);


        mBack.setOnClickListener(UIHelper.finish(this));
        mPre.setOnClickListener(preClickListener);
        mNext.setOnClickListener(nextClickListener);
        mRefresh.setOnClickListener(refreshClickListener);

    }

    //初始化控件数据
    private void initData() {


        lvSubjectHandler = new Handler() {
            public void handleMessage(Message msg) {

                headButtonSwitch(DATA_LOAD_COMPLETE);
                if (msg.what >= 0) {
                    SubjectList list = (SubjectList) msg.obj;
                    curPage = list.getPageNow();
                    boardType = list.getBoardType();
                    mHeadTitle.setText(list.getBoardChsName() + curPage + "/" + list.getPageCount());
                    Result notice = list.getResult();
                    //处理listview数据
                    lvSubjectData.clear();
                    lvSubjectData.addAll(list.getSubjectList());


                    if (list.getPageNow() == list.getPageCount()) {
                        curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
                        lvSubjectAdapter.notifyDataSetChanged();
                        lvSubject_foot_more.setText(R.string.load_full);
                    } else {
                        curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
                        lvSubjectAdapter.notifyDataSetChanged();
                        lvSubject_foot_more.setText(R.string.load_more);
                    }


                    lvSubject.setSelection(0);

                    //发送通知广播
                    if (notice != null) {
                        UIHelper.sendBroadCast(BoardDetail.this, notice);
                    }
                } else if (msg.what == -1) {
                    //有异常--也显示更多 & 弹出错误消息
                    curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
                    lvSubject_foot_more.setText(R.string.load_more);
                    ((AppException) msg.obj).makeToast(BoardDetail.this);
                }
                if (lvSubjectData.size() == 0) {
                    curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
                    lvSubject_foot_more.setText(R.string.load_empty);
                }

                lvSubject.setTag(curLvDataState);
                lvSubject_foot_progress.setVisibility(View.GONE);
                if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH)
                    lvSubject.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
            }
        };

        if (lvSubjectData.isEmpty() || lvSubjectData.size() < 1) {
            loadLvSubjectListByBoard(lvSubjectHandler, boardEngName, UIHelper.LISTVIEW_BOARD_TYPE_SUBJECT, 1, UIHelper.LISTVIEW_ACTION_REFRESH);
        }
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

            loadLvSubjectListByBoard(lvSubjectHandler, boardEngName, boardType, 1, UIHelper.LISTVIEW_ACTION_REFRESH);
        }
    };

    private View.OnClickListener preClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            loadLvSubjectListByBoard(lvSubjectHandler, boardEngName, boardType, curPage - 1, UIHelper.LISTVIEW_ACTION_REFRESH);
        }
    };

    private View.OnClickListener nextClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            loadLvSubjectListByBoard(lvSubjectHandler, boardEngName, boardType, curPage + 1, UIHelper.LISTVIEW_ACTION_REFRESH);
        }
    };


    private void loadLvSubjectListByBoard(final Handler handler, final String boardEngName, final int type, final int page, final int action) {
        mProgressbar.setVisibility(ProgressBar.VISIBLE);
        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    SubjectList list = ((AppContext) getApplication()).getSubjectListByBoard(boardEngName, type, page, isRefresh);
                    if (list == null) {
                        return;
                    }
                    curPage = list.getPageNow();
                    msg.what = list.getPageCount();
                    msg.obj = list;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;
                msg.arg2 = UIHelper.LISTVIEW_DATATYPE_SUBJECT_BOARD;
                handler.sendMessage(msg);
            }
        }.start();
    }


    private View.OnClickListener frameBoardBtnClick(final Button btn, final String boardEngName, final int type) {
        return new View.OnClickListener() {
            public void onClick(View v) {

                framebtn_Board_class.setEnabled(true);
                framebtn_Board_subject.setEnabled(true);
                framebtn_Board_g.setEnabled(true);
                framebtn_Board_m.setEnabled(true);

                if (btn.equals(framebtn_Board_class)) {
                    framebtn_Board_class.setEnabled(false);
                }
                if (btn.equals(framebtn_Board_subject)) {
                    framebtn_Board_subject.setEnabled(false);
                }

                if (btn.equals(framebtn_Board_g)) {
                    framebtn_Board_g.setEnabled(false);
                }
                if (btn.equals(framebtn_Board_m)) {
                    framebtn_Board_m.setEnabled(false);
                }

                loadLvSubjectListByBoard(lvSubjectHandler, boardEngName, type, 1, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        };
    }

//    /**
//     * 注册双击全屏事件
//     */
//    private void regOnDoubleEvent() {
//        gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//                isFullScreen = !isFullScreen;
//                if (!isFullScreen) {
//                    WindowManager.LayoutParams params = getWindow().getAttributes();
//                    params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                    getWindow().setAttributes(params);
//                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//                    mHeader.setVisibility(View.VISIBLE);
//                    mFooter.setVisibility(View.VISIBLE);
//                } else {
//                    WindowManager.LayoutParams params = getWindow().getAttributes();
//                    params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//                    getWindow().setAttributes(params);
//                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//                    mHeader.setVisibility(View.GONE);
//                    mFooter.setVisibility(View.GONE);
//                }
//                return true;
//            }
//        });
//    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        gd.onTouchEvent(event);
//        return super.dispatchTouchEvent(event);
//    }
}