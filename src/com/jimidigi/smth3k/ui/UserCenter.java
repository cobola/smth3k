package com.jimidigi.smth3k.ui;

import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.bean.Result;
import com.jimidigi.smth3k.bean.User;
import com.jimidigi.smth3k.common.UIHelper;
import com.jimidigi.smth3k.widget.PullToRefreshListView;
import com.jimidigi.smth3k.widget.UserInfoDialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * 用户专页
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UserCenter extends BaseActivity{
	
	private ImageView mBack;
	private ImageView mRefresh;
	private TextView mHeadTitle;
	private ProgressBar mProgressbar;
	private RadioButton mRelation;
	private RadioButton mMessage;
	private RadioButton mAtme;
	private UserInfoDialog mUserinfoDialog;
	private Button mTabActive;
	private Button mTabBlog;
	
	private ImageView mUserface;
	private TextView mUsername;
	private TextView mFrom;
	private TextView mGender;
	private TextView mJointime;
	private TextView mDevplatform;
	private TextView mExpertise;
	private TextView mLatestonline;
	
	private PullToRefreshListView mLvActive;
	private View lvActive_footer;
	private TextView lvActive_foot_more;
	private ProgressBar lvActive_foot_progress;
    private Handler mActiveHandler;
	private int lvActiveSumData;
	
	private PullToRefreshListView mLvBlog;
	private View lvBlog_footer;
	private TextView lvBlog_foot_more;
	private ProgressBar lvBlog_foot_progress;
    private Handler mBlogHandler;
	private int lvBlogSumData;
    
    private User mUser;
    private Handler mUserHandler;
	private int relationAction;	
	private int curLvActiveDataState;
	private int curLvBlogDataState;	
	
	private String _uid;
	private String _hisuid;
	private String _hisname;
	private String _username;
	private int _pageSize = 20;
	
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center);
		
		this.initView();
		
		this.initData();
	}
	
    //初始化视图控件
    private void initView()
    {
    	_hisuid = getIntent().getStringExtra("his_id");
    	_hisname = getIntent().getStringExtra("his_name");
    	_username = getIntent().getStringExtra("his_name");
    	_uid = ((AppContext)getApplication()).getLoginUserID();
    	
    	mBack = (ImageView)findViewById(R.id.user_center_back);
    	mRefresh = (ImageView)findViewById(R.id.user_center_refresh);
    	mHeadTitle = (TextView)findViewById(R.id.user_center_head_title);
    	mProgressbar = (ProgressBar)findViewById(R.id.user_center_head_progress);
    	mRelation = (RadioButton)findViewById(R.id.user_center_footbar_relation);
    	mMessage = (RadioButton)findViewById(R.id.user_center_footbar_message);
    	mAtme = (RadioButton)findViewById(R.id.user_center_footbar_atme);
    	
    	mTabActive = (Button)findViewById(R.id.user_center_btn_active);
    	mTabBlog = (Button)findViewById(R.id.user_center_btn_blog);
    	
    	mUserinfoDialog = new UserInfoDialog(UserCenter.this);
    	mUserface = (ImageView)mUserinfoDialog.findViewById(R.id.user_center_userface);
    	mUsername = (TextView)mUserinfoDialog.findViewById(R.id.user_center_username);
    	mFrom = (TextView)mUserinfoDialog.findViewById(R.id.user_center_from);
    	mGender = (TextView)mUserinfoDialog.findViewById(R.id.user_center_gender);
    	mJointime = (TextView)mUserinfoDialog.findViewById(R.id.user_center_jointime);
    	mDevplatform = (TextView)mUserinfoDialog.findViewById(R.id.user_center_devplatform);
    	mExpertise = (TextView)mUserinfoDialog.findViewById(R.id.user_center_expertise);
    	mLatestonline = (TextView)mUserinfoDialog.findViewById(R.id.user_center_latestonline);
    	
    	mHeadTitle.setText(_username + " ▼");
    	//设置第一选中项
    	mTabActive.setEnabled(false);
    	mTabActive.setOnClickListener(tabBtnClick(mTabActive));
    	mTabBlog.setOnClickListener(tabBtnClick(mTabBlog));
    	
    	mBack.setOnClickListener(UIHelper.finish(this));
    	mRefresh.setOnClickListener(refreshClickListener);
    	mHeadTitle.setOnClickListener(headTitleClickListener);
    	mMessage.setOnClickListener(messageClickListener);
    	mAtme.setOnClickListener(atmeClickListener);
    	mUserinfoDialog.setOnCancelListener(dialogCancelListener);
    	
//    	this.initLvActive();
//    	this.initLvBlog();
    }    
    
    //初始化动态列表控件

    //初始化控件数据
	private void initData()
	{    	
    	mActiveHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				headButtonSwitch(DATA_LOAD_COMPLETE);
//				lvActiveHandleMessage(msg);
			}
		};
		
    	mBlogHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				headButtonSwitch(DATA_LOAD_COMPLETE);
//				lvBlogHandleMessage(msg);
			}
		};
		
		mUserHandler = new Handler(){
			public void handleMessage(Message msg) {
				headButtonSwitch(DATA_LOAD_COMPLETE);
				if(mUser != null){
					_username = mUser.getNickName();
					mHeadTitle.setText(_username + " ▼");
					mUsername.setText(mUser.getNickName());
//					mFrom.setText(mUser.getLocation());
//					mGender.setText(mUser.getGender());
//					mJointime.setText(StringUtility.friendly_time(mUser.getJointime()));
//					mDevplatform.setText(mUser.getDevplatform());
//					mExpertise.setText(mUser.getExpertise());
//					mLatestonline.setText(StringUtility.friendly_time(mUser.getLatestonline()));
					
					//初始化用户关系 & 点击事件
//					loadUserRelation(mUser.getRelation());
					
					//加载用户头像
//					UIHelper.showUserFace(mUserface, mUser.getFace());
				}
//				lvActiveHandleMessage(msg);
			}
		};
		
		this.loadLvActiveData(mUserHandler, 0 ,UIHelper.LISTVIEW_ACTION_INIT);
//		this.loadLvBlogData(mBlogHandler, 0, UIHelper.LISTVIEW_ACTION_INIT);
	}

	//加载动态列表
	private void loadLvActiveData(final Handler handler, final int pageIndex, final int action){  
		headButtonSwitch(DATA_LOAD_ING);
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
//					UserInformation uinfo = ((AppContext)getApplication()).getInformation(_uid, _hisuid, _hisname, pageIndex, isRefresh);
//					mUser = uinfo.getUser();
//					msg.what = uinfo.getPageSize();
//					msg.obj = uinfo;
	            } catch (Exception e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
                handler.sendMessage(msg);
			}
		}.start();
	}
	

    /**
     * 头部按钮展示
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
	
	private View.OnClickListener tabBtnClick(final Button btn){
    	return new View.OnClickListener() {
			public void onClick(View v) {
		    	if(btn == mTabActive){
		    		mTabActive.setEnabled(false);
		    	}else{
		    		mTabActive.setEnabled(true);
		    	}
		    	if(btn == mTabBlog){
		    		mTabBlog.setEnabled(false);
		    	}else{
		    		mTabBlog.setEnabled(true);
		    	}	    	
				
				if(btn == mTabActive){
					mLvActive.setVisibility(View.VISIBLE);
					mLvBlog.setVisibility(View.GONE);
					

				}else{
					mLvActive.setVisibility(View.GONE);
					mLvBlog.setVisibility(View.VISIBLE);
					
//					if(lvBlogData.size() == 0)
//						loadLvBlogData(mBlogHandler, 0, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
				}
			}
		};
    }
	
	private View.OnClickListener refreshClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			loadLvActiveData(mUserHandler, 0 ,UIHelper.LISTVIEW_ACTION_REFRESH);
//			loadLvBlogData(mBlogHandler, 0, UIHelper.LISTVIEW_ACTION_REFRESH);
		}
	};
	private View.OnClickListener headTitleClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if(mUserinfoDialog != null && mUserinfoDialog.isShowing()){
				mHeadTitle.setText(_username + " ▼");
				mUserinfoDialog.hide();
			}else{
				mHeadTitle.setText(_username + " ▲");				
				mUserinfoDialog.show();
			}
		}
	};
	private DialogInterface.OnCancelListener dialogCancelListener = new DialogInterface.OnCancelListener(){
		public void onCancel(DialogInterface dialog) {
			mHeadTitle.setText(_username + " ▼");
		}		
	};	
	private View.OnClickListener messageClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			if(mUser == null)	return;
//			UIHelper.showMessagePub(UserCenter.this, mUser.getUid(), mUser.getName());
		}
	};
	private View.OnClickListener atmeClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			if(mUser == null)	return;
//			UIHelper.showTweetPub(UserCenter.this, "@"+mUser.getName()+" ", mUser.getUid());
		}
	};
	private View.OnClickListener relationClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			if(mUser == null)	return;
			//判断登录
			final AppContext ac = (AppContext)getApplication();
			if(!ac.isLogin()){
				UIHelper.showLoginDialog(UserCenter.this);
				return;
			}
			
			final Handler handler = new Handler(){
				public void handleMessage(Message msg) {
					if(msg.what == 1){
						Result res = (Result)msg.obj;
						if(res.isOk()){

						}
						UIHelper.ToastMessage(UserCenter.this, res.getErrorMessage());
					}else{
						((AppException)msg.obj).makeToast(UserCenter.this);
					}
				}
			};
			final Thread thread = new Thread(){
				public void run() {
					Message msg = new Message();
					try {
//						Result res = ac.updateRelation(_uid, _hisuid, relationAction);
						msg.what = 1;
//						msg.obj = res;
		            } catch (Exception e) {
		            	e.printStackTrace();
		            	msg.what = -1;
		            	msg.obj = e;
		            }
	                handler.sendMessage(msg);
				}
			};
			String dialogTitle = "";

			new AlertDialog.Builder(v.getContext())
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(dialogTitle)
			.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					thread.start();
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
	};
}
