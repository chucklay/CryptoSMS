package com.claymon.android.cryptosms.classes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.claymon.android.cryptosms.R;
import com.claymon.android.cryptosms.ThreadContainer;

public class SmsReceiver extends BroadcastReceiver {

    final SmsManager manager = SmsManager.getDefault();

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();

        System.err.println("Broadcast received!");

        try{
            if(bundle != null){
                final Object[] pdusArray = (Object[]) bundle.get("pdus");

                for(int i=0; i < pdusArray.length; i++){

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
                    showNotification(incomingNumber, message, thread_id, context);
                }
            }
        } catch (Exception e){
            System.err.println("Error retrieving SMS: " + e);
            e.printStackTrace();
        }
    }

    private void showNotification(String incomingNumber, String message, String thread_id, Context context) {
        NotificationCompat.Builder mBuilder;
        if(message.length() > 60) {
            mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.contact_default)
                            .setContentTitle("Incoming Message")
                            .setContentText(message.substring(0, 60));      //TODO get contact name, if possible.
        }
        else{
            mBuilder =
                    new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.contact_default)
                    .setContentTitle("Incoming Message")
                    .setContentText(message);
        }
        Intent resultIntent = new Intent(context, ThreadContainer.class);

        resultIntent.putExtra("number", incomingNumber);
        resultIntent.putExtra("thread_id", thread_id);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Issue the notification
        mNotificationManager.notify(1, mBuilder.build());
    }
}
