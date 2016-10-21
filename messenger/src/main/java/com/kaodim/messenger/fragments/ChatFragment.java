package com.kaodim.messenger.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kaodim.messenger.R;
import com.kaodim.messenger.activities.PreviewActivity;
import com.kaodim.messenger.adapters.ChatAdapter;
import com.kaodim.messenger.models.Chat;
import com.kaodim.messenger.models.ChatModel;
import com.kaodim.messenger.models.Message;
import com.kaodim.messenger.models.MessageModel;
import com.kaodim.messenger.recievers.MessageReciever;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Kanskiy on 18/10/2016.
 */

public  class ChatFragment extends Fragment{
    private AQuery aq;
    private ChatAdapter adapter;
    private RecyclerView rvMessageThread;
    protected Gson gson;
    String conversationId;
    String incommingMessageAvatar;
    String url;
    private static final int REQUEST_CODE_SEND_FILE=1;
    protected String getMessageThreadURL(String conversationId){
        return url;
    }
    protected MessageModel fromJsonToMessageModel(String json){
        return gson.fromJson(json, Message.class);
    }
    protected ChatModel fromJsonToChatModel(String json){
        return gson.fromJson(json, Chat.class);
    }


    private BroadcastReceiver mMessageReceiver = new MessageReciever() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateMessageList();
        }
    };

    public static ChatFragment newInstance(String conversationId, String incommingMessageName, String incommingMessageAvatar, String url) {
        ChatFragment fragment = new ChatFragment();
        Bundle b = new Bundle();
        b.putSerializable("extra_chat_model_class", ChatModel.class );
        b.putString("extra_id",conversationId );
        b.putString("extra_name",incommingMessageName );
        b.putString("extra_incomming_message_avatar",incommingMessageAvatar );
        b.putString("extra_chat_url",url);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        Bundle b = getArguments();
        conversationId = b.getString("extra_id");
        if (android.text.TextUtils.isEmpty(conversationId)){
            Log.d("A-Messenger", "not chat id given");
            return rootView;
        }

        url = b.getString("extra_chat_url");
        if (android.text.TextUtils.isEmpty(url)){
            Log.d("A-Messenger", "not chat url given");
            return rootView;
        }
        url.replace("@",conversationId); //Here adding conversaionId to url

        incommingMessageAvatar = b.getString("extra_incomming_message_avatar");

        String title = b.getString("extra_name");
        if (TextUtils.isEmpty(title)){
            getActivity().setTitle("");
        }else{
            getActivity().setTitle(title);
        }

        aq = new AQuery(rootView);
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
        initializeRecyclerView(rootView, incommingMessageAvatar);
        aq.id(R.id.etNewMessage).getEditText().clearComposingText();
        aq.id(R.id.llSendNewMessage).clicked(this, "clickedSendMessage");
        aq.id(R.id.llOpenCamera).clicked(this, "onllOpenCameraClicked");
        aq.id(R.id.llattach).clicked(this, "onllAttachClicked");
        updateMessageList();
        return rootView;
    }
    public void onllOpenCameraClicked(){
        Intent cameraIntent = new Intent(getContext(), PreviewActivity.class);
        cameraIntent.putExtra("intentType",PreviewActivity.INTENT_TYPE_CAMERA);
        startActivityForResult(cameraIntent,REQUEST_CODE_SEND_FILE);
    }
    public void onllAttachClicked(){
        Intent cameraIntent = new Intent(getContext(), PreviewActivity.class);
        cameraIntent.putExtra("intentType",PreviewActivity.INTENT_TYPE_SELECTION);
        startActivityForResult(cameraIntent,REQUEST_CODE_SEND_FILE);
    }
    private void initializeRecyclerView(View container, String avatar){
        rvMessageThread = (RecyclerView)container.findViewById(R.id.rvMessageThread);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        rvMessageThread.setLayoutManager(layoutManager);
        rvMessageThread.setHasFixedSize(true); // Increases the performance
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        rvMessageThread.setItemAnimator(itemAnimator);
        adapter = new ChatAdapter(getContext(), new ArrayList<MessageModel>(),avatar);
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

    private void updateMessageList(){
        aq.ajax(getMessageThreadURL(conversationId), String.class, this, "callbackPerformGetThread");
    }
    @Override
    public void onPause() {
        super.onPause();
        try {
            getContext().unregisterReceiver(mMessageReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(mMessageReceiver, new IntentFilter(MessageReciever.FILTER_MESSAGE_RECEIVER));
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Picasso.with(getContext()).cancelTag(this);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_CANCELED && data!=null){
            Toast.makeText(getContext(), data.getStringExtra("reason"), Toast.LENGTH_LONG).show();
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
}
