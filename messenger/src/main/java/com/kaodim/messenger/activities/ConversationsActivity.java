package com.kaodim.messenger.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kaodim.messenger.R;
import com.kaodim.messenger.adapters.ConversationsAdapter;
import com.kaodim.messenger.models.ConversationModel;

import java.util.ArrayList;

/**
 * Created by Kanskiy on 11/10/2016.
 */

public abstract class ConversationsActivity extends AppCompatActivity {

    private AQuery aq;
    private ArrayList<ConversationModel> mMessages;
    private ConversationsAdapter mAdapter;
    private Context mContext;
    protected Gson gson;
    private final String TAG = getClass().getName();

    protected abstract ArrayList<ConversationModel> fromJsonToConverstionModelArray(String json);
    protected abstract String getConversationUrl();
    protected abstract Class<? extends ChatActivity> getChatActivityChild();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        setTitle(getString(R.string.messenger_title_conversation_activity));
        showBackButton(getIntent().getBooleanExtra("extra_should_show_back_button", false));
        aq = new AQuery(this);
        mContext = this;
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();

        ListView lvMessages = (ListView) findViewById(R.id.lvMessages);
        lvMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, getChatActivityChild());
                intent.putExtra("extra_name", mMessages.get(position).getName());
                intent.putExtra("extra_id", mMessages.get(position).getId());
                intent.putExtra("extra_incomming_message_avatar", mMessages.get(position).getAvatar());
                startActivity(intent);
            }
        });

//        if (getIntent().getStringExtra("msg") != "") {
//            GcmMessageHelper.mMessageNumber = 1;
//        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }



    private  void showBackButton(boolean shouldShowBackButton){
        if (!shouldShowBackButton){
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateMessageList();
//        registerReceiver(mMessageReceiver, new IntentFilter(GcmMessageHelper.FILTER_MESSAGE_RECEIVER));
    }



    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(mMessageReceiver);
    }
    private void updateMessageList(){
        aq.ajax(getConversationUrl(), String.class, this, "callbackPerformGetMessage");
    }

    public void menuBackClicked(View view) {
        onBackPressed();
    }

    public void callbackPerformGetMessage(String url, String json, AjaxStatus status) {
        if (json != null) {

            mMessages = fromJsonToConverstionModelArray(json);
//            mMessages = jsonParser.fromJsonToConversationArray(json);
            if (mMessages.size()==0){
                aq.id(R.id.tvError).text(getString(R.string.messenger_no_conversations)).visibility(View.VISIBLE);
                aq.id(R.id.lvMessages).visibility(View.INVISIBLE);
            }else{
                aq.id(R.id.lvMessages).visibility(View.VISIBLE);
                aq.id(R.id.tvError).visibility(View.INVISIBLE);
            }

            mAdapter = new ConversationsAdapter(mContext, mMessages);
            aq.id(R.id.lvMessages).adapter(mAdapter);
            Log.d("get messages result", json.toString());
        } else {
            aq.id(R.id.tvError).text(getString(R.string.messenger_HTTP_RANDOM_ERROR)).visibility(View.VISIBLE);
            aq.id(R.id.lvMessages).visibility(View.INVISIBLE);
            Log.d("get messages error", status.getError());
        }
    }

    //TODO Builder
    public  static class Builder {
        private final Bundle args = new Bundle();
        Class<? extends ConversationsActivity> conversationActivityChild;
        public Builder(Class<? extends ConversationsActivity> conversationActivityChild) {
            this.conversationActivityChild = conversationActivityChild;
        }
        public ConversationsActivity.Builder showBackButton(boolean shouldShowBackButton) {
            this.args.putBoolean("extra_should_show_back_button", shouldShowBackButton);
            return this;
        }
//        public ConversationsActivity.Builder addConversationUrl(String conversationUrl){
//            args.putString("extra_conversation_url", conversationUrl);
//            return this;
//        }
        public void show(Context context) {
            Log.d("ConversationsActivity", "show: showing ConversationsActivity");
            context.startActivity(this.intent(context));
        }
        Intent intent(Context context) {
            Log.d("ConversationsActivity", "intent: creating Intent");
            Intent intent = new Intent(context, conversationActivityChild);
            intent.putExtras(this.args);
            return intent;
        }
    }


}