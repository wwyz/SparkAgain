package com.wwyz.loltv.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
//		Log.i("debug", "in receiver start");
		Intent broadcast_intent = new Intent(context, NotificationService.class);
        broadcast_intent.putExtra("msg", intent.getStringExtra("msg"));
        broadcast_intent.putExtra("rand", intent.getIntExtra("rand", 1));   
		WakefulIntentService.sendWakefulWork(context, broadcast_intent);
	}
}
