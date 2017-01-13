package com.kaodim.messenger.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.kaodim.messenger.R;
import com.kaodim.messenger.fragments.ChatFragment;
import com.kaodim.messenger.fragments.ConversationsFragment;

import static com.kaodim.messenger.tools.ExtraKeeper.EXTRA_OUTGOING_USER_ID;

/**
 * Created by Kanskiy on 12/01/2017.
 */

public abstract class MessengerActivity extends BaseBackButtonActivity implements ConversationsFragment.OnConversationFragmentListener {

    private final String TAG = getClass().getName();
    protected abstract void getConversation(int page);
    String outgoinUserId;
    protected ConversationsFragment conversationsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        outgoinUserId = getIntent().getStringExtra(EXTRA_OUTGOING_USER_ID);
        conversationsFragment = ConversationsFragment.newInstance();
        changeFragment(conversationsFragment);
    }

    @Override
    public void onConversationSelected(String groupId) {
        goToChat(groupId);
    }

    @Override
    public void getConversationList(int page) {
        getConversation(page);
    }



    private void goToChat(String groupId){
        ChatFragment chatFragment = ChatFragment.newInstance(groupId, outgoinUserId);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flRoot, chatFragment, groupId)
                .addToBackStack(groupId)
                .commit();
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
        }
        else {
            super.onBackPressed();
        }
    }
    //Here Builder
    public  static abstract class Builder {
        private  Bundle args;
        public Builder() {
            args  = new Bundle();
        }
        public void show(Context context) {
            Log.d("MessengerActivity", "show: showing MessengerActivity");
            context.startActivity(this.buildIntent(context));
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
