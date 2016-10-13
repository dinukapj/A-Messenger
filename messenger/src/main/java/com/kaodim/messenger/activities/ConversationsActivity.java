package com.kaodim.messenger.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kaodim.messenger.R;
import com.kaodim.messenger.adapters.ConversationsAdapter;
import com.kaodim.messenger.models.ConversationModel;
import com.kaodim.messenger.recievers.MessageReciever;
import com.kaodim.messenger.tools.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kanskiy on 11/10/2016.
 */

public abstract class ConversationsActivity extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener  {


    private final String TAG = getClass().getName();


    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeView;
    private AQuery aq;
    private Context mContext;
    protected Gson gson;
    private final int MESSAGES_THRESHOLD = 10;
    private int currentPage;
    private ConversationsAdapter mAdapter;
    private ArrayList<ConversationModel> mMessages;
    private Boolean isLoading;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;



    private BroadcastReceiver mMessageReceiver = new MessageReciever() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateMessageList();
        }
    };



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
        initSwipteRefreshLayout();
        initRecyclerView();
        isLoading = false;
        currentPage = 0;
//        if (getIntent().getStringExtra("msg") != "") {
//            GcmMessageHelper.mMessageNumber = 1;
//        }
    }
    private void initRecyclerView(){
        recyclerView = (RecyclerView)findViewById(R.id.rvConversations);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mSwipeView.isRefreshing()) {
                    return;
                }
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                    if (!isLoading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            isLoading = true;
                            Log.v("...", "Last Item Wow !");
                            getMessages(currentPage + 1);
                            ((ConversationsAdapter) recyclerView.getAdapter()).updateFooter(true);
                        }
                    }
                }
            }
        });
        recyclerView.setAdapter(new ConversationsAdapter(this,new ArrayList<ConversationModel>(),new ConversationsAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(int position, ConversationModel conversation) {
//                try {
//                    GcmMessageHandler.removeMsgsNoti(message.getServiceQuotationId(), getApplicationContext());
//                }catch (Exception e){
//                    e.printStackTrace();
//                }

                Intent intent = new Intent(mContext, getChatActivityChild());
                intent.putExtra("extra_name", conversation.getName());
                intent.putExtra("extra_id", conversation.getId());
                intent.putExtra("extra_incomming_message_avatar", conversation.getAvatar());
                startActivity(intent);
            }
        }));
    }
    private void initSwipteRefreshLayout(){
        mSwipeView = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshlayout);
        mSwipeView.setOnRefreshListener(this);
        mSwipeView.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent),ContextCompat.getColor(this,R.color.colorPrimary),ContextCompat.getColor(this,R.color.colorAccent));
    }

    public void getMessages(int page) {
        int progressBar=0;
        if (((ConversationsAdapter)recyclerView.getAdapter()).getItemCount()<1){
            progressBar = R.id.progressBar;
        }
        aq.progress(progressBar).ajax(getMessagesUrl(page), String.class, this, "callbackPerformGetMessage");
    }

    private String getMessagesUrl(int page)
    {
        String url =getConversationUrl()+ TextUtils.getPagingParams(MESSAGES_THRESHOLD, page);
        return url;
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
        mSwipeView.post(new Runnable() {
            @Override
            public void run() {
                mSwipeView.setRefreshing(true);
                refresh();
            }
        });
        registerReceiver(mMessageReceiver, new IntentFilter(MessageReciever.FILTER_MESSAGE_RECEIVER));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }


    @Override
    public void onRefresh() {
        refresh();
    }
    private void refresh(){
        if (isLoading){
            aq.ajaxCancel();
            isLoading=false;
            ((ConversationsAdapter) recyclerView.getAdapter()).updateFooter(false);
        }
        getMessages(1);
    }

    private void updateMessageList(){
        aq.ajax(getConversationUrl(), String.class, this, "callbackPerformGetMessage");
    }

    public void callbackPerformGetMessage(String url, String json, AjaxStatus status) {
        if (json != null) {

            ArrayList<ConversationModel> mMessages =fromJsonToConverstionModelArray(json);
            if (mSwipeView.isRefreshing()){
                ((ConversationsAdapter)recyclerView.getAdapter()).clear();
                currentPage=1;
                Log.d("callbackGetMessage", "refresh "+currentPage);

                if (mMessages.size() == 0) {
                    aq.id(R.id.llNoMessages).visible();
                    aq.id(R.id.tvNoItemsTitle).text("");
                    aq.id(R.id.tvNoItemsText).text(getString(R.string.messenger_no_conversations));
                }
                else {
                    aq.id(R.id.llNoMessages).gone();
                }
            }else{
                if (mMessages.size()>0)
                    currentPage++;
                Log.d("callbackGetMessage", "load more "+currentPage);
            }
            ((ConversationsAdapter)recyclerView.getAdapter()).addViews(mMessages);
            Log.d("get messages result", json.toString());
        } else {
            aq.id(R.id.llNoMessages).visible();
            aq.id(R.id.tvNoItemsTitle).text("");
            aq.id(R.id.tvNoItemsText).text(getString(R.string.messenger_HTTP_RANDOM_ERROR));
            Log.d("get messages error", status.getError());
        }
        ((ConversationsAdapter) recyclerView.getAdapter()).updateFooter(false);
        mSwipeView.setRefreshing(false);
        isLoading=false;
    }

    //Here Builder
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