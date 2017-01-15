package com.kaodim.messenger.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.kaodim.messenger.R;
import com.kaodim.messenger.fragments.ChatFragment;
import com.kaodim.messenger.fragments.ConversationsFragment;
import com.kaodim.messenger.tools.TextUtils;

import static com.kaodim.messenger.tools.ExtraKeeper.EXTRA_OUTGOING_USER_ID;

/**
 * Created by Kanskiy on 12/01/2017.
 */

public abstract class MessengerActivity extends BaseBackButtonActivity implements ConversationsFragment.OnConversationFragmentListener, ChatFragment.OnChatFragmentListener {

    private final String TAG = getClass().getName();
    String outgoinUserId;
    protected ConversationsFragment conversationsFragment;
    protected ChatFragment chatFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        outgoinUserId = getIntent().getStringExtra(EXTRA_OUTGOING_USER_ID);
        goToConversations();
    }

    @Override
    public void onConversationSelected(String groupId, String conversationName) {
        goToChat(groupId, conversationName);
    }

    private void goToConversations(){
        setTitle(getString(R.string.messenger_title_conversation_activity));
        conversationsFragment = ConversationsFragment.newInstance();
        changeFragment(conversationsFragment);
    }

    private void goToChat(String groupId, String conversationName){
        if (!TextUtils.isEmpty(conversationName)){
            setTitle(conversationName);
        }else{
            setTitle(getString(R.string.messenger_title_messages));
        }

        chatFragment = ChatFragment.newInstance(groupId, outgoinUserId);
        changeFragment(chatFragment);
    }
    private void changeFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.flRoot, fragment, fragment.getClass().getName())
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }
    @Override
    public void onBackPressed(){
        if (getSupportFragmentManager().getBackStackEntryCount() == 1){
            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_down);
        }
        else {
            setTitle(getString(R.string.messenger_title_conversation_activity));
            hideKeyBoard();
            super.onBackPressed();
        }
    }
    protected  void hideKeyBoard(){
            final InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
    }
    //Here Builder
    public  static abstract class Builder {
        private  Bundle args;
        public Builder() {
            args  = new Bundle();
        }
        public void show(Context context, String userId) {
            Log.d("MessengerActivity", "show: showing MessengerActivity");
            args.putString(EXTRA_OUTGOING_USER_ID, userId);
            context.startActivity(this.buildIntent(context));
            ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.stay);
        }
        private Intent buildIntent(Context context) {
            Log.d("MessengerActivity", "intent: creating Intent");
            Intent intent = new Intent(context, getChildActivityClass());
            intent.putExtras(this.args);
            return intent;
        }
        protected abstract Class getChildActivityClass();
    }
}
