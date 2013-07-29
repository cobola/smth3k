package com.jimidigi.smth3k.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.jimidigi.smth3k.AppConfig;
import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.bean.Post;
import com.jimidigi.smth3k.bean.Result;
import com.jimidigi.smth3k.common.UIHelper;
import com.jimidigi.smth3k.common.StringUtility;

/**
 * 发表帖子
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class PostPub extends BaseActivity{

	private ImageView mBack;
	private EditText mTitle;
	private EditText mContent;
	private Spinner mCatalog;
	private CheckBox mEmail;
	private Button mPublish;
    private ProgressDialog mProgress;
	private Post post;
	private InputMethodManager imm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_pub);
		
		this.initView();
		
	}
	
    //初始化视图控件
    private void initView()
    {    	
    	imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	
    	mBack = (ImageView)findViewById(R.id.question_pub_back);
    	mPublish = (Button)findViewById(R.id.post_pub_publish);
    	mTitle = (EditText)findViewById(R.id.question_pub_title);
    	mContent = (EditText)findViewById(R.id.question_pub_content);
    	mEmail = (CheckBox)findViewById(R.id.post_pub_email);
    	mCatalog = (Spinner)findViewById(R.id.post_pub_catalog);
    	
    	mBack.setOnClickListener(UIHelper.finish(this));
    	mPublish.setOnClickListener(publishClickListener);
    	mCatalog.setOnItemSelectedListener(catalogSelectedListener);
    	//编辑器添加文本监听
    	mTitle.addTextChangedListener(UIHelper.getTextWatcher(this, AppConfig.TEMP_POST_TITLE));
    	mContent.addTextChangedListener(UIHelper.getTextWatcher(this, AppConfig.TEMP_POST_CONTENT));
    	
    	//显示临时编辑内容
    	UIHelper.showTempEditContent(this, mTitle, AppConfig.TEMP_POST_TITLE);
    	UIHelper.showTempEditContent(this, mContent, AppConfig.TEMP_POST_CONTENT);
    	//显示临时选择分类
    	String position = ((AppContext)getApplication()).getProperty(AppConfig.TEMP_POST_CATALOG);
    	mCatalog.setSelection(StringUtility.toInt(position, 0));
    }
	
    private AdapterView.OnItemSelectedListener catalogSelectedListener = new AdapterView.OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			//保存临时选择的分类
			((AppContext)getApplication()).setProperty(AppConfig.TEMP_POST_CATALOG, position+"");
		}
		public void onNothingSelected(AdapterView<?> parent) {}
    };
    
	private View.OnClickListener publishClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			//隐藏软键盘
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
			
			String title = mTitle.getText().toString();
			if(StringUtility.isEmpty(title)){
				UIHelper.ToastMessage(v.getContext(), "请输入标题");
				return;
			}
			String content = mContent.getText().toString();
			if(StringUtility.isEmpty(content)){
				UIHelper.ToastMessage(v.getContext(), "请输入提问内容");
				return;
			}
			
			final AppContext ac = (AppContext)getApplication();
			if(!ac.isLogin()){
				UIHelper.showLoginDialog(PostPub.this);
				return;
			}
			
			mProgress = ProgressDialog.show(v.getContext(), null, "发布中···",true,true); 
			
			post = new Post();
			post.setAuthor(ac.getLoginUserID());
			post.setTitle(title);
			post.setContent(content);
//			if(mEmail.isChecked())
//				post.setIsNoticeMe(1);
			
			final Handler handler = new Handler(){
				public void handleMessage(Message msg) {
					if(mProgress!=null)mProgress.dismiss();
					if(msg.what == 1){
						Result res = (Result)msg.obj;
						UIHelper.ToastMessage(PostPub.this, res.getErrorMessage());
						if(res.isOk()){
							//发送通知广播
								UIHelper.sendBroadCast(PostPub.this, res);
							//清除之前保存的编辑内容
							ac.removeProperty(AppConfig.TEMP_POST_TITLE,AppConfig.TEMP_POST_CATALOG,AppConfig.TEMP_POST_CONTENT);
							//跳转到文章详情
							finish();
						}
					}
					else {
						((AppException)msg.obj).makeToast(PostPub.this);
					}
				}
			};
			new Thread(){
				public void run() {
					Message msg = new Message();					
					try {
						Result res = ac.pubPost(post);
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
