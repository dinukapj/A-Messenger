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

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.kaodim.messenger.R;
import com.kaodim.messenger.adapters.ConversationsAdapter;
import com.kaodim.messenger.models.Chat;
import com.kaodim.messenger.models.Conversation;
import com.kaodim.messenger.models.ConversationModel;
import com.kaodim.messenger.recievers.MessageReciever;
import com.kaodim.messenger.tools.AMessenger;
import com.kaodim.messenger.tools.NotificationManager;

import java.util.ArrayList;

/**
 * Created by Kanskiy on 11/10/2016.
 */

public abstract class  ConversationsActivity extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener  {


    private final String TAG = getClass().getName();


    private RecyclerView recyclerView;
    private ConversationsAdapter adapter;
    private SwipeRefreshLayout mSwipeView;
    private AQuery aq;
    private Context mContext;
    private int currentPage;
    private Boolean isLoading;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;


    public abstract void getMessages(int page);



    private BroadcastReceiver mMessageReceiver = new MessageReciever() {
        @Override
        public void onReceive(Context context, Intent intent) {
          refresh();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        setTitle(getString(R.string.messenger_title_conversation_activity));
        showBackButton();
        aq = new AQuery(this);
        mContext = this;
        initSwipteRefreshLayout();
        initRecyclerView();
        isLoading = false;
        currentPage = 0;
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
                            adapter.updateFooter(true);
                        }
                    }
                }
            }
        });
        adapter  = new ConversationsAdapter(this,new ArrayList<Conversation>(),new ConversationsAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(int position, Conversation conversation) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_CHAT_GROUP_ID, conversation.message.group_id);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void initSwipteRefreshLayout(){
        mSwipeView = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshlayout);
        mSwipeView.setOnRefreshListener(this);
        mSwipeView.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent),ContextCompat.getColor(this,R.color.colorPrimary),ContextCompat.getColor(this,R.color.colorAccent));
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private  void showBackButton(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
    }
    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        registerReceiver(mMessageReceiver, new IntentFilter(MessageReciever.FILTER_MESSAGE_RECEIVER));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }


    @Override
    public void onRefresh() {
        if (isLoading){
            aq.ajaxCancel();
            isLoading=false;
            adapter.updateFooter(false);
        }
        getMessages(1);
    }

    private void refresh(){
        mSwipeView.post(new Runnable() {
            @Override
            public void run() {
                mSwipeView.setRefreshing(true);
                onRefresh();
            }
        });
    }

    public void onNewItems( ArrayList<Conversation> mMessages){
        if (mSwipeView.isRefreshing()){
            adapter.clear();
            currentPage=1;
            Log.d("callbackGetMessage", "refresh "+currentPage);

            if (mMessages.size() == 0) {
                aq.id(R.id.llNoMessages).visible();
                aq.id(R.id.tvNoItemsTitle).text("");
                aq.id(R.id.tvNoItemsText).text(getString(R.string.messenger_no_conversations));
            }
            else {
                aq.id(R.id.llNoMessages).gone();
                if (currentPage==1){
//                    NotificationManager.updateNotifications(getUnreadConversations(mMessages), getApplicationContext());
                }
            }
        }else{
            if (mMessages.size()>0)
                currentPage++;
            Log.d("callbackGetMessage", "load more "+currentPage);
        }
        adapter.addViews(mMessages);
        toggleLoading(false);
    }
    public void onNewItemsFailed(String errorTitle, String errorDetails){
        aq.id(R.id.llNoMessages).visible();
        aq.id(R.id.tvNoItemsTitle).text(errorTitle);
        aq.id(R.id.tvNoItemsText).text(getString(R.string.messenger_HTTP_RANDOM_ERROR));
        toggleLoading(false);
    }
    private void toggleLoading(boolean shouldShow){
        adapter.updateFooter(shouldShow);
        mSwipeView.setRefreshing(shouldShow);
        isLoading=!shouldShow;
    }
//    private ArrayList<String> getUnreadConversations(ArrayList<Conversation> conversations){
//        ArrayList<String> ids = new ArrayList<>();
//        for (Conversation model : conversations){
//            if (model.unread_count>0){
//                ids.add(model.message.id);
//            }
//        }
//        return ids;
//    }
    //Here Builder
    public  static abstract class Builder {
        private  Bundle args;
        public Builder() {
            args  = new Bundle();
        }
        public void show(Context context) {
            Log.d("ConversationsActivity", "show: showing ConversationsActivity");
            context.startActivity(this.buildIntent(context));
        }
        private Intent buildIntent(Context context) {
            Log.d("ConversationsActivity", "intent: creating Intent");
            Intent intent = new Intent(context, getChildActivityClass());
            intent.putExtras(this.args);
            return intent;
        }
    protected abstract Class getChildActivityClass();

    }



}