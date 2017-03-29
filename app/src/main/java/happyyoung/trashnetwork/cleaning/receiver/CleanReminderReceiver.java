package happyyoung.trashnetwork.cleaning.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.model.Trash;
import happyyoung.trashnetwork.cleaning.service.MqttService;
import happyyoung.trashnetwork.cleaning.ui.activity.TrashInfoActivity;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-29
 */
public class CleanReminderReceiver extends BroadcastReceiver {
    private static final int CLEAN_REMINDER_NOTIFICATION_ID = 0x233333;
    private static final String JSON_KEY_TRASH_ID = "trash_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            JsonElement element = new JsonParser().parse(intent.getStringExtra(MqttService.BUNDLE_KEY_MESSAGE));
            if(!element.getAsJsonObject().has(JSON_KEY_TRASH_ID))
                return;
            long trashId = element.getAsJsonObject().get(JSON_KEY_TRASH_ID).getAsLong();
            Trash t = GlobalInfo.findTrashById(trashId);
            if(t == null)
                return;

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(context.getString(R.string.cleaning_reminder));
            mBuilder.setContentText(String.format(context.getString(R.string.cleaning_reminder_content_format), trashId));
            mBuilder.setSmallIcon(R.drawable.ic_delete_32dp);
            mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.addLine(t.getDescription());
            mBuilder.setStyle(inboxStyle);

            Intent notifyIntent = new Intent(context, TrashInfoActivity.class);
            notifyIntent.putExtra(TrashInfoActivity.BUNDLE_KEY_TRASH_ID, trashId);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(notifyPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(CLEAN_REMINDER_NOTIFICATION_ID, mBuilder.build());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}