package com.example.windows.pushexample2;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import java.util.Map;

public class MyApplication extends Application {
	private static final String TAG = MyApplication.class.getName();

	private PushAgent mPushAgent;

	@Override
	public void onCreate() {
		mPushAgent = PushAgent.getInstance(this);
		mPushAgent.setDebugMode(true);
		/**
		 * 该Handler是在IntentService中被调用，故
		 * 1. 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
		 * 2. IntentService里的onHandleIntent方法是并不处于主线程中，因此，如果需调用到主线程，需如下所示;
		 * 	      或者可以直接启动Service
		 * */
		UmengMessageHandler messageHandler = new UmengMessageHandler(){
			@Override
			public void dealWithCustomMessage(final Context context, final UMessage msg) {
				new Handler(getMainLooper()).post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
						Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
					}
				});
			}
			
			@Override
			public Notification getNotification(Context context,
					UMessage msg) {
				switch (msg.builder_id) {
				case 1:
					NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
					RemoteViews myNotificationView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
					myNotificationView.setTextViewText(R.id.notification_title, msg.title);
					myNotificationView.setTextViewText(R.id.notification_text, msg.text);
					myNotificationView.setImageViewBitmap(R.id.notification_large_icon, getLargeIcon(context, msg));
					myNotificationView.setImageViewResource(R.id.notification_small_icon, getSmallIconId(context, msg));
					builder.setContent(myNotificationView);
					builder.setAutoCancel(true);
					Notification mNotification = builder.build();
					//由于Android v4包的bug，在2.3及以下系统，Builder创建出来的Notification，并没有设置RemoteView，故需要添加此代码
					mNotification.contentView = myNotificationView;
					return mNotification;
				default:
					//默认为0，若填写的builder_id并不存在，也使用默认。
					return super.getNotification(context, msg);
				}
			}
		};
		mPushAgent.setMessageHandler(messageHandler);

		/**
		 * 该Handler是在BroadcastReceiver中被调用，故
		 * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
		 * */
		UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler(){
			@Override
			public void dealWithCustomAction(Context context, UMessage msg) {
			   Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
             //  final String url = msg.custom;
                final Map<String,String> url = msg.extra;
                System.out.println("url地址是");
                System.out.println(url);
              new Thread(new Runnable() {
                  @Override
                  public void run() {
                      Intent intent = new Intent(MyApplication.this,WebActivity.class);
                     // intent.putExtra("url", url.get("url"));
                      intent.putExtra("url",url.get("url"));
                      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                      //WebActivity.web.loadUrl(msg.custom);
                      startActivity(intent);
                  }
              }).start();
               //WebActivity.mWebView.loadUrl(msg.custom);
			}
		};
		mPushAgent.setNotificationClickHandler(notificationClickHandler);
	}

}
