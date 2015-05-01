package com.claymon.android.cryptosms.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.claymon.android.cryptosms.R;

import java.util.ArrayList;

/**
 * Created by Charlie on 4/30/2015.
 */
public class MessageAdapter extends ArrayAdapter {

    private ArrayList<CryptoMessage> items;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public MessageAdapter(Context context, int resource, ArrayList<CryptoMessage> objects) {
        super(context, resource, objects);
        this.items = objects;
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            CryptoMessage current = items.get(position);

            if(current.isSent()){
                //This was a sent message, use the appropriate view.
                v = inflater.inflate(R.layout.sent_message, null);

                //Set up the views.
                TextView sentMessage = (TextView) v.findViewById(R.id.sentMessageText);
                TextView sentDate = (TextView) v.findViewById(R.id.sentMessageDate);

                if(sentMessage != null){
                    sentMessage.setText(current.getMessage());
                }

                if(sentDate != null){
                    sentDate.setText(current.getDate());
                }
            }
            else{
                //This was a received message, use the appropriate view.
                v = inflater.inflate(R.layout.recieved_message, null);

                //Set up the views
                TextView receivedMessage = (TextView) v.findViewById(R.id.recievedMessageText);
                TextView receivedDate = (TextView) v.findViewById(R.id.recievedTimestampText);

                if(receivedMessage != null){
                    receivedMessage.setText(current.getMessage());
                }

                if(receivedDate != null){
                    receivedDate.setText(current.getDate());
                }
            }
        }

        return v;
    }
}
