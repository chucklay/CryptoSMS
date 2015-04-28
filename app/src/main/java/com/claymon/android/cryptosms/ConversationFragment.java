package com.claymon.android.cryptosms;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ConversationFragment extends ListFragment {

    //Array of names. Debug.
    String[] names = new String[] {"Alice", "Bob", "Charlie", "Danny", "etc"};
    int[] photos = new int[] {R.drawable.contact_default,R.drawable.contact_default,R.drawable.contact_default,R.drawable.contact_default,R.drawable.contact_default};

    private OnFragmentInteractionListener mListener;

    public static ConversationFragment newInstance(String param1, String param2) {
        ConversationFragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConversationFragment() {
    }

    /**
     * Provide default implementation to return a simple list view.  Subclasses
     * can override to replace with their own layout.  If doing so, the
     * returned view hierarchy <em>must</em> have a ListView whose id
     * is {@link android.R.id#list android.R.id.list} and can optionally
     * have a sibling view id {@link android.R.id#empty android.R.id.empty}
     * that is to be shown when the list is empty.
     * <p/>
     * <p>If you are overriding this method with your own custom content,
     * consider including the standard layout {@link android.R.layout#list_content}
     * in your layout file, so that you continue to retain all of the standard
     * behavior of ListFragment.  In particular, this is currently the only
     * way to have the built-in indeterminant progress state be shown.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  super.onCreateView(inflater, container, savedInstanceState);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the conversations.

        Uri smsUri = Uri.parse("content://mms-sms/conversations");

        Cursor mCursor = getActivity().getContentResolver().query(smsUri, new
                String[] { "_id", "thread_id", "address", "person", "date"},
                null, null, null);

        getActivity().startManagingCursor(mCursor);

        List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();

        if(mCursor.getCount() > 0){
            String count = Integer.toString(mCursor.getCount());
            while(mCursor.moveToNext()){
                String address = mCursor.getString(mCursor.getColumnIndex("address"));
                String person = mCursor.getString(mCursor.getColumnIndex("person"));

                HashMap<String, String> hm = new HashMap<>();
                if(person == null){
                    //Person is null, use address instead.
                    hm.put("person", address);
                    hm.put("photo", Integer.toString(R.id.contactPhoto));
                    hm.put("knownContact", "false");
                }
                else{
                    hm.put("person", person);
                    hm.put("photo", Integer.toString(R.id.contactPhoto));
                    hm.put("knownContact", "true");
                }

                aList.add(hm);
            }
        }

        //System.err.println("aList size is: " + aList.size());
        //Get the contact photo, if the contact exists.
        for(int j = 0; j < aList.size(); j++){
            String[] stats = getContactPhoto(aList.get(j).get("person"));

            if(stats[0] == null){
               //No contact entry. Just display phone number.
                aList.get(j).put("photo", Integer.toString(R.drawable.contact_default));
            }
            else{
                //Contact found. Display name and phone number.
                aList.get(j).remove("person");
                aList.get(j).put("person", stats[0]);
                if(stats[1] != null) {
                    aList.get(j).put("photo", stats[1]);
                }
                else{
                    aList.get(j).put("photo", Integer.toString(R.drawable.contact_default));
                }
            }
        }

        //Keys
        String[] from = {"person", "photo"};

        //IDs of views in conversations_layout_list.
        int[] to = {R.id.contactName, R.id.contactPhoto};

        SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), aList, R.layout.conversations_layout_list, from, to);

        setListAdapter(adapter);
    }

    public String[] getContactPhoto(String number){
        Uri mUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, number);
        Cursor mCursor = getActivity().getContentResolver()
                .query(mUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI}, null, null, null);

        String mName = null;
        String mPhotoUri = null;

        getActivity().startManagingCursor(mCursor);

        if(mCursor.getCount() > 0){
            while(mCursor.moveToNext()){
                //This is the contact. Get their name and photo.
                mName = mCursor.getString(mCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                mPhotoUri = mCursor.getString(mCursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI));

                System.err.println("Photo Uri is: " + mPhotoUri);
            }
        }

        return new String[] {mName, mPhotoUri};
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
            mListener.onFragmentInteraction(names[position]);
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
