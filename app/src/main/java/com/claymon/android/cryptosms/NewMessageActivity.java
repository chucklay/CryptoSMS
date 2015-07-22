package com.claymon.android.cryptosms;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.claymon.android.cryptosms.classes.CompleteCursorAdapter;

import java.util.ArrayList;


/**
 The activity that allows the user to send a message to a new number.
 */

public class NewMessageActivity extends ActionBarActivity {

    private AutoCompleteTextView destination;
    private TextView mNewMessage;
    private Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        setTitle(getString(R.string.new_message_title));

        //Set up the behavior for the recipient field.
        destination = (AutoCompleteTextView) findViewById(R.id.destination_number);

        //Get the list of contacts, add it to an array adapter.
        /*
        ArrayList<String>[] contactNames = getContactList();
        ArrayList<String> contactNumbers = getContactNumbers(contactNames[1]);
        */

        //Initialize the CursorAdapter.
        final ContentResolver cr = getContentResolver();
        Cursor nameCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE},
                ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "> 0", null, null);

        CompleteCursorAdapter mAdapter = new CompleteCursorAdapter(NewMessageActivity.this, nameCursor, false);

        //nameCursor.close();

        FilterQueryProvider filter = new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String query = "(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +
                        " LIKE '%" + constraint + "%' OR " +
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                        + " LIKE '%" + constraint + "%') AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "> 0";

                return cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE},
                        query, null, null);
            }
        };

        mAdapter.setFilterQueryProvider(filter);
        destination.setAdapter(mAdapter);
        destination.setThreshold(2);

        //Set up the text view for the new message.
        mNewMessage = (TextView) findViewById(R.id.new_message);

        //Set up the send button.
        mSendButton = (Button) findViewById(R.id.send_new);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the entered phone number
                String phoneNumber = normalizeNumber(destination.getText().toString());

                //Ensure that both the text and recipient fields have data.
                if(mNewMessage.getText().equals("")){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.empty_message, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                else if(destination.getText().toString().equals("")){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.empty_recipient, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                else{
                    //Attempt to send the message.
                    sendMessage(mNewMessage.getText().toString(), phoneNumber);

                    //Get the conversation ID.
                    String id = "";
                    String where = "address = '" + phoneNumber + "'";
                    Uri uri = Uri.parse("content://sms/sent");
                    Cursor idCursor = getContentResolver().query(uri, new String[]{"_id", "thread_id"},
                            where, null, null);
                    if(idCursor.moveToFirst()) {
                        while (idCursor.moveToNext()) {
                            id = idCursor.getString(idCursor.getColumnIndex("thread_id"));
                        }
                    }
                    else{
                        System.err.println("idCursor did not move to first!");
                    }
                    idCursor.close();

                    //Go to this conversation.
                    Intent intent = new Intent(getApplicationContext(), ThreadContainer.class);
                    intent.putExtra("number", phoneNumber);
                    intent.putExtra("thread_id", id);
                    startActivity(intent);
                }
            }
        });
    }

    private String normalizeNumber(String number){
        StringBuilder result = new StringBuilder(number);
        for(int i=0; i < result.length(); i++){
            if(!Character.isDigit(result.charAt(i))){
                result.deleteCharAt(i);
                i--;
            }
        }
        return result.toString();
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
    }

    /**
     * Gets a list of all contacts with phone numbers.
     * @return A list of all contact names and IDs with associated phone numbers. Index 0 contains phone numbers and index 1 contains IDs
     */
    @Deprecated
    public ArrayList<String>[] getContactList(){
        ArrayList<String>[] contactList = new ArrayList[2];
        contactList[0] = new ArrayList<>();
        contactList[1] = new ArrayList<>();

        Cursor mContactsCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, "HAS_PHONE_NUMBER = 1", null, null);
        while(mContactsCursor.moveToNext()){
            contactList[0].add(mContactsCursor.getString(mContactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            contactList[1].add(mContactsCursor.getString(mContactsCursor.getColumnIndex(ContactsContract.Contacts._ID)));
        }

        mContactsCursor.close();

        return contactList;
    }

    /**
     * Gets a list of phone numbers associated with the given list of contact IDs.
     * @param id a list of contact IDs
     * @return An ArrayList containing all phone numbers associated with the contacts.
     */
    @Deprecated
    public ArrayList<String> getContactNumbers(ArrayList<String> id){
        ArrayList<String> numbers = new ArrayList<>();

        //Loop through the given list of IDs, and add the phone number from each to the list of
        //Phone numbers.
        for(int i = 0; i < id.size(); i++){
            Uri contact = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, id.get(i));
            Cursor contactCursor = getContentResolver().query(contact, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);             //TODO ensure that I'm only getting the needed columns, same with the above query.
            while (contactCursor.moveToNext()){
                String current = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                current = current.substring(2);
                numbers.add(current);
            }
            contactCursor.close();
        }
        return numbers;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_message, menu);
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
}
