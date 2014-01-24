package com.wwyz.loltv.notification;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;



	public class AlarmService {
	    private Context context;
	    private PendingIntent mAlarmSender;

	    
	    public AlarmService(Context context, String mTitle, int mRand) {
	        this.context = context;
	        Intent broadcast_intent = new Intent(context, AlarmReceiver.class);
	        broadcast_intent.putExtra("msg", mTitle);
	        broadcast_intent.putExtra("rand", mRand);   
	        mAlarmSender = PendingIntent.getBroadcast(context, mRand, broadcast_intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    }

	    public void startAlarm(long millseconds){
	        //Set the alarm to 10 seconds from now
//	        Calendar c = Calendar.getInstance();
//	        c.add(Calendar.SECOND, 10);
//	        long firstTime = c.getTimeInMillis();
	        // Schedule the alarm!
	        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	        am.set(AlarmManager.RTC_WAKEUP, millseconds, mAlarmSender);
	    }
	}
