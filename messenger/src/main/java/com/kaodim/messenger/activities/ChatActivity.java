package com.kaodim.messenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.textservice.TextInfo;

import com.kaodim.messenger.R;
import com.kaodim.messenger.fragments.ChatFragment;
import com.kaodim.messenger.tools.AMessenger;
import com.kaodim.messenger.tools.Logs;
import com.kaodim.messenger.tools.NotificationManager;

/**
 * Created by Kanskiy on 12/10/2016.
 */

public class ChatActivity extends BaseBackButtonActivity {
    public static final String EXTRA_CONVERSATION_ID = "extra_id";
    public static final String EXTRA_INCOMING_MESSAGE_AVATAR = "extra_incoming_message_avatar";
    public static final String EXTRA_INCOMING_MESSAGE_USER_NAME = "extra_incoming_message_user_name";
    public static final String EXTRA_CHAT_CONTEXT_ID = "extra_chat_context_id";


    String conversationId;
    String chatContextId;
    String incomingMessageName;
    String incomingMessageAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (AMessenger.getInstance()==null){
            Logs.log(Logs.NOT_INITIALISED);
            return;
        }
        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        chatContextId = getIntent().getStringExtra(EXTRA_CHAT_CONTEXT_ID);
        if (android.text.TextUtils.isEmpty(conversationId)){
            Logs.log(Logs.NO_CONVERSATION_ID);
            return;
        }

        incomingMessageAvatar = getIntent().getStringExtra(EXTRA_INCOMING_MESSAGE_AVATAR);
        if (android.text.TextUtils.isEmpty(incomingMessageAvatar)){
            Logs.log(Logs.NO_AVATAR_PASSED);
        }

        incomingMessageName = getIntent().getStringExtra(EXTRA_INCOMING_MESSAGE_USER_NAME);
        if (android.text.TextUtils.isEmpty(incomingMessageName)){
            Logs.log(Logs.NO_NAME_PASSED);
        }else {
            setTitle(incomingMessageName);
        }


        if (savedInstanceState == null) {
            ChatFragment.newInstance(conversationId,incomingMessageAvatar).commit(getSupportFragmentManager());
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.itemRequest) {
            if (chatContextId!=null)
                AMessenger.getInstance().menItemClicked(chatContextId);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (chatContextId!=null && !TextUtils.isEmpty(AMessenger.getInstance().chatMenuTitle)){
            getMenuInflater().inflate(R.menu.menu_chat_activity, menu);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!TextUtils.isEmpty(AMessenger.getInstance().chatMenuTitle))
            menu.findItem(R.id.itemRequest).setTitle(AMessenger.getInstance().chatMenuTitle);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (conversationId!=null)
            NotificationManager.removeNotifications(conversationId, getApplicationContext());
    }
}