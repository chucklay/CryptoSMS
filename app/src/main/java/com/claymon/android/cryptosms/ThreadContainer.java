package com.claymon.android.cryptosms;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.claymon.android.cryptosms.classes.CryptoMessage;
import com.claymon.android.cryptosms.classes.MessageAdapter;

import java.util.ArrayList;


/**
 * Displays the selected conversation.
 */

public class ThreadContainer extends ActionBarActivity implements MessageFragment.OnFragmentInteractionListener {

    Button mSendButton;
    EditText mSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_container);
        String name = "";
        final String number = getIntent().getStringExtra("number");

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
        mAdapter.add(new CryptoMessage(message, Long.toString(System.currentTimeMillis()), true));
        mAdapter.notifyDataSetChanged();
        fragment.getListView().smoothScrollToPosition(mAdapter.getCount()-1);

        return;
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
