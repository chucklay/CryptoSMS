package com.claymon.android.cryptosms;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.claymon.android.cryptosms.dummy.DummyContent;

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

        if(mPhoneNumber == null){
            //This should never happen.
            System.err.println("ERROR GETTING PHONE NUMBER. How did you do this?");
        }
        else{

            //Get all SMS/MMS messages with the given
            Uri mUri = Uri.parse("content://sms-mms/conversations");

            Cursor mCursor = getActivity().getContentResolver().query(mUri, new String[] {"_ID", "DATE", "BODY"}, "'ADDRESS' = ? AND ('TYPE' = '2' OR 'TYPE' = '2') ", new String[] {mPhoneNumber}, null);

            getActivity().startManagingCursor(mCursor);

            if(mCursor.getCount() > 0){
                while (mCursor.moveToNext()){
                    HashMap<String, String> hm = new HashMap<>();

                    hm.put("body", mCursor.getString(mCursor.getColumnIndex("BODY")));
                    hm.put("date", mCursor.getString(mCursor.getColumnIndex("DATE")));

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
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
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
