package com.kaodim.messenger.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;
import com.kaodim.messenger.R;
import com.kaodim.messenger.models.Conversation;
import com.kaodim.messenger.models.ConversationModel;
import com.kaodim.messenger.tools.Blur;
import com.kaodim.messenger.tools.CircleTransform;
import com.kaodim.messenger.tools.TextUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;

/**
 * Created by Kanskiy on 11/10/2016.
 */
@Deprecated
public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {
    private final int TYPE_ITEM=1;
    private final int TYPE_FOOTER=2;

    private Context mContetx;
    private AQuery aq;
    private ArrayList<Conversation> conversations;
    private boolean shouldShowFooter;

    private OnItemClickListener onClickListener;
    Transformation blurTransformation;

    public ConversationsAdapter(final Context context, ArrayList<Conversation> conversations, OnItemClickListener onClickListener) {
        mContetx = context;
        this.conversations = conversations;
        this.onClickListener =onClickListener;
        blurTransformation = new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                Bitmap blurred = Blur.fastblur(context, source, 10);
                source.recycle();
                return blurred;
            }

            @Override
            public String key() {
                return "blur()";
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View container;
        public ViewHolder(View container) {
            super(container);
            this.container = container;
        }
    }
    public interface OnItemClickListener{
        public void onItemClick(int position,Conversation conversation);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=null;
        switch (viewType){
            case TYPE_ITEM:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_conversation, parent, false);
                break;
            case TYPE_FOOTER:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_list_footer, parent, false);
                break;
        }
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        aq = new AQuery(holder.container);
        if (position==conversations.size()){
            if (shouldShowFooter){
                aq.id(R.id.pbLoadingMore).visible();
            }else{
                aq.id(R.id.pbLoadingMore).invisible();
            }
            return ;
        }
        final Conversation conversation = conversations.get(position);
        if (conversation.message!=null){
            aq.id(R.id.tvMessageName).text(conversation.message.name);

            if (conversation.message.content!=null){
                aq.id(R.id.tvMessageContent).text(conversation.message.content.text);
            }




        }

        String date = DateUtils.getRelativeDateTimeString(mContetx,
                conversation.message.updated_at.getTime(),
                DateUtils.DAY_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL)
                .toString();

        aq.id(R.id.tvMessageDate).text(date);
        if(conversation.unread_count>0) {
            aq.id(R.id.tvPostsCount).text(Integer.toString(conversation.unread_count));
            aq.id(R.id.llNewMessagesCountBackground).visibility(View.VISIBLE);
        }else{
            aq.id(R.id.tvPostsCount).text("");
            aq.id(R.id.llNewMessagesCountBackground).visibility(View.GONE);
        }


//        Picasso.with(mContetx)
//                .load(conversation.getAvatar())
//                .resize(184,184)
//                .onlyScaleDown()
//                .placeholder(R.drawable.ic_default_avatar)
//                .transform(new CircleTransform())
//                .error(R.drawable.ic_default_avatar)
//                .into(aq.id(R.id.ciProfileImage).getImageView());
        aq.id(holder.container).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(position, conversation);
            }
        });
    }
    public void updateFooter(boolean shouldShowFooter){
        this.shouldShowFooter=shouldShowFooter;
        notifyItemChanged(conversations.size());
    }

    public void addViews(ArrayList<Conversation> messages){
        conversations.addAll(messages);
        notifyDataSetChanged();
    }

    public void clear(){
        conversations.clear();
        notifyDataSetChanged();
    }
    public ArrayList<Conversation> getItems(){
        return conversations;
    }
    @Override
    public int getItemCount() {
        return conversations.size()+1;
    }
    @Override
    public int getItemViewType(int position) {
        if (position==conversations.size()){
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

}
