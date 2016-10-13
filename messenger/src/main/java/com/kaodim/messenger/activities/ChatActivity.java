package com.kaodim.messenger.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kaodim.messenger.R;
import com.kaodim.messenger.adapters.ChatAdapter;
import com.kaodim.messenger.models.ChatModel;
import com.kaodim.messenger.models.MessageModel;
import com.kaodim.messenger.recievers.MessageReciever;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kanskiy on 12/10/2016.
 */

public abstract class ChatActivity extends AppCompatActivity {
    private AQuery aq;
    private ChatAdapter adapter;
    private RecyclerView rvMessageThread;
    protected Gson gson;
    String conversationId;
    String incommingMessageAvatar;

    private static final int REQUEST_CODE_SEND_FILE=1;
    public abstract String getMessageThreadURL(String conversationId);
    protected abstract MessageModel fromJsonToMessageModel(String json);
    protected abstract ChatModel fromJsonToChatModel(String json);

    private BroadcastReceiver mMessageReceiver = new MessageReciever() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateMessageList();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        configureActionBar();
        conversationId = getIntent().getStringExtra("extra_id");
        incommingMessageAvatar = getIntent().getStringExtra("extra_incomming_message_avatar");
        String title = getIntent().getStringExtra("extra_name");
        if (TextUtils.isEmpty(title)){
            setTitle("");
        }else{
            setTitle(title);
        }
        aq = new AQuery(this);
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
        initializeRecyclerView();

        aq.id(R.id.etNewMessage).getEditText().clearComposingText();
        aq.id(R.id.llSendNewMessage).clicked(this, "clickedSendMessage");
        aq.id(R.id.llOpenCamera).clicked(this, "onllOpenCameraClicked");
        aq.id(R.id.llattach).clicked(this, "onllAttachClicked");

//        GcmMessageHelper.removeMsgsNoti(mQuotationId, getApplicationContext());
    }

    public void onllOpenCameraClicked(){
        Intent cameraIntent = new Intent(this, PreviewActivity.class);
        cameraIntent.putExtra("intentType",PreviewActivity.INTENT_TYPE_CAMERA);
        startActivityForResult(cameraIntent,REQUEST_CODE_SEND_FILE);
    }
    public void onllAttachClicked(){
        Intent cameraIntent = new Intent(this, PreviewActivity.class);
        cameraIntent.putExtra("intentType",PreviewActivity.INTENT_TYPE_SELECTION);
        startActivityForResult(cameraIntent,REQUEST_CODE_SEND_FILE);
    }
    private void initializeRecyclerView(){
        rvMessageThread = (RecyclerView)findViewById(R.id.rvMessageThread);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        rvMessageThread.setLayoutManager(layoutManager);
        rvMessageThread.setHasFixedSize(true); // Increases the performance
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        rvMessageThread.setItemAnimator(itemAnimator);
        adapter = new ChatAdapter(this, new ArrayList<MessageModel>(),getIntent().getStringExtra("extra_avatar"));
        rvMessageThread.setAdapter(adapter);
    }

    public void callbackPerformSendMessage(String url, String  json, AjaxStatus status){

        if(json != null){
//            mixpanel.track(MixpanelEvents.sent_message); //TODO set mixpanel
            aq.id(R.id.etNewMessage).text("");
            MessageModel newPost = fromJsonToMessageModel(json);
            adapter.addItem(newPost);
            adapter.notifyItemInserted(0);
            rvMessageThread.scrollToPosition(0);
        }else{
            Log.d("send message error", status.getError()) ;
//            mixpanel.track(MixpanelEvents.sent_message_failed); //TODO add mixpanel event
        }
    }

    public void callbackPerformGetThread(String url, String json, AjaxStatus status) {
        if (json != null){
            Log.d("get Thread result", json.toString());
            ChatModel chat = fromJsonToChatModel(json);
            conversationId = chat.getConversationId();
            invalidateOptionsMenu();
            adapter.clear();
            adapter.addItems(chat.getMessages());
            adapter.notifyDataSetChanged();
        }else{
            Log.d("get Thread error", status.getError()) ;
        }
    }

    public void clickedSendMessage(View button){
        String content =  aq.id(R.id.etNewMessage).getText().toString();
        if (TextUtils.isEmpty(content)){return;}
        sendPost(content,null, 0);

    }
    public void sendPost(String content, File attachment, int progress){
        Map<String, Object> params = new HashMap<String, Object>();
        if (!TextUtils.isEmpty(content)){
            params.put("content",content.replaceAll("(\r\n|\n)","<br />"));
        }
        if (attachment!=null){
            params.put("attachment", attachment);
        }
        aq.progress(progress).ajax(getMessageThreadURL(conversationId), params, String.class, this, "callbackPerformSendMessage");
    }


//    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMessageList();
        registerReceiver(mMessageReceiver, new IntentFilter(MessageReciever.FILTER_MESSAGE_RECEIVER));
//        GcmMessageHelper.removeMsgsNoti(mQuotationId, getApplicationContext());
    }
    private void updateMessageList(){
        aq.ajax(getMessageThreadURL(conversationId), String.class, this, "callbackPerformGetThread");
    }
    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mMessageReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_CANCELED && data!=null){
            Toast.makeText(this, data.getStringExtra("reason"), Toast.LENGTH_LONG).show();
            return;
        }
        if (resultCode!= RESULT_OK){
            return;
        }

        switch (requestCode){
            case REQUEST_CODE_SEND_FILE:
                String resultCaption = data.getStringExtra("resultCaption");
                String fileUrl = data.getStringExtra("resultFilePath");
                if (fileUrl!=null) {
                    sendPost(resultCaption, new File(fileUrl), R.id.llProgress);
                }
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Picasso.with(this).cancelTag(this);
    }


    //Here ActionBar
    private void configureActionBar(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
