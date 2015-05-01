package com.claymon.android.cryptosms.classes;

/**
 * Created by Charlie on 4/30/2015.
 */
public class CryptoMessage {
    private String message;
    private String date;
    private boolean sent;

    public CryptoMessage(String message, String date, boolean sent) {
        this.message = message;
        this.date = date;
        this.sent = sent;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
