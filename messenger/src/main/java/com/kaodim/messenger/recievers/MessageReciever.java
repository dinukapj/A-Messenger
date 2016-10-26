package com.kaodim.messenger.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Kanskiy on 13/10/2016.
 */

public class MessageReciever  extends BroadcastReceiver {
    public static final String FILTER_MESSAGE_RECEIVER = "FILTER_MESSAGE_RECEIVER";
    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_SENDER = "extra_sender";
    public static final String EXTRA_MESSAGE = "extra_message";
    @Override
    public void onReceive(Context context, Intent intent) {

    }
}