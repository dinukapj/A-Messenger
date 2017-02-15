package com.kaodim.messenger.tools;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kaodim.messenger.R;
import com.kaodim.messenger.models.PushNotificationModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Kanskiy on 14/02/2017.
 */

public class PushNotificationManager {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private final String MESSAGES  = "sp_messages";
    private Gson gson;
    private Context mContext;
    private static final int DEFAULTS = android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.DEFAULT_VIBRATE | android.app.Notification.DEFAULT_SOUND;
    private static final int MESSAGES_NOTIFICATION_ID = 32509385;
    NotificationManagerCompat notificationManager ;


    public PushNotificationManager(Context mContext){
        this.mContext = mContext;
        String prefsName = mContext.getApplicationInfo().packageName+getClass().getName(); // HERE to get a unique sp name
        sp = mContext.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
        editor = sp.edit();
        gson = new Gson();
        notificationManager =
                NotificationManagerCompat.from(mContext);
    }

    private static PushNotificationManager config;

    public static PushNotificationManager getInstance(Context context){
        if (config==null){
            config = new PushNotificationManager(context);
        }
        return config;
    }

    public ArrayList<PushNotificationModel> getNotifications(){
        ArrayList<PushNotificationModel> notifications = gson.fromJson(sp.getString(MESSAGES,""), new TypeToken<ArrayList<PushNotificationModel>>(){}.getType());
        if (notifications== null){
            notifications = new ArrayList<>();
        }
        return notifications;
    }
    private void save(PushNotificationModel... newNotifications){
        ArrayList<PushNotificationModel> savedNotifications = getNotifications();
        if (savedNotifications==null){
            savedNotifications = new ArrayList<>();
        }
        savedNotifications.addAll(new ArrayList<PushNotificationModel>(Arrays.asList(newNotifications)){});
        editor.putString(MESSAGES, gson.toJson(savedNotifications));
        editor.commit();
    }
    public void clear(String... ids){
        ArrayList<PushNotificationModel> savedNotifications = getNotifications();
        for (String id : ids){
            Iterator<PushNotificationModel> pushes = savedNotifications.iterator();
            while (pushes.hasNext()) {
                PushNotificationModel push = pushes.next(); // must be called before you can call i.remove()
                if (push.conversationId.equalsIgnoreCase(id)){
                    pushes.remove();
                }
            }
        }
        editor.putString(MESSAGES, gson.toJson(savedNotifications));
        editor.commit();
        if (savedNotifications.size()==0){
            notificationManager.cancel(MESSAGES_NOTIFICATION_ID);
        }
    }
    public void clear(){
        editor.remove(MESSAGES);
        editor.commit();
        notificationManager.cancel(MESSAGES_NOTIFICATION_ID);
    }
    public void push(PushNotificationModel newPush,String conversationIdExtra, Class<?extends  Activity> singleSenderActivity, Class<?extends  Activity> multipleSendersActivity ){
        save(newPush);
        ArrayList<PushNotificationModel> currentNotification = getNotifications();
        pushStackedNotification(currentNotification,conversationIdExtra, singleSenderActivity, multipleSendersActivity );
    }
    private Intent getSingleSenderIntent(Context mContext, String key, String value, Class<?extends Activity> activity){
        Intent intent = new Intent(mContext, activity);
        intent.putExtra(key, value);
        return intent;
    }
    private Intent getMultipleSenderIntent(Context mContext,  Class<?extends Activity> activity){
        Intent intent = new Intent(mContext, activity);
        return intent;
    }
    private PendingIntent getPendingIntent(Context mContext, Intent intent){
        return TaskStackBuilder.create(mContext)
                .addNextIntentWithParentStack(intent)
                .addNextIntent(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    private void pushStackedNotification(ArrayList<PushNotificationModel> pushes, String conversationIdExtra, Class<?extends Activity> singleSenderActivity, Class<?extends Activity> multipleSendersActivity ){
        if (pushes.size()==0){
            return;
        }
        Collections.reverse(pushes);
        Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.ic_launcher);
        PendingIntent pendingIntent = null;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_messages)
                .setLargeIcon(largeIcon)
                .setDefaults(DEFAULTS)
                .setAutoCancel(true)
                .setContentText(pushes.get(0).message)
                .setNumber(pushes.size())
                .setPriority(2);
        if (pushes.size()>1){
            builder.setStyle(toInboxStyle(pushes))
                    .setContentTitle(mContext.getString(R.string.x_new_messages, pushes.size()+""))
                    .setGroupSummary(true);
        }else{
            builder .setContentTitle(pushes.get(0).sender)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(pushes.get(0).message));
        }
        if (isMoreThanOneConversation(pushes)){
            pendingIntent = getPendingIntent(mContext,getMultipleSenderIntent(mContext, multipleSendersActivity));
        }else{
            pendingIntent = getPendingIntent(mContext,getSingleSenderIntent(mContext,conversationIdExtra, pushes.get(0).conversationId,singleSenderActivity ));
        }
        builder.setContentIntent(pendingIntent);
        Notification summaryNotification = builder.build();
        notificationManager.notify(MESSAGES_NOTIFICATION_ID, summaryNotification);
    }
    private static boolean isMoreThanOneConversation(ArrayList<PushNotificationModel> pushModels){
        String firstConversationId=pushModels.get(0).conversationId;
        for (PushNotificationModel models : pushModels){
            if (models.conversationId==null){
                return true;
            }
            if (!models.conversationId.equalsIgnoreCase(firstConversationId)){
                return true;
            }
        }
        return false;
    }

    private static NotificationCompat.InboxStyle toInboxStyle(ArrayList<PushNotificationModel> pushes){
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (PushNotificationModel push : pushes){
            inboxStyle.addLine(push.sender+": "+push.message);
        }
        return inboxStyle;
    }
}
