package happyyoung.trashnetwork.receiver;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.baidu.android.pushservice.PushMessageReceiver;
import com.microsoft.windowsazure.messaging.NotificationHub;

import java.util.List;

import happyyoung.trashnetwork.Application;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-26
 */
public class TestPushMessageReceiver extends PushMessageReceiver {
    /** TAG to Log */
    public static NotificationHub hub = null;
    public static String mChannelId, mUserId;
    public static final String TAG = "TestPushMsgReceiver";

    @Override
    public void onBind(Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {
        String responseString = "onBind errorCode=" + errorCode + " appid="
                + appid + " userId=" + userId + " channelId=" + channelId
                + " requestId=" + requestId;
        Log.d(TAG, responseString);
        mChannelId = channelId;
        mUserId = userId;

        try {
            if (hub == null) {
                hub = new NotificationHub(
                        Application.AZURE_NOTIFICATION_HUB_NAME,
                        Application.AZURE_NOTIFICATION_HUB_CONN_NAME,
                        context);
                Log.i(TAG, "Notification hub initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        registerWithNotificationHubs();
    }

    private void registerWithNotificationHubs() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    hub.registerBaidu(mUserId, mChannelId);
                    Log.i(TAG, "Registered with Notification Hub - '"
                            + Application.AZURE_NOTIFICATION_HUB_NAME + "'"
                            + " with UserId - '"
                            + mUserId + "' and Channel Id - '"
                            + mChannelId + "'");
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @Override
    public void onSetTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onSetTags errorCode=" + errorCode
                + " sucessTags=" + sucessTags + " failTags=" + failTags
                + " requestId=" + requestId;
        Log.d(TAG, responseString);
    }

    @Override
    public void onDelTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onDelTags errorCode=" + errorCode
                + " sucessTags=" + sucessTags + " failTags=" + failTags
                + " requestId=" + requestId;
        Log.d(TAG, responseString);
    }

    @Override
    public void onListTags(Context context, int errorCode, List<String> tags,
                           String requestId) {
        String responseString = "onListTags errorCode=" + errorCode + " tags="
                + tags;
        Log.d(TAG, responseString);
    }

    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        String responseString = "onUnbind errorCode=" + errorCode
                + " requestId = " + requestId;
        Log.d(TAG, responseString);
    }

    @Override
    public void onNotificationClicked(Context context, String title,
                                      String description, String customContentString) {
        String notifyString = "title=\"" + title + "\" description=\""
                + description + "\" customContent=" + customContentString;
        Log.d(TAG, notifyString);
    }

    @Override
    public void onNotificationArrived(Context context, String s, String s1, String s2) {

    }

    @Override
    public void onMessage(Context context, String message,
                          String customContentString) {
        String messageString = "message=\"" + message + "\" customContentString=" + customContentString;
        Log.d(TAG, messageString);
    }
}
