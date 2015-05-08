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
 * Created by Charlie on 5/2/2015.
 */
public class MessageAdapter extends ArrayAdapter {

    ArrayList<CryptoMessage> mMessages = new ArrayList<>();

    public MessageAdapter(Context context, ArrayList<CryptoMessage> messages){
        super(context, 0, messages);
        mMessages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        if(mMessages.get(position).isSent()){
            return 0;
        }
        else{
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param message The object to add at the end of the array.
     */
    public void add(CryptoMessage message) {
        mMessages.add(message);
        notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
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
        CryptoMessage current = (CryptoMessage) getItem(position);

        if(convertView == null){
            if(current.isSent()){
                //This is message that the user sent. Inflate the view for a sent message.
                LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.sent_message, parent, false);
            }
            else{
                //Otherwise, this is a message that the user received. Inflate the view for a received message.
                LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.recieved_message, parent, false);
            }
        }

        if(current.isSent()){
            //The current message is a sent message. Get the resources from the sent view.
            TextView body = (TextView) convertView.findViewById(R.id.sentMessageText);
            TextView date = (TextView) convertView.findViewById(R.id.sentMessageDate);

            //Populate the data.
            body.setText(current.getMessage());
            date.setText(current.getDate());
        }
        else{
            TextView body = (TextView) convertView.findViewById(R.id.recievedMessageText);
            TextView date = (TextView) convertView.findViewById(R.id.recievedTimestampText);

            body.setText(current.getMessage());
            date.setText(current.getDate());
        }

        return convertView;
    }
}
