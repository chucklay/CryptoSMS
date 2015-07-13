package com.claymon.android.cryptosms.classes;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.claymon.android.cryptosms.R;

/**
 * Created by Charlie on 7/12/2015.
 */
public class CompleteCursorAdapter extends CursorAdapter {

    LayoutInflater mInflater;

    /**
     * Constructor that allows control over auto-requery.  It is recommended
     * you not use this, but instead .
     * When using this constructor, {@link #FLAG_REGISTER_CONTENT_OBSERVER}
     * will always be set.
     *
     * @param context     The context
     * @param c           The cursor from which to get the data.
     * @param autoRequery If true the adapter will call requery() on the
     *                    cursor whenever it changes so the most recent
     */
    public CompleteCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.new_message_adapter_view, null);
    }

    @Override
    public CharSequence convertToString(Cursor cursor){
        return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.adapter_name);
        name.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));

        TextView number = (TextView) view.findViewById(R.id.adapter_number);
        number.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

        TextView type = (TextView) view.findViewById(R.id.adapter_type);
        switch (cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))){
            case "2":
                type.setText("Mobile");
                break;
            case "1":
                type.setText("Home");
                break;
            default:
                type.setText("Other");
                break;
        }
    }
}
