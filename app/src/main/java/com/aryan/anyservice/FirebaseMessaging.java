package com.aryan.anyservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ProgressBar;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

import androidx.core.app.NotificationCompat;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;

public class FirebaseMessaging extends FirebaseMessagingService {
    int mid=1;
    NotificationChannel channel=null;
    OdooRPC odooRPC;
    String uid;
    String TAG="ANYSERVICEFCM";
    public void onMessageReceived(RemoteMessage remoteMessage) {


        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
        }

    }

    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {

        sendRegistrationToServer(token);
    }



    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        if(uid==null) {
            SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
            uid = sp.getString("login", null);
        }
        HashMap map = new HashMap<String,String>();
        map.put("method","set_token");
        map.put("token",token);
        map.put("login",uid);
        map.put("model","res.partner");
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody) {
        String channelId= getString(R.string.default_notification_channel_id);
        String channelName = getString(R.string.default_notification_channel_name);

        Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(messageBody);
        bigText.setBigContentTitle(title);
        bigText.setSummaryText(title);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.any_service_icon_foreground)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setStyle(bigText);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(channel==null) {
                channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            }
            mNotificationManager.createNotificationChannel(channel);
        }
        mNotificationManager.notify(mid, builder.build());
        mid++;
    }


    class AsyncOdooRPCcall extends AsyncTask<HashMap<String,Object>, ProgressBar,HashMap<String,String>> {

        @Override
        protected HashMap<String,String> doInBackground(HashMap<String, Object>... hashMaps) {
            HashMap<String, String> result=null;
            try {
                if (odooRPC == null) {
                    odooRPC = new OdooRPC();
                    try {
                        odooRPC.login();
                    } catch (XMLRPCException e) {
                        e.printStackTrace();
                    }
                }
                String method = (String) hashMaps[0].get("method");
                hashMaps[0].remove("method");
                result = (HashMap<String, String>) odooRPC.callOdoo("res.partner", method, hashMaps[0]);
                if (method.equals("set_token")) {

                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}
