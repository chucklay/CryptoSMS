package com.claymon.android.cryptosms;

import android.app.Activity;
import android.os.Bundle;
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

    // TODO: Rename and change types of parameters
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

        if (getArguments() != null) {
        }


        List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < names.length; i++){
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("name", names[i]);
            hm.put("photo", Integer.toString(photos[i]));
            aList.add(hm);
        }

        //Keys
        String[] from = {"name", "photo"};

        //IDs of views in conversations_layout_list.
        int[] to = {R.id.contactName, R.id.contactPhoto};

        SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), aList, R.layout.conversations_layout_list, from, to);

        setListAdapter(adapter);
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
