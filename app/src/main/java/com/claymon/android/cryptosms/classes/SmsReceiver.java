package com.claymon.android.cryptosms.classes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.claymon.android.cryptosms.R;
import com.claymon.android.cryptosms.ThreadContainer;

public class SmsReceiver extends BroadcastReceiver {

    final SmsManager manager = SmsManager.getDefault();
    private SharedPreferences preferences;

    boolean mShowNotification;
    boolean mVibrate;
    boolean mSound;

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

         mShowNotification = preferences.getBoolean("notifications_on", true);
         mVibrate = preferences.getBoolean("notifications_vibration_on", true);
         mSound= preferences.getBoolean("notifications_sound_on", true);

        System.err.println("Broadcast received! Notifications on: " + mShowNotification + ", Vibrations on: " + mVibrate + ", Sound on: " + mSound);



        try{
            if(bundle != null){
                final Object[] pdusArray = (Object[]) bundle.get("pdus");

                for(int i=0; i < pdusArray.length; i++){

                    System.err.println("Looping through PDUs array.");

                    //Get message contents.
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusArray[i]);
                    String incomingNumber = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();
                    String thread_id = "";

                    String where = "address=" + incomingNumber;

                    Uri smsUri = Uri.parse("content://sms/inbox");
                    Cursor mCursor = context.getContentResolver().query(smsUri, new String[] {"_id", "thread_id"}, where, null, null);

                    if (mCursor.moveToFirst()){
                        while(mCursor.moveToNext()){
                            thread_id = mCursor.getString(mCursor.getColumnIndex("thread_id"));
                        }
                    }
                    else{
                        System.err.println("Error moving cursor to first!");
                    }

                    mCursor.close();
                    if(thread_id.equals("")){
                        System.err.println("ERROR: Conversation not found!");
                    }

                    //Show a notification.
                    if(mShowNotification) {
                        if(preferences.getString("current", null) != null) {
                            if(!preferences.getString("current", "-1").equals(incomingNumber)) {
                                showNotification(incomingNumber, message, thread_id, context);
                            }
                            else {
                                System.err.println("Preferences string matches current number! Both are" + preferences.getString("current", "") + incomingNumber);
                            }
                        }
                        else {
                            System.err.println("Preferences string is null, showing notification.");
                            showNotification(incomingNumber, message, thread_id, context);
                        }
                    }
                }
            }
            else{
                System.err.println("error: bundle was null.");
            }
        } catch (Exception e){
            System.err.println("Error retrieving SMS: " + e);
            e.printStackTrace();
        }
    }

    private void showNotification(String incomingNumber, String message, String thread_id, Context context) {
        System.err.println("showNotification called!");
        NotificationCompat.Builder mBuilder = null;
        if(message.length() > 60) {
            if(mVibrate && mSound) {
                mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("Incoming Message")
                                .setContentText(message.substring(0, 60))
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setAutoCancel(true)
                                .setVibrate(new long[]{100, 50, 100});      //TODO get contact name, if possible.
            }
            else if(mVibrate){
                mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("Incoming Message")
                                .setContentText(message.substring(0, 60))
                                .setAutoCancel(true)
                                .setSound(null)
                                .setVibrate(new long[]{100, 50, 100});      //TODO get contact name, if possible.
            }
            else if(mSound){
                mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("Incoming Message")
                                .setContentText(message.substring(0, 60))
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setAutoCancel(true);      //TODO get contact name, if possible.
            }
        }
        else{
            if(mSound && mVibrate) {
                mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("Incoming Message")
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setVibrate(new long[]{100, 50, 100}); //TODO get personalized contact notification sound.
            }
            else if(mSound){
                mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("Incoming Message")
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)); //TODO get personalized contact notification sound.
            }
            else if(mVibrate){
                mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("Incoming Message")
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setSound(null)
                                .setVibrate(new long[]{100, 50, 100}); //TODO get personalized contact notification sound.
            }
        }
        Intent resultIntent = new Intent(context, ThreadContainer.class);

        resultIntent.putExtra("number", incomingNumber);
        resultIntent.putExtra("thread_id", thread_id);
        int notificationId = (int) Long.parseLong(incomingNumber)-1000000000;

                PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        System.err.println("Notification ID is: " + notificationId);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Issue the notification
        mNotificationManager.notify(notificationId, mBuilder.build());
    }
}
