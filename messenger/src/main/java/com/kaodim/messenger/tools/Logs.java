package com.kaodim.messenger.tools;

import android.util.Log;

/**
 * Created by Kanskiy on 20/10/2016.
 */

public class Logs {
    public static final String TAG = "A-Messenger";
    public static final String NOT_INITIALISED = "seems like I was not initialised";
    public static final String NO_CONVERSATION_ID = "I didn't get conversationId";
    public static final String NO_AVATAR_PASSED = "no avatar url given";
    public static final String NO_NAME_PASSED = "no name given";
    public static void log(String log){
        Log.d(TAG, log);
    }
}
