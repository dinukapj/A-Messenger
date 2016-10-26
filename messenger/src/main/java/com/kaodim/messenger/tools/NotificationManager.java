package com.kaodim.messenger.tools;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import com.kaodim.messenger.R;
import com.kaodim.messenger.activities.ChatActivity;
import com.kaodim.messenger.activities.ConversationsActivity;
import com.kaodim.messenger.database.DatabaseManager;
import com.kaodim.messenger.models.MessagePushModel;
import com.kaodim.messenger.recievers.MessageReciever;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Kanskiy on 24/10/2016.
 */

public class NotificationManager {
    private static final int DEFAULTS = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
    private static final int NOTIFICATION_ID = 6789221;

    public static void addNotification(String conversationId, String sender, String message, Context context){
        Intent broadcastIntent =new Intent(MessageReciever.FILTER_MESSAGE_RECEIVER);
        broadcastIntent.putExtra(MessageReciever.EXTRA_CONVERSATION_ID, conversationId);
        broadcastIntent.putExtra(MessageReciever.EXTRA_SENDER, sender);
        broadcastIntent.putExtra(MessageReciever.EXTRA_MESSAGE, message);
        context.sendBroadcast(broadcastIntent);


        ArrayList<MessagePushModel> pushes = DatabaseManager.insertMessage(new MessagePushModel(conversationId, sender, message), context);
        if (pushes.size()>1){
            sendStackedNotification(pushes, context);
        }else{
            sendSingleMessagePush(pushes.get(0),context);
        }
    }

    private static boolean isMoreThanOneConversation(ArrayList<MessagePushModel> pushModels){
        String firstConversationId=pushModels.get(0).conversationId;
        for (MessagePushModel models : pushModels){
            if (!models.conversationId.equalsIgnoreCase(firstConversationId)){
                return true;
            }
        }
        return false;
    }
    private static void sendSingleMessagePush(MessagePushModel pushModel, Context context){
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, pushModel.conversationId);
        intent.putExtra(ChatActivity.EXTRA_INCOMING_MESSAGE_USER_NAME, pushModel.sender);
        PendingIntent pendingIntent =
                TaskStackBuilder.create(context)
                        .addNextIntent(new Intent(context, AMessenger.getInstance().getParentStackClass()))
                        .addNextIntentWithParentStack(intent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);



        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_vect_default_avatar);
        Notification summaryNotification = new NotificationCompat.Builder(context)
                .setContentTitle(pushModel.sender)
                .setContentText(pushModel.message)
                .setSmallIcon(R.drawable.ic_vect_clip)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setDefaults(DEFAULTS)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(pushModel.message))
                .setPriority(2)
                .build();
        notificationManager.notify(NOTIFICATION_ID, summaryNotification);

    }
    private static void sendStackedNotification(ArrayList<MessagePushModel> pushes, Context context){
        if (pushes.size()==0){
            return;
        }
        Collections.reverse(pushes);
        Intent intent =null;
        if (isMoreThanOneConversation(pushes)){
            intent =  new Intent(context, ConversationsActivity.class);
        }else{
            intent = new Intent(context, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, pushes.get(0).conversationId);
            intent.putExtra(ChatActivity.EXTRA_INCOMING_MESSAGE_USER_NAME, pushes.get(0).sender);
        }


        PendingIntent pendingIntent =
                TaskStackBuilder.create(context)
                        .addNextIntent(new Intent(context, AMessenger.getInstance().getParentStackClass()))
                        .addNextIntentWithParentStack(intent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);




        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_vect_default_avatar);
        Notification summaryNotification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.x_new_messages, pushes.size()+""))
                .setContentText(pushes.get(0).message)
                .setSmallIcon(R.drawable.ic_vect_clip)
                .setLargeIcon(largeIcon)
                .setDefaults(DEFAULTS)
                .setStyle(toInboxStyle(pushes))
                .setContentIntent(pendingIntent)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setPriority(2)
                .build();
        notificationManager.notify(NOTIFICATION_ID, summaryNotification);
    }
    private static NotificationCompat.InboxStyle toInboxStyle(ArrayList<MessagePushModel> pushes){
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (MessagePushModel push : pushes){
            inboxStyle.addLine(push.sender+": "+push.message);
        }
        return inboxStyle;
    }
    public static void removeNotifications(String id, Context context){//HERE Removes  all if conversationId is null
            DatabaseManager.removeFromDatabase(id, context);
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public static void updateNotifications(ArrayList<String> unreadConversations, Context context) {
        if (unreadConversations.size()<1){
            return;
        }
        DatabaseManager.clearDatabaseExcept(unreadConversations, context);
    }
}
