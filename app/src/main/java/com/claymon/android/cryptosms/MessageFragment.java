package com.claymon.android.cryptosms;

import android.app.Activity;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MessageFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;

    List<HashMap<String, String>> messageList = new ArrayList<HashMap<String, String>>();

    // TODO: Rename and change types of parameters
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

            /*
            if(mCursor.getCount() > 0){
                while(mCursor.moveToNext()){
                    HashMap<String, String> hm = new HashMap<>();

                    hm.put("body", mCursor.getString(mCursor.getColumnIndex("body")));
                    hm.put("date", mCursor.getString(mCursor.getColumnIndex("date")));

                    messageList.add(hm);
                }
            }
            */

            int mTotal = mInboxCursor.getCount() + mOutboxCursor.getCount();

            System.err.println("mTotal is: " + mTotal);

            for(int i = 0; i < mTotal; i++){
                if(!mInboxCursor.moveToNext()){
                    //If the inbox cursor cannot be moved to next, take from the outbox.
                    while(mOutboxCursor.moveToNext()){
                        HashMap<String, String> hm = new HashMap<>();

                        hm.put("body", mOutboxCursor.getString(mOutboxCursor.getColumnIndex("body")));
                        hm.put("date", mOutboxCursor.getString(mOutboxCursor.getColumnIndex("date")));

                        messageList.add(hm);
                    }
                    break;
                }
                else if(!mOutboxCursor.moveToNext()){
                    //If the outbox cursor cannot be moved to next, take from the inbox.

                    //Add the current first, as the above if statement will have moved it once already.
                    HashMap<String, String> hm1 = new HashMap<>();

                    hm1.put("body", mInboxCursor.getString(mInboxCursor.getColumnIndex("body")));
                    hm1.put("date", mInboxCursor.getString(mInboxCursor.getColumnIndex("date")));

                    messageList.add(hm1);

                    while(mInboxCursor.moveToNext()){
                        HashMap<String, String> hm = new HashMap<>();

                        hm.put("body", mInboxCursor.getString(mInboxCursor.getColumnIndex("body")));
                        hm.put("date", mInboxCursor.getString(mInboxCursor.getColumnIndex("date")));

                        messageList.add(hm);
                    }
                    break;
                }

                else{
                    long inDate = Long.parseLong(mInboxCursor.getString(mInboxCursor.getColumnIndex("date")));
                    long outDate = Long.parseLong(mOutboxCursor.getString(mOutboxCursor.getColumnIndex("date")));

                    HashMap<String, String> hm = new HashMap<>();

                    if(inDate < outDate){
                        hm.put("body", mInboxCursor.getString(mInboxCursor.getColumnIndex("body")));
                        hm.put("date", mInboxCursor.getString(mInboxCursor.getColumnIndex("date")));

                        //Do this to prevent the cursor from being advanced needlessly.
                        mOutboxCursor.moveToPrevious();
                    }
                    else{
                        hm.put("body", mOutboxCursor.getString(mOutboxCursor.getColumnIndex("body")));
                        hm.put("date", mOutboxCursor.getString(mOutboxCursor.getColumnIndex("date")));

                        mInboxCursor.moveToPrevious();
                    }

                    messageList.add(hm);
                }
            }

            String[] from = {"body", "date"};
            int[] to = {R.id.recievedMessageText, R.id.recievedTimestampText};

            setListAdapter(new SimpleAdapter(getActivity().getBaseContext(), messageList, R.layout.recieved_message, from, to));
        }
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
