package com.kaodim.messenger.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.androidquery.AQuery;
import com.kaodim.messenger.R;
import com.kaodim.messenger.models.ConversationModel;
import com.kaodim.messenger.tools.Blur;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;

/**
 * Created by Kanskiy on 11/10/2016.
 */

public class ConversationsAdapter extends ArrayAdapter<ConversationModel> {

    public ConversationsAdapter(final Context context, ArrayList<ConversationModel> messages) {
        super(context, 0, messages);
        mContetx = context;
        mConversations = messages;
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
    private Context mContetx;
    private AQuery aq;
    private ArrayList<ConversationModel> mConversations;
    Transformation blurTransformation;



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ConversationModel conversation = mConversations.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContetx).inflate(R.layout.item_conversation, parent, false);
        }

        aq = new AQuery(convertView);
        aq.id(R.id.tvMessageName).text(conversation.getName());
        aq.id(R.id.tvMessageContent).text(conversation.getLastMessage());

        String date = DateUtils.getRelativeDateTimeString(mContetx,
                conversation.getDate().getTime(),
                DateUtils.DAY_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL)
                .toString();
        aq.id(R.id.tvMessageDate).text(date);
        if (conversation.getUnreadMessagesCount()!=0){
            aq.id(R.id.tvPostsCount).text(conversation.getUnreadMessagesCount() + "");
            aq.id(R.id.llNewMessagesCountBackground).visible();
        }else{
            aq.id(R.id.llNewMessagesCountBackground).gone();
        }


        Picasso.with(getContext())
                .load(conversation.getAvatar())
                .placeholder(R.drawable.ic_person_black_24dp)
                .transform(blurTransformation)
                .into(aq.id(R.id.ciProfileImage).getImageView(), new Callback() {
                    @Override
                    public void onSuccess() {
                    }
                    @Override
                    public void onError() {
                        Picasso.with(getContext()).load(R.drawable.ic_person_black_24dp).
                                into(aq.id(R.id.ciProfileImage).getImageView());
                    }
                });
        return convertView;
    }

}