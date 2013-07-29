package com.jimidigi.smth3k.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.AppException;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.bean.Result;
import com.jimidigi.smth3k.bean.User;
import com.jimidigi.smth3k.common.*;
import com.jimidigi.smth3k.widget.LoadingDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用户资料
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UserInfo extends BaseActivity {

    private ImageView back;
    private ImageView refresh;
    private ImageView face;
    private ImageView gender;
    private Button editer;
    private TextView nickName;
    private TextView constellation;
    private TextView level;
    private TextView postNumber;
    private TextView loginTimes;
    private TextView aliveness;
    private TextView regDate;
    private TextView lastLogin;
    private TextView lastIp;
    private TextView status;
    private LoadingDialog loading;
    private User user;
    private Handler mHandler;

    private final static int CROP = 200;
    private final static String FILE_SAVEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/OSChina/Portrait/";
    private Uri origUri;
    private Uri cropUri;
    private File protraitFile;
    private Bitmap protraitBitmap;
    private String protraitPath;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info);

        //初始化视图控件
        this.initView();
        //初始化视图数据
        this.initData();
    }

    private void initView() {
        back = (ImageView) findViewById(R.id.user_info_back);
        refresh = (ImageView) findViewById(R.id.user_info_refresh);
//        editer = (Button) findViewById(R.id.user_info_editer);
        back.setOnClickListener(UIHelper.finish(this));
        refresh.setOnClickListener(refreshClickListener);
//        editer.setOnClickListener(editerClickListener);

        face = (ImageView) findViewById(R.id.user_info_userface);
        gender = (ImageView) findViewById(R.id.user_info_gender);
        nickName = (TextView) findViewById(R.id.user_nickname);
        constellation = (TextView) findViewById(R.id.user_constellation);
        level = (TextView) findViewById(R.id.user_level);
        postNumber = (TextView) findViewById(R.id.user_post_number);
        loginTimes = (TextView) findViewById(R.id.user_login_times);
        aliveness = (TextView) findViewById(R.id.user_aliveness);
        regDate = (TextView) findViewById(R.id.user_reg_date);
        lastLogin = (TextView) findViewById(R.id.user_last_login);
        lastIp = (TextView) findViewById(R.id.user_last_ip);
        status = (TextView) findViewById(R.id.user_status);

    }

    private void initData() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (loading != null) loading.dismiss();
                if (msg.what == 1 && msg.obj != null) {
                    user = (User) msg.obj;

                    //加载用户头像
//					UIHelper.showUserFace(face, user.getFace());

                    //用户性别
                    if ("男".equals(user.getGender()))
                        gender.setImageResource(R.drawable.widget_gender_man);
                    else
                        gender.setImageResource(R.drawable.widget_gender_woman);

                    //其他资料
                    nickName.setText(user.getNickName());
                    constellation.setText(user.getConstellation());
                    level.setText(user.getLevel());
                    postNumber.setText(user.getPostNumber() + "");
                    loginTimes.setText(user.getLoginTimes() + "");
                    aliveness.setText(user.getAliveness() + "");
                    regDate.setText(user.getRegDate() + "");
                    lastLogin.setText(DateUtils.niceDay(user.getLastLoginDate()));
                    lastIp.setText(user.getLastIp());
                    status.setText(user.getStatus());
                    loginTimes.setText(user.getLoginTimes() + "");

                } else if (msg.obj != null) {
                    ((AppException) msg.obj).makeToast(UserInfo.this);
                }
            }
        };
        this.loadUserInfoThread(false);
    }

    private void loadUserInfoThread(final boolean isRefresh) {
        loading = new LoadingDialog(this);
        loading.show();

        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    User user = ((AppContext) getApplication()).getMyInformation(isRefresh);
                    msg.what = 1;
                    msg.obj = user;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    private View.OnClickListener editerClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            CharSequence[] items = {
                    getString(R.string.img_from_album),
                    getString(R.string.img_from_camera)
            };
            imageChooseItem(items);
        }
    };

    private View.OnClickListener refreshClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            loadUserInfoThread(true);
        }
    };


    /**
     * 操作选择
     *
     * @param items
     */
    public void imageChooseItem(CharSequence[] items) {
        AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle("上传头像").setIcon(android.R.drawable.btn_star).setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        //判断是否挂载了SD卡
                        String storageState = Environment.getExternalStorageState();
                        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                            File savedir = new File(FILE_SAVEPATH);
                            if (!savedir.exists()) {
                                savedir.mkdirs();
                            }
                        } else {
                            UIHelper.ToastMessage(UserInfo.this, "无法保存上传的头像，请检查SD卡是否挂载");
                            return;
                        }

                        //输出裁剪的临时文件
                        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                        //照片命名
                        String origFileName = "osc_" + timeStamp + ".jpg";
                        String cropFileName = "osc_crop_" + timeStamp + ".jpg";

                        //裁剪头像的绝对路径
                        protraitPath = FILE_SAVEPATH + cropFileName;
                        protraitFile = new File(protraitPath);

                        origUri = Uri.fromFile(new File(FILE_SAVEPATH, origFileName));
                        cropUri = Uri.fromFile(protraitFile);

                        //相册选图
                        if (item == 0) {
                            startActionPickCrop(cropUri);
                        }
                        //手机拍照
                        else if (item == 1) {
                            startActionCamera(origUri);
                        }
                    }
                }).create();

        imageDialog.show();
    }

    /**
     * 选择图片裁剪
     *
     * @param output
     */
    private void startActionPickCrop(Uri output) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra("output", output);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", CROP);// 输出图片大小
        intent.putExtra("outputY", CROP);
        startActivityForResult(Intent.createChooser(intent, "选择图片"), ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
    }

    /**
     * 相机拍照
     *
     * @param output
     */
    private void startActionCamera(Uri output) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
    }

    /**
     * 拍照后裁剪
     *
     * @param data   原始图片
     * @param output 裁剪后图片
     */
    private void startActionCrop(Uri data, Uri output) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("output", output);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", CROP);// 输出图片大小
        intent.putExtra("outputY", CROP);
        startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP);
    }

    /**
     * 上传新照片
     */
    private void uploadNewPhoto() {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (loading != null) loading.dismiss();
                if (msg.what == 1 && msg.obj != null) {
                    Result res = (Result) msg.obj;
                    //提示信息
                    UIHelper.ToastMessage(UserInfo.this, res.getErrorMessage());
                    if (res.isOk()) {
                        //显示新头像
                        face.setImageBitmap(protraitBitmap);
                    }
                } else if (msg.what == -1 && msg.obj != null) {
                    ((AppException) msg.obj).makeToast(UserInfo.this);
                }
            }
        };

        if (loading != null) {
            loading.setLoadText("正在上传头像···");
            loading.show();
        }

        new Thread() {
            public void run() {
                //获取头像缩略图
                if (!StringUtility.isEmpty(protraitPath) && protraitFile.exists()) {
                    protraitBitmap = ImageUtils.loadImgThumbnail(protraitPath, 200, 200);
                }

                if (protraitBitmap != null) {
                    Message msg = new Message();
                    try {
                        Result res = ((AppContext) getApplication()).updatePortrait(protraitFile);
                        if (res != null && res.isOk()) {
                            //保存新头像到缓存
                            String filename = FileUtils.getFileName(user.getFace());
                            ImageUtils.saveImage(UserInfo.this, filename, protraitBitmap);
                        }
                        msg.what = 1;
                        msg.obj = res;
                    } catch (AppException e) {
                        e.printStackTrace();
                        msg.what = -1;
                        msg.obj = e;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    handler.sendMessage(msg);
                }
            }

            ;
        }.start();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
                startActionCrop(origUri, cropUri);//拍照后裁剪
                break;
            case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
            case ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP:
                uploadNewPhoto();//上传新照片
                break;
        }
    }

}
