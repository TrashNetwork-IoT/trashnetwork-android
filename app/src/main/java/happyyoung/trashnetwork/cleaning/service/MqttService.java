package happyyoung.trashnetwork.cleaning.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class MqttService extends Service implements MqttCallback {
    public static final String BUNDLE_KEY_MESSAGE = "MessageContent";
    public static final String BUNDLE_KEY_TOPIC = "Topic";
    public static final String BUNDLE_KEY_TOPIC_OWNER_ID = "TopicOwnerID";

    private static final String TAG = "MQTT Service";
    private static final int TIME_OUT = 2000;
    private static final int MAX_RETRY = 5;
    
    public static String BROKER_URL;
    public static final String CLIENT_ID_PREFIX = "trashnetwork_mobile_cleaning_user:";
    public static final String USERNAME_PREFIX = "trashnetwork_mobile_cleaning_user:";

    private MqttClient mqttClient;
    private MqttConnectOptions connOpts;
    private boolean exitFlag = false;
    private Thread networkThread;
    private Object mqttAction;
    private Map<String, String> topicActionMap = new HashMap<>();
    private LinkedBlockingQueue<Object> actionQueue = new LinkedBlockingQueue<>();

    public MqttService() throws MqttException {
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);
        connOpts.setConnectionTimeout(10000);
        connOpts.setKeepAliveInterval(30);
        connOpts.setAutomaticReconnect(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        topicActionMap.clear();
        actionQueue.clear();
    }

    public void startWork(long userId, String password) throws MqttException {
        if(mqttClient != null && mqttClient.isConnected())
            return;
        String clientId = CLIENT_ID_PREFIX + userId;
        mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());
        mqttClient.setCallback(this);
        connOpts.setUserName(USERNAME_PREFIX + userId);
        connOpts.setPassword(password.toCharArray());
        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!exitFlag){
                    try {
                        mqttClient.connect(connOpts);
                        Log.i(TAG, "Connected to MQTT broker.");
                        break;
                    } catch (MqttException e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(TIME_OUT);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                while(!exitFlag){
                    try{
                        mqttAction = actionQueue.poll(TIME_OUT, TimeUnit.MILLISECONDS);
                        if(mqttAction == null)
                            continue;
                        final Object mqttActionClone = mqttAction;
                        for(int i = 0; !exitFlag && i < MAX_RETRY + 1; i++){
                            try {
                                if (mqttAction instanceof MqttSubscriptionAction) {
                                    String topic = getFullTopic(((MqttSubscriptionAction) mqttAction).topic,
                                            ((MqttSubscriptionAction) mqttAction).topicOwnerId);
                                    mqttClient.subscribe(topic, ((MqttSubscriptionAction) mqttAction).qos, new IMqttMessageListener() {
                                        @Override
                                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                                            if (((MqttSubscriptionAction) mqttActionClone).action != null && !((MqttSubscriptionAction) mqttActionClone).action.isEmpty()) {
                                                String msg = new String(message.getPayload());
                                                Log.i(TAG, "Message Arrived: " + msg);
                                                Intent intent = new Intent(((MqttSubscriptionAction) mqttActionClone).action);
                                                intent.addCategory(getPackageName());
                                                parseTopic(topic, intent);
                                                intent.putExtra(BUNDLE_KEY_MESSAGE, msg);
                                                sendBroadcast(intent);
                                            }
                                        }
                                    });
                                    Log.i(TAG, "Subscribe on topic <" + topic + "> successfully.");
                                } else if (mqttAction instanceof MqttPublishAction) {
                                    String topic = getFullTopic(((MqttPublishAction) mqttAction).topic,
                                            ((MqttPublishAction) mqttAction).topicOwnerId);
                                    mqttClient.publish(topic, ((MqttPublishAction) mqttAction).message.getBytes(), ((MqttPublishAction) mqttAction).qos,
                                            ((MqttPublishAction) mqttAction).retain);
                                    Log.i(TAG, "Publish on topic <" + topic + "> successfully.");
                                    if (((MqttPublishAction) mqttAction).intent != null) {
                                        sendBroadcast(((MqttPublishAction) mqttAction).intent);
                                    }
                                }
                                mqttAction = null;
                                break;
                            }catch (MqttException e){
                                Log.e(TAG, "", e);
                                Thread.sleep(TIME_OUT);
                            }
                        }
                        if(mqttAction != null){
                            actionQueue.offer(mqttAction, TIME_OUT, TimeUnit.MILLISECONDS);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                networkThread = null;
                if(mqttClient.isConnected()){
                    try {
                        mqttClient.disconnect(TIME_OUT);
                        Log.i(TAG, "Disconnected from MQTT server.");
                    }catch (MqttException me){
                        me.printStackTrace();
                    }
                }
            }
        });
        networkThread.start();
    }

    @Override
    public void onDestroy() {
        exitFlag = true;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(TAG, "Lost connection from MQTT broker.");
        cause.printStackTrace();
        while (!exitFlag) {
            try {
                if(mqttClient.isConnected())
                    break;
                Log.i(TAG, "Trying to reconnect to MQTT broker...");
                mqttClient.connect(connOpts);
                Log.i(TAG, "Reconnected to MQTT broker.");
                break;
            }catch (MqttException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(TIME_OUT);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String msg = new String(message.getPayload());
        Log.i(TAG, "Message Arrived: " + msg);
        Intent intent = new Intent();
        intent.addCategory(getPackageName());
        parseTopic(topic, intent);
        String action = topicActionMap.get(intent.getStringExtra(BUNDLE_KEY_TOPIC));
        if(action != null) {
            intent.putExtra(BUNDLE_KEY_MESSAGE, new String(message.getPayload()));
            intent.setAction(action);
            sendBroadcast(intent);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

    private String getFullTopic(String topic, Long topicOwnerId){
        String result = topic;
        if(topicOwnerId != null)
            result += '/' + topicOwnerId.toString();
        return result;
    }

    private void parseTopic(String fullTopic, Intent intent) throws NumberFormatException {
        if(fullTopic == null || fullTopic.isEmpty())
            return;
        String[] topicSplit = fullTopic.split("/");
        if(topicSplit.length >= 1){
            intent.putExtra(BUNDLE_KEY_TOPIC, topicSplit[0]);
            if(topicSplit.length >= 2) {
                try {
                    intent.putExtra(BUNDLE_KEY_TOPIC_OWNER_ID, Long.valueOf(topicSplit[1]));
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
        }
    }

    public void addMQTTAction(Object action){
        try {
            actionQueue.offer(action, TIME_OUT, TimeUnit.MILLISECONDS);
            if(action instanceof MqttSubscriptionAction){
                if(((MqttSubscriptionAction) action).action != null &&
                        !((MqttSubscriptionAction) action).action.isEmpty()) {
                    topicActionMap.put(((MqttSubscriptionAction) action).topic,
                            ((MqttSubscriptionAction) action).action);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class Binder extends android.os.Binder {
        public MqttService getService(){
            return MqttService.this;
        }
    }

    public static class MqttSubscriptionAction implements Serializable {
        private String topic;
        private Long topicOwnerId;
        private int qos;
        private String action;

        public MqttSubscriptionAction(String topic, @Nullable Long topicOwnerId, int qos, @Nullable String broadcastAction) {
            this.topic = topic;
            this.topicOwnerId = topicOwnerId;
            this.qos = qos;
            this.action = broadcastAction;
        }

    }

    public static class MqttPublishAction implements Serializable {
        private String topic;
        private Long topicOwnerId;
        private int qos;
        private boolean retain = false;
        private String message;
        private Intent intent;

        public MqttPublishAction(String topic, @Nullable Long topicOwnerId, int qos, String message, @Nullable Intent intent) {
            this.topic = topic;
            this.topicOwnerId = topicOwnerId;
            this.qos = qos;
            this.message = message;
            this.intent = intent;
        }
    }

}
