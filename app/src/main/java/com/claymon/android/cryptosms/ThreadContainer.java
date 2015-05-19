package com.claymon.android.cryptosms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.claymon.android.cryptosms.classes.CryptoMessage;
import com.claymon.android.cryptosms.classes.MessageAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Displays the selected conversation.
 */

public class ThreadContainer extends AppCompatActivity implements MessageFragment.OnFragmentInteractionListener {

    Button mSendButton;
    EditText mSendMessage;
    String number;
    Receiver mReceiver;

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("current", number).apply();
        //notify data set changed to ensure all new messages are loaded.
        ListFragment currentFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.thread_container);
        MessageAdapter mAdapter = (MessageAdapter) currentFragment.getListAdapter();
        mAdapter.notifyDataSetChanged();
        //Register the receiver to ensure that things work well.
        registerReceiver(mReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    @Override
    protected void onPause(){
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("current", null).apply();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_container);
        String name = "";
        number = getIntent().getStringExtra("number");
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mReceiver = new Receiver();

        registerReceiver(mReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

        mPreferences.edit().putString("current", number).apply();

        System.err.println("Opening conversation with " + number);
        Uri mUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] query = new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor mCursor = this.getContentResolver().query(mUri, query, null, null, null);

        if(mCursor.moveToFirst()){
            name = mCursor.getString(0);
        }

        if(!name.equals("")) {
            setTitle(name);
        }
        else {
            setTitle(number);
        }

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendMessage = (EditText) findViewById(R.id.newMessageText);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mMessage = mSendMessage.getText().toString();
                if (mMessage.equals("")){
                    //Message is null, alert the user.
                    Toast mToast = Toast.makeText(getApplicationContext(), "Unable to send an empty message", Toast.LENGTH_SHORT);
                    mToast.show();
                }
                else{
                    //Send the message.
                    sendMessage(mMessage, number);
                }
            }
        });
    }



    /**
     * Sends the given message as a standard SMS message.
     *
     * @param message The message to be sent.
     * @param phoneNumber The phone number to send the message to.
     */
    private void sendMessage(String message, String phoneNumber) {
        System.err.println("Message is: " + message + "\nPhone number is: " + phoneNumber);
        boolean multipleMessages = false;
        ArrayList<String> messageParts = new ArrayList<>();
        SmsManager mManager = SmsManager.getDefault();          //Get the SMS manager.
        //Split the message, if needed.
        if(message.length() > 160){
            multipleMessages = true;
            messageParts = mManager.divideMessage(message);
        }
        //Send the messages.
        if(multipleMessages){
            mManager.sendMultipartTextMessage(phoneNumber, null, messageParts, null, null);
        }
        else {
            mManager.sendTextMessage(phoneNumber, null, message, null, null);
        }

        //Clear the editText.
        EditText mText = (EditText) findViewById(R.id.newMessageText);
        mText.setText("");

        //Add the message to the conversation, and scroll to the bottom.
        ListFragment fragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.thread_container);
        MessageAdapter mAdapter = (MessageAdapter) fragment.getListAdapter();
        Date date = new Date(System.currentTimeMillis());
        String currentTime = new SimpleDateFormat("MMM dd, hh:mm").format(date);
        mAdapter.add(new CryptoMessage(message, currentTime, true));
        mAdapter.notifyDataSetChanged();
        fragment.getListView().smoothScrollToPosition(mAdapter.getCount()-1);

        return;
    }

    private class Receiver extends BroadcastReceiver{

        /**
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle mBundle = intent.getExtras();

            System.err.println("onReceive called in inner receiver");

            try{
                if(mBundle != null){
                    final Object[] pdusArray = (Object[]) mBundle.get("pdus");
                    for(int i = 0; i < pdusArray.length; i++){
                        SmsMessage currentMesssage = SmsMessage.createFromPdu((byte[]) pdusArray[i]);
                        String messageAddr = currentMesssage.getOriginatingAddress();

                        if(number != null){
                            if(number.equals(messageAddr)){
                                //This is the current conversation. Do not display a notification, but refresh the data.
                                ListFragment currentConvo = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.thread_container);
                                MessageAdapter mAdapter = (MessageAdapter) currentConvo.getListAdapter();
                                Date mDate = new Date(currentMesssage.getTimestampMillis());
                                String formattedDate = new SimpleDateFormat("MMM dd, hh:mm").format(mDate);
                                mAdapter.add(new CryptoMessage(currentMesssage.getDisplayMessageBody(), formattedDate, false));
                                mAdapter.notifyDataSetChanged();
                                currentConvo.getListView().smoothScrollToPosition(mAdapter.getCount()-1);
                            }
                            else{
                                System.err.println("Number was " + number + " while addr was " + messageAddr);
                            }
                        }
                        else{
                            System.err.println("number was null.");
                        }
                    }
                }
                else{
                    System.err.println("Bundle was null");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_thread_container, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
