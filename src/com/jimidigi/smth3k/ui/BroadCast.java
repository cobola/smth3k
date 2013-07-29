package com.jimidigi.smth3k.ui;

import com.jimidigi.smth3k.AppContext;
import com.jimidigi.smth3k.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * 通知信息广播接收器
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-4-16
 */
public class BroadCast extends BroadcastReceiver {

    private final static int NOTIFICATION_ID = R.layout.main;

    private static int lastNoticeCount;

    @Override
    public void onReceive(Context context, Intent intent) {
        String ACTION_NAME = intent.getAction();
        if ("com.jimidigi.smth3k.action.APPWIDGET_UPDATE".equals(ACTION_NAME)) {
            int atmeCount = intent.getIntExtra("atmeCount", 0);//@我
            int replyCount = intent.getIntExtra("replyCount", 0);//回复
            boolean newMail = intent.getBooleanExtra("newMail", false);//邮件
            int activeCount = atmeCount + replyCount;//信息总数



            //message
            if (Main.bv_message != null) {
                if (activeCount > 0) {
                    Main.bv_message.setText(activeCount + "");
                    Main.bv_message.show();
                } else if (newMail) {
                    Main.bv_message.setText("新");
                    Main.bv_message.show();
                } else {
                    Main.bv_message.setText("");
                    Main.bv_message.hide();
                }
            }

            //通知栏显示
            this.notification(context, activeCount);
        }
    }

    private void notification(Context context, int noticeCount) {
        //创建 NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String contentTitle = "水木三千";
        String contentText = "您有 " + noticeCount + " 条最新信息";
        int _lastNoticeCount;

        //判断是否发出通知信息
        if (noticeCount == 0) {
            notificationManager.cancelAll();
            lastNoticeCount = 0;
            return;
        } else if (noticeCount == lastNoticeCount) {
            return;
        } else {
            _lastNoticeCount = lastNoticeCount;
            lastNoticeCount = noticeCount;
        }

        //创建通知 Notification
        Notification notification = null;

        if (noticeCount > _lastNoticeCount) {
            String noticeTitle = "您有 " + (noticeCount - _lastNoticeCount) + " 条最新信息";
            notification = new Notification(R.drawable.icon, noticeTitle, System.currentTimeMillis());
        } else {
            notification = new Notification();
        }

        //设置点击通知跳转
        Intent intent = new Intent(context, Main.class);
        intent.putExtra("NOTICE", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //设置最新信息
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        //设置点击清除通知
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        if (noticeCount > _lastNoticeCount) {
            //设置通知方式
            notification.defaults |= Notification.DEFAULT_LIGHTS;

            //设置通知音-根据app设置是否发出提示音
            if (((AppContext) context.getApplicationContext()).isAppSound())
                notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notificationsound);

            //设置振动 <需要加上用户权限android.permission.VIBRATE>
            //notification.vibrate = new long[]{100, 250, 100, 500};
        }

        //发出通知
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
