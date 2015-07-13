package com.claymon.android.cryptosms;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.claymon.android.cryptosms.classes.CryptoMessage;
import com.claymon.android.cryptosms.classes.MessageAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of messages. This makes up an individual conversation.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MessageFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;
    int total = 0;
    MessageAdapter mAdapter;

    List<HashMap<String, String>> messageList = new ArrayList<HashMap<String, String>>();

    public static MessageFragment newInstance(String param1, String param2) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessageFragment() {
    }

    /**
     * Get the ListAdapter associated with this activity's ListView.
     */
    @Override
    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    /**
     * Attach to list view once the view hierarchy has been created.
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();

        String mPhoneNumber = getActivity().getIntent().getStringExtra("number");
        String mThreadId = getActivity().getIntent().getStringExtra("thread_id");

        if(mPhoneNumber == null || mThreadId == null){
            //This should never happen.
            System.err.println("ERROR GETTING PHONE NUMBER OR THREAD_ID. How did you do this?");
        }
        else{

            //Get all SMS messages with the given conversation
            Uri mInboxUri = Uri.parse("content://sms/inbox");
            Uri mOutboxUri = Uri.parse("content://sms/sent");

            String where = "thread_id=" + mThreadId;

            Cursor mInboxCursor = getActivity().getContentResolver().query(mInboxUri, new String[] {"_id", "date", "body"}, where, null, "date ASC");
            Cursor mOutboxCursor = getActivity().getContentResolver().query(mOutboxUri, new String[] {"_id", "date", "body"}, where, null, "date ASC");

            getActivity().startManagingCursor(mInboxCursor);
            getActivity().startManagingCursor(mOutboxCursor);

            MergeCursor mCursor = new MergeCursor(new Cursor[]{mInboxCursor, mOutboxCursor});

            ArrayList<CryptoMessage> mMessages = new ArrayList<>();

            int mInboxTotal = 0;            //Total number of inbox messages read.
            int mOutboxTotal = 0;           //Total number of outbox messages read.

            total = mInboxCursor.getCount() + mOutboxCursor.getCount();

            while(mInboxTotal < mInboxCursor.getCount() || mOutboxTotal < mOutboxCursor.getCount()){
                if(mInboxTotal == mInboxCursor.getCount()){
                    //If the inbox cursor cannot be moved to next, take from the outbox.
                    while(mOutboxCursor.moveToNext()){
                        CryptoMessage current;
                        String message = mOutboxCursor.getString(mOutboxCursor.getColumnIndex("body"));
                        String date = mOutboxCursor.getString(mOutboxCursor.getColumnIndex("date"));

                        Date formattedDate = new Date(Long.parseLong(date));
                        String formattedDateString = new SimpleDateFormat("MMM dd, hh:mm").format(formattedDate);

                        current = new CryptoMessage(message, formattedDateString, true);

                        mMessages.add(current);
                        mOutboxTotal++;
                    }
                    mInboxTotal = mInboxCursor.getCount();
                    mOutboxTotal = mOutboxCursor.getCount();
                    break;
                }
                else if(mOutboxTotal == mOutboxCursor.getCount()){
                    //If the outbox cursor cannot be moved to next, take from the inbox.
                    while(mInboxCursor.moveToNext()){
                        CryptoMessage current;

                        String message = mInboxCursor.getString(mInboxCursor.getColumnIndex("body"));
                        String date = mInboxCursor.getString(mInboxCursor.getColumnIndex("date"));

                        Date formattedDate = new Date(Long.parseLong(date));
                        String formattedDateString = new SimpleDateFormat("MMM dd, hh:mm").format(formattedDate);

                        current = new CryptoMessage(message, formattedDateString, false);

                        mMessages.add(current);
                        mInboxTotal++;
                    }
                    mInboxTotal = mInboxCursor.getCount();
                    mOutboxTotal = mOutboxCursor.getCount();
                    break;
                }

                else{
                    mInboxCursor.moveToNext();
                    mOutboxCursor.moveToNext();

                    long inDate = Long.parseLong(mInboxCursor.getString(mInboxCursor.getColumnIndex("date")));
                    long outDate = Long.parseLong(mOutboxCursor.getString(mOutboxCursor.getColumnIndex("date")));

                    CryptoMessage current;

                    if(inDate < outDate){
                        String message = mInboxCursor.getString(mInboxCursor.getColumnIndex("body"));
                        String date = mInboxCursor.getString(mInboxCursor.getColumnIndex("date"));

                        Date formattedDate = new Date(Long.parseLong(date));
                        String formattedDateString = new SimpleDateFormat("MMM dd, hh:mm").format(formattedDate);

                        current = new CryptoMessage(message, formattedDateString, false);

                        //Do this to prevent the other cursor from being advanced incorrectly.
                        mOutboxCursor.moveToPrevious();
                        mInboxTotal++;
                    }
                    else{
                        String message = mOutboxCursor.getString(mOutboxCursor.getColumnIndex("body"));
                        String date = mOutboxCursor.getString(mOutboxCursor.getColumnIndex("date"));

                        Date formattedDate = new Date(Long.parseLong(date));
                        String formattedDateString = new SimpleDateFormat("MMM dd, hh:mm").format(formattedDate);

                        current = new CryptoMessage(message, formattedDateString, true);

                        //Do this to prevent the other cursor from being advanced incorrectly.
                        mInboxCursor.moveToPrevious();
                        mOutboxTotal++;
                    }

                    mMessages.add(current);
                }
            }

            mInboxCursor.close();
            mOutboxCursor.close();

            mAdapter = new MessageAdapter(getActivity(), mMessages);

            setListAdapter(mAdapter);
        }

        if(total > 0) {
            setSelection(total - 1);
        }

        //If a notification for this conversation exists, close it.
        int notificationId = (int) Long.parseLong(mPhoneNumber)-1000000000;
        System.err.println("Attempting to dismiss notification with ID: " + notificationId);
        NotificationManager notificationManager = (NotificationManager) getActivity().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }




    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.

            mListener.onFragmentInteraction(position + "");
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
