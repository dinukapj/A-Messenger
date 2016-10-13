package com.kaodim.messenger.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.kaodim.messenger.R;
import com.kaodim.messenger.models.ChatModel;
import com.kaodim.messenger.models.MessageModel;
import com.kaodim.messenger.tools.Blur;
import com.kaodim.messenger.tools.CircleTransform;
import com.kaodim.messenger.tools.FileHelper;
import com.kaodim.messenger.tools.RoundedCornersTransform;
import com.kaodim.messenger.tools.TextUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Kanskiy on 12/10/2016.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{
    private LayoutInflater mInflater;
    private ArrayList<MessageModel> messages;
    private String smbdysAvatar;
    private Context mContext;

    public static final int TYPE_MESSAGE_ME = 0;
    public static final int TYPE_MESSAGE_SMBDY = 1;

    Transformation blurTransformation;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View convertView;
        int rowType;


        public ViewHolder(View v, int rowType) {
            super(v);
            convertView = v;
            this.rowType = rowType;
        }
    }

    public ChatAdapter(final Context mContext, ArrayList<MessageModel> posts, String smbdysAvatar) {
        this.mContext =mContext;
        this.messages = posts;
        this.smbdysAvatar = smbdysAvatar;
        mInflater = LayoutInflater.from(mContext);
        blurTransformation = new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                Bitmap blurred = Blur.fastblur(mContext, source, 10);
                source.recycle();
                return blurred;
            }

            @Override
            public String key() {
                return "blur()";
            }
        };
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        View v=null;
        switch (viewType) {
            case TYPE_MESSAGE_ME:
                v =mInflater.inflate(R.layout.item_outgoing_chat_message, parent, false);
                break;
            case TYPE_MESSAGE_SMBDY:
                v =mInflater.inflate(R.layout.item_incomming_chat_message, parent, false);
                break;
        }
        ViewHolder vh = new ViewHolder(v, viewType);
        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getIsOutgoingMessage()){
            return TYPE_MESSAGE_ME;
        }else{
            return TYPE_MESSAGE_SMBDY;
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MessageModel message = messages.get(position);
        if (message==null){
            return;
        }
        final AQuery aq = new AQuery(holder.convertView);
        String date = TextUtils.getDateString(message.getDate().getTime(), aq.getContext());
        if (getItemViewType(position) == TYPE_MESSAGE_SMBDY ){
            Picasso.with(mContext)
                    .load(smbdysAvatar)
                    .placeholder(R.drawable.ic_person_black_24dp)
                    .transform(blurTransformation)
                    .into(aq.id(R.id.ciProfileImage).getImageView(), new Callback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError() {
                            Picasso.with(mContext).load(R.drawable.ic_person_black_24dp).
                                    into(aq.id(R.id.ciProfileImage).getImageView());
                        }
                    });
            aq.id(R.id.tvContent).textColor(ContextCompat.getColor(mContext,R.color.secondary_black));
            aq.id(R.id.llBubble).background(R.drawable.bg_message_bubble_white);
            aq.id(R.id.cardDocument).backgroundColor(ContextCompat.getColor(mContext,R.color.main_background));
            aq.id(R.id.ivClip).backgroundColor(ContextCompat.getColor(mContext,R.color.white));
        }else{
            aq.id(R.id.llBubble).background(R.drawable.bg_message_bubble_blue);
            aq.id(R.id.tvContent).textColor(ContextCompat.getColor(mContext,R.color.white));
            aq.id(R.id.cardDocument).backgroundColor(ContextCompat.getColor(mContext,R.color.white));
            aq.id(R.id.ivClip).backgroundColor(ContextCompat.getColor(mContext,R.color.main_background));


        }

        if (message.getContent()==null){
            return;
        }
        String content = message.getContent().getText();
        if (content!=null){
            aq.id(R.id.tvContent).text(TextUtils.fromHtml(content));
        }else {
            aq.id(R.id.tvContent).clear();
        }
        aq.id(R.id.tvPostDate).text(date);
        if (message.getContent().getAttachment()==null){
            aq.id(R.id.llAttachment).gone();
            return;
        }
        switch (FileHelper.getFileExtensionFromString(message.getContent().getAttachment().getOriginal())) {
            case FileHelper.FILE_DOC:
            case FileHelper.FILE_DOCX:
            case FileHelper.FILE_TXT:
            case FileHelper.FILE_PDF:
                aq.id(R.id.llAttachment).visible();
                aq.id(R.id.cardDocument).visible();
                aq.id(R.id.ivAttachmentImage).gone();
                aq.id(R.id.tvFileName).text(message.getContent().getAttachment().getFileName());
                aq.id(R.id.llAttachment).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+message.getContent().getAttachment().getFileName()).exists()){
                                openFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+message.getContent().getAttachment().getFileName()), mContext, message.getContent().getAttachment().getOriginal());
                                return;
                            }
                            final File file = FileHelper.createFileNamed(message.getContent().getAttachment().getFileName(), Environment.DIRECTORY_DOWNLOADS);
                            aq.progress(new ProgressDialog(mContext)).download(message.getContent().getAttachment().getOriginal(), file, new AjaxCallback<File>() {
                                @Override
                                public void callback(String url, File object, AjaxStatus status) {
                                    super.callback(url, object, status);
                                    openFile(file, mContext, message.getContent().getAttachment().getOriginal());
                                    Toast.makeText(mContext, "Attachment downloaded", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case FileHelper.FILE_JPEG:
            case FileHelper.FILE_JPG:
            case FileHelper.FILE_PNG:
                aq.id(R.id.llAttachment).visible();
                aq.id(R.id.cardDocument).gone();
                aq.id(R.id.ivAttachmentImage).visible();
                Picasso.with(mContext)
                        .load(message.getContent().getAttachment().getThumb())
                        .placeholder(null)
                        .transform(blurTransformation)
                        .into(aq.id(R.id.ivAttachmentImage).getImageView(), new Callback() {
                            @Override
                            public void onSuccess() {
                                Picasso.with(mContext)
                                        .load(message.getContent().getAttachment().getOriginal())
                                        .resize(800, 600)
                                        .centerCrop()
                                        .transform(new RoundedCornersTransform(20,0))
                                        .placeholder(aq.id(R.id.ivAttachmentImage).getImageView().getDrawable())
                                        .into(aq.id(R.id.ivAttachmentImage).getImageView());
                            }
                            @Override
                            public void onError() {
                                Picasso.with(mContext)
                                        .load(message.getContent().getAttachment().getOriginal())
                                        .resize(1000, 1000)
                                        .centerCrop()
                                        .transform(new RoundedCornersTransform(20,0))
                                        .placeholder(aq.id(R.id.ivAttachmentImage).getImageView().getDrawable())
                                        .into(aq.id(R.id.ivAttachmentImage).getImageView());
                                Log.d("Picasso", "error loading thumb: " + message.getContent().getAttachment().getThumb());
                            }
                        });
                aq.id(R.id.ivAttachmentImage).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent intent = new Intent(mContext, ImageViewerActivity.class);
//                        intent.putExtra("currentPosition", 0);
//                        intent.putStringArrayListExtra("photos", new ArrayList<String>(Arrays.asList(new String[]{ message.getContent().getAttachment().getOriginal()})));
//                        mContext.startActivity(intent);
                    }
                });
                break;
            default:
                aq.id(R.id.llAttachment).gone();
                break;
        }
    }

    private void openFile(File file, Context context, String url){
        final String fileExtension = FileHelper.getFileExtensionFromString(url);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        if (FileHelper.isAbleToOpenFile(mime, context)){
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), mime);
            context.startActivity(intent);
            return;
        }
        Toast.makeText(context, "No application to open the file found", Toast.LENGTH_LONG).show();

    }
    public void addItems(ArrayList<? extends MessageModel> messages){
        this.messages.addAll(messages);
    }
    public void addItem(MessageModel message){
        messages.add(0,message);
    }

    public void clear(){
        messages.clear();
    }
    @Override
    public int getItemCount() {
        return messages.size();
    }

}
