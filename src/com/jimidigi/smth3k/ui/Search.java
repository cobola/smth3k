package com.jimidigi.smth3k.ui;

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
import com.jimidigi.smth3k.adapter.ListViewSubjectAdapter;
import com.jimidigi.smth3k.bean.Subject;
import com.jimidigi.smth3k.bean.SubjectList;
import com.jimidigi.smth3k.common.StringUtility;
import com.jimidigi.smth3k.common.UIHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Search extends BaseActivity {
    private Button mSearchBtn;
    private EditText mSearchEditer;
    private ProgressBar mProgressbar;

    private Button search_catalog_board;
    private Button search_catalog_post;
    private Button search_catalog_user;

    private ListView mlvSearch;
    private ListViewSubjectAdapter lvSubjectAdapter;
    private List<Subject> lvSearchData = new ArrayList<Subject>();
    private View lvSearch_footer;
    private TextView lvSearch_foot_more;
    private ProgressBar lvSearch_foot_progress;
    private Handler mSearchHandler;
    private int lvSumData;

    private int curLvDataState;
    private int curSearchCatalog;
    private String curSearchContent = "";


    private InputMethodManager imm;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        this.initView();

        this.initData();
    }

    /**
     * 头部按钮展示
     *
     * @param type
     */
    private void headButtonSwitch(int type) {
        switch (type) {
            case DATA_LOAD_ING:
                mSearchBtn.setClickable(false);
                mProgressbar.setVisibility(View.VISIBLE);
                break;
            case DATA_LOAD_COMPLETE:
                mSearchBtn.setClickable(true);
                mProgressbar.setVisibility(View.GONE);
                break;
        }
    }

    //初始化视图控件
    private void initView() {
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mSearchBtn = (Button) findViewById(R.id.search_btn);
        mSearchEditer = (EditText) findViewById(R.id.search_editer);
        mProgressbar = (ProgressBar) findViewById(R.id.search_progress);

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mSearchEditer.clearFocus();
                curSearchContent = mSearchEditer.getText().toString();
                loadLvSearchData(curSearchCatalog, mSearchHandler, UIHelper.LISTVIEW_ACTION_INIT);
            }
        });
        mSearchEditer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imm.showSoftInput(v, 0);
                } else {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        mSearchEditer.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH) {
                    if (v.getTag() == null) {
                        v.setTag(1);
                        mSearchEditer.clearFocus();
                        curSearchContent = mSearchEditer.getText().toString();
                        loadLvSearchData(curSearchCatalog, mSearchHandler, UIHelper.LISTVIEW_ACTION_INIT);
                    } else {
                        v.setTag(null);
                    }
                    return true;
                }
                return false;
            }
        });

        search_catalog_board = (Button) findViewById(R.id.search_catalog_board);
        search_catalog_post = (Button) findViewById(R.id.search_catalog_post);
        search_catalog_user = (Button) findViewById(R.id.search_catalog_user);

        search_catalog_board.setOnClickListener(this.searchBtnClick(search_catalog_board, UIHelper.LISTVIEW_DATATYPE_SUBJECT_BOARD));
        search_catalog_post.setOnClickListener(this.searchBtnClick(search_catalog_post, UIHelper.LISTVIEW_DATATYPE_BOARD));
        search_catalog_user.setOnClickListener(this.searchBtnClick(search_catalog_user, UIHelper.LISTVIEW_DATATYPE_USER));


        search_catalog_board.setEnabled(false);

        lvSearch_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
        lvSearch_foot_more = (TextView) lvSearch_footer.findViewById(R.id.listview_foot_more);
        lvSearch_foot_progress = (ProgressBar) lvSearch_footer.findViewById(R.id.listview_foot_progress);

        lvSubjectAdapter = new ListViewSubjectAdapter(this, lvSearchData, R.layout.search_listitem);
        mlvSearch = (ListView) findViewById(R.id.search_listview);
        mlvSearch.setVisibility(ListView.GONE);
        mlvSearch.addFooterView(lvSearch_footer);//添加底部视图  必须在setAdapter前
        mlvSearch.setAdapter(lvSubjectAdapter);
        mlvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击底部栏无效
                if (view == lvSearch_footer) return;

                Subject res = null;
                //判断是否是TextView
                if (view instanceof TextView) {
                    res = (Subject) view.getTag();
                } else {
                    TextView title = (TextView) view.findViewById(R.id.search_listitem_title);
                    res = (Subject) title.getTag();
                }
                if (res == null) return;

                //跳转
                UIHelper.showUrlRedirect(view.getContext(), res.getUrl());
            }
        });
        mlvSearch.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //数据为空--不用继续下面代码了
                if (lvSearchData.size() == 0) return;

                //判断是否滚动到底部
                boolean scrollEnd = false;
                try {
                    if (view.getPositionForView(lvSearch_footer) == view.getLastVisiblePosition())
                        scrollEnd = true;
                } catch (Exception e) {
                    scrollEnd = false;
                }

                if (scrollEnd && curLvDataState == UIHelper.LISTVIEW_DATA_MORE) {
                    mlvSearch.setTag(UIHelper.LISTVIEW_DATA_LOADING);
                    lvSearch_foot_more.setText(R.string.load_ing);
                    lvSearch_foot_progress.setVisibility(View.VISIBLE);
                    //当前pageIndex
                    loadLvSearchData(curSearchCatalog, mSearchHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    //初始化控件数据
    private void initData() {
        mSearchHandler = new Handler() {
            public void handleMessage(Message msg) {

                headButtonSwitch(DATA_LOAD_COMPLETE);

                if (msg.what >= 0) {
                    SubjectList list = (SubjectList) msg.obj;
                    //处理listview数据


                    lvSearchData.clear();//先清除原有数据
                    lvSearchData.addAll(list.getSubjectList());


                    if (msg.what < 20) {
                        curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
                        lvSubjectAdapter.notifyDataSetChanged();
                        lvSearch_foot_more.setText(R.string.load_full);
                    } else if (msg.what == 20) {
                        curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
                        lvSubjectAdapter.notifyDataSetChanged();
                        lvSearch_foot_more.setText(R.string.load_more);
                    }

                } else if (msg.what == -1) {
                    //有异常--显示加载出错 & 弹出错误消息
                    curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
                    lvSearch_foot_more.setText(R.string.load_error);
                    ((AppException) msg.obj).makeToast(Search.this);
                }
                if (lvSearchData.size() == 0) {
                    curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
                    lvSearch_foot_more.setText(R.string.load_empty);
                }
                lvSearch_foot_progress.setVisibility(View.GONE);
                if (msg.arg1 != UIHelper.LISTVIEW_ACTION_SCROLL) {

                    if (!mlvSearch.isStackFromBottom()) {
                        mlvSearch.setStackFromBottom(true);
                    }
                    mlvSearch.setStackFromBottom(false);
                }
            }
        };
    }

    /**
     * 线程加载收藏数据
     *
     * @param handler 处理器
     * @param action  动作标识
     */
    private void loadLvSearchData(final int catalog, final Handler handler, final int action) {
        if (StringUtility.isEmpty(curSearchContent)) {
            UIHelper.ToastMessage(Search.this, "请输入搜索内容");
            return;
        }

        headButtonSwitch(DATA_LOAD_ING);
        mlvSearch.setVisibility(ListView.VISIBLE);

        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    SubjectList searchList = ((AppContext) getApplication()).getSearchResult(catalog, curSearchContent);
                    msg.what = searchList.getPageCount();
                    msg.obj = searchList;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;//告知handler当前action
                if (curSearchCatalog == catalog)
                    handler.sendMessage(msg);
            }
        }.start();
    }

    private View.OnClickListener searchBtnClick(final Button btn, final int catalog) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                if (btn == search_catalog_board)
                    search_catalog_board.setEnabled(false);
                else
                    search_catalog_board.setEnabled(true);

                if (btn == search_catalog_post)
                    search_catalog_post.setEnabled(false);
                else
                    search_catalog_post.setEnabled(true);
                if (btn == search_catalog_user)
                    search_catalog_user.setEnabled(false);
                else
                    search_catalog_user.setEnabled(true);

                //开始搜索
                mSearchEditer.clearFocus();
                curSearchContent = mSearchEditer.getText().toString();
                curSearchCatalog = catalog;
                loadLvSearchData(catalog, mSearchHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
            }
        };
    }
}
