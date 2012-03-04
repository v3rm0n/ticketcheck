package me.vermon.ticketcheck;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

/**
 * BroadcastReceiver that checks if tickets are available and notifies user
 * 
 * @author Maido Käära
 * 
 */
public class AlarmReceiver extends BroadcastReceiver {
	private static final String TAG = AlarmReceiver.class.getName();
	private static final String SHOW_ID = "show_id";

	/**
	 * Schedules this broadcast receiver to run.
	 */
	public static void setAlarm(Context context, int minutes, int showId) {
		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = getAlarmIntent(context, showId);
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), minutes * 60 * 1000, pi);
		Log.d(TAG, "Event scheduled");
	}

	/**
	 * Removes this broadcast receiver alarm.
	 */
	public static void removeAlarm(Context context) {
		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = getAlarmIntent(context, 0);
		mgr.cancel(alarmIntent);
		alarmIntent.cancel();
		Log.d(TAG, "Event canceled");
	}

	/**
	 * Checks if alarm is already set
	 */
	public static boolean isAlarmSet(Context context) {
		Intent i = new Intent(context, AlarmReceiver.class);
		return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_NO_CREATE) != null;
	}

	private static PendingIntent getAlarmIntent(Context context, int showId) {
		Intent i = new Intent(context, AlarmReceiver.class);
		i.putExtra(SHOW_ID, showId);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		return pi;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Event started");
		Bundle bundle = intent.getExtras();
		int showId = bundle.getInt(SHOW_ID);
		boolean checkTickets = checkTickets(showId);
		if (checkTickets) {
			notify(context);
			removeAlarm(context);
		}
	}

	private boolean checkTickets(int id) {
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL("http://www.piletilevi.ee/est/piletid/teater/show/?concert=" + id);
			urlConnection = (HttpURLConnection) url.openConnection();
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			String str = new Scanner(in).useDelimiter("\\A").next();
			boolean available = !str.contains("concert_details_ticketprices_unavailable");
			Log.d(TAG, "Tickets available: " + available);
			return available;
		} catch (Exception e) {
			Log.d(TAG, "Problem when parsing input", e);

		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return false;
	}

	private void notify(Context context) {
		CharSequence ticketsAreHere = context.getResources().getText(R.string.ticketshere);
		Notification notification = new Notification(R.drawable.piletilevi, ticketsAreHere, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notification.sound = getSound();
		notification.audioStreamType = AudioManager.STREAM_NOTIFICATION;

		notification.vibrate = new long[] { 0, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200,
				500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200,
				500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200, 500, 500, 200, 200, 500, 500 };

		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;

		CharSequence contentTitle = context.getResources().getText(R.string.notificationtitle);
		CharSequence contentText = context.getResources().getText(R.string.notificationtext);
		Intent notificationIntent = new Intent(context, AlarmReceiver.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(1, notification);

	}

	private Uri getSound() {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (alert == null) {
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (alert == null) {
				alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			}
		}
		return alert;
	}
}
