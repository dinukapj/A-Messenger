package com.kaodim.messenger.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.kaodim.messenger.R;
import com.kaodim.messenger.fragments.ChatFragment;
import com.kaodim.messenger.tools.AMessenger;
import com.kaodim.messenger.tools.Logs;

/**
 * Created by Kanskiy on 12/10/2016.
 */

public class ChatActivity extends BaseBackButtonActivity {
    String conversationId;
    String incommingMessageName;
    String incommingMessageAvatar;
    String chatUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (AMessenger.getInstance()==null){
            Logs.log(Logs.NOT_INITIALISED);
            return;
        }
        conversationId = getIntent().getStringExtra("extra_id");
        if (android.text.TextUtils.isEmpty(conversationId)){
            Logs.log(Logs.NO_CONVERSATION_ID);
            return;
        }

        chatUrl = AMessenger.getInstance().getChatUrl(conversationId);

        incommingMessageAvatar = getIntent().getStringExtra("extra_incomming_message_avatar");
        if (android.text.TextUtils.isEmpty(incommingMessageAvatar)){
            Logs.log(Logs.NO_AVATAR_PASSED);
        }

        incommingMessageName = getIntent().getStringExtra("extra_name");
        if (android.text.TextUtils.isEmpty(incommingMessageName)){
            Logs.log(Logs.NO_NAME_PASSED);
        }


        if (savedInstanceState == null) {
            ChatFragment chatFragment = ChatFragment.newInstance(conversationId,incommingMessageName,incommingMessageAvatar, chatUrl );
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.flContainer, chatFragment)
                    .commit();
        }
    }



}