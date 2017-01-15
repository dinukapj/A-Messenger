package com.kaodim.messenger.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaodim.messenger.R;
import com.kaodim.messenger.adapters.ConversationsAdapter;
import com.kaodim.messenger.models.Conversation;
import com.kaodim.messenger.recievers.MessageReciever;

import java.util.ArrayList;


/**
 * Created by Kanskiy on 10/01/2017.
 */

public class ConversationsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = getClass().getName();

    RecyclerView recyclerView;
    private ConversationsAdapter adapter;
    private SwipeRefreshLayout mSwipeView;
    private int currentPage;
    private Boolean isLoading;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    protected LinearLayout llNoMessages;
    protected TextView tvNoItemsTitle;
    protected TextView tvNoItemsText;

    private BroadcastReceiver mMessageReceiver = new MessageReciever() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    };

    public interface OnConversationFragmentListener {
        void onConversationSelected(String groupId, String conversationName);
        void getConversationList(int page);
    }


    public static ConversationsFragment newInstance() {
        Bundle args = new Bundle();
        ConversationsFragment fragment = new ConversationsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_conversation, container, false);
        initSwipteRefreshLayout(v);
        initRecyclerView(v);
        configure(v);
        isLoading = false;
        currentPage = 0;
        return v;
    }
    private void configure(View v){
        llNoMessages = (LinearLayout)v.findViewById(R.id.llNoMessages);
        tvNoItemsTitle = (TextView)v.findViewById(R.id.tvNoItemsTitle);
        tvNoItemsText = (TextView)v.findViewById(R.id.tvNoItemsText);
    }

    private void initRecyclerView(View view){
        recyclerView = (RecyclerView)view.findViewById(R.id.rvConversations);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                            if (getActivity()!=null){
                                ((OnConversationFragmentListener)getActivity()).getConversationList(currentPage + 1);
                            }

                            adapter.updateFooter(true);
                        }
                    }
                }
            }
        });
        adapter  = new ConversationsAdapter(getActivity(),new ArrayList<Conversation>(),new ConversationsAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(int position, Conversation conversation) {
                if (getActivity()!=null){
                    ((OnConversationFragmentListener)getActivity()).onConversationSelected(conversation.message.group_id, conversation.message.name);
                }

            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void initSwipteRefreshLayout(View view){
        mSwipeView = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefreshlayout);
        mSwipeView.setOnRefreshListener(this);
        mSwipeView.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorAccent),ContextCompat.getColor(getActivity(),R.color.colorPrimary),ContextCompat.getColor(getActivity(),R.color.colorAccent));
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
        getActivity().registerReceiver(mMessageReceiver, new IntentFilter(MessageReciever.FILTER_MESSAGE_RECEIVER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onRefresh() {
        if (isLoading){
            isLoading=false;
            adapter.updateFooter(false);
        }
        ((OnConversationFragmentListener)getActivity()).getConversationList(1);
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
    public void setErrorMessage(String title, String message){
        if (title==null && message==null){
            llNoMessages.setVisibility(View.GONE);
            return;
        }
        llNoMessages.setVisibility(View.VISIBLE);
        tvNoItemsTitle.setText(title);
        tvNoItemsText.setText(message);
    }

    public void onNewItems( ArrayList<Conversation> mMessages){
        if (mSwipeView.isRefreshing()){
            adapter.clear();
            currentPage=1;
            Log.d("callbackGetMessage", "refresh "+currentPage);

            if (mMessages.size() == 0) {
                setErrorMessage("",getString(R.string.messenger_no_conversations));
            }
            else {
                setErrorMessage(null,null);
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
        setErrorMessage(errorTitle, errorDetails);
        toggleLoading(false);
    }
    private void toggleLoading(boolean shouldShow){
        adapter.updateFooter(shouldShow);
        mSwipeView.setRefreshing(shouldShow);
        isLoading=!shouldShow;
    }
}
