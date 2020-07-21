package com.aryan.anyservice;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import de.timroes.axmlrpc.XMLRPCException;
import helper.AnyserviceNotification;
import helper.OdooRPC;

public class NotifyService extends Service {


    private PowerManager.WakeLock mWakeLock;
    OdooRPC odooRPC;
    String uid;
    boolean running=false;
    NotificationChannel channel=null;

    /**
     * Simply return null, since our Service will not be communicating with
     * any other components. It just does its work silently.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("InvalidWakeLockTag")
    private void handleIntent() {
        running=true;
        try {
            // obtain the wake lock
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Partial");
            mWakeLock.acquire(10*60*1000L /*10 minutes*/);
        }catch (Exception e) {
            e.printStackTrace();
        }

        // check the global background data setting
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (!cm.getBackgroundDataSetting()) {
            stopSelf();
            return;
        }

        // do the actual work, in a separate thread
        new PollTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class PollTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            new Thread(new Runnable() {
                @Override
                public void run() {

            int mId = 2;
            String channelId = "anyservice_channel";
            while(true) {
                try {
                    if (odooRPC == null) {
                        odooRPC = new OdooRPC();
                        try {
                            odooRPC.login();
                        } catch (XMLRPCException e) {
                            e.printStackTrace();
                        }
                    }
                    if (uid == null) {
                        SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
                        uid = sp.getString("login", null);
                    }
                    HashMap map = new HashMap<String, String>();
                    map.put("login", uid);
                    final HashMap<String, Object> result = (HashMap<String, Object>) odooRPC.callOdoo("anyservice.notification", "get_user_notification", map);

                    if (result.get("result").equals("Success")) {
                        List<AnyserviceNotification> notifications = new ArrayList<>();
                        Object[] objects = (Object[]) result.get("notification");
                        if (objects != null) {
                            for (Object object : objects) {
                                HashMap<String, String> notificationResp = (HashMap<String, String>) object;
    //                            new AlertDialog.Builder(getApplicationContext())
    //                                    .setTitle(notificationResp.get("title"))
    //                                    .setMessage(notificationResp.get("message"))
    //                                    .setIcon(R.mipmap.any_service_icon_foreground)
    //                                    .setPositiveButton("Ok",null)
    //                                    .show();
                                AnyserviceNotification notification = new AnyserviceNotification();
                                notification.setTitle(notificationResp.get("title"));
                                notification.setMessage(notificationResp.get("message"));
                                notification.setModel(notificationResp.get("model"));
                                notification.setRecord(String.valueOf(notificationResp.get("record")));
                                notifications.add(notification);

                                Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);
                                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                                //Set up the notification
                                NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                                bigText.bigText(notification.getMessage());
                                bigText.setBigContentTitle(notification.getTitle());
                                bigText.setSummaryText(notification.getTitle());
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
                                builder.setSmallIcon(R.mipmap.any_service_icon_foreground)
                                        .setTicker(notification.getTitle())
                                        .setContentTitle("Anyservice")
                                        .setContentText(notification.getMessage())
                                        .setPriority(Notification.PRIORITY_HIGH)
                                        .setContentIntent(contentIntent)
                                        .setAutoCancel(true)
                                        .setStyle(bigText);
                                //At most three action buttons can be added
                                //.addAction(android.R.drawable.ic_menu_camera, "Action 1", contentIntent)
                                //.addAction(android.R.drawable.ic_menu_compass, "Action 2", contentIntent)
                                //.addAction(android.R.drawable.ic_menu_info_details, "Action 3", contentIntent)
                                //                            .setAutoCancel(true).build();

                                //Show the notification
                                NotificationManager mNotificationManager =
                                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                // mId allows you to update the notification later on.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    if(channel==null) {
                                        channel = new NotificationChannel(channelId, "Anyservice Main", NotificationManager.IMPORTANCE_HIGH);
                                    }
                                    mNotificationManager.createNotificationChannel(channel);
                                }
                                mNotificationManager.notify(mId, builder.build());
                                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                                    startForeground(1, builder.build());
                                }
                                mId++;
                            }
                        }

                    }
                    Thread.sleep(10000);
                }catch(Exception e){
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        stopSelf();
                    }

                }
            }
                }
            }).start();
            return "Done";

        }

        /**
         * In here you should interpret whatever you fetched in doInBackground
         * and push any notifications you need to the status bar, using the
         * NotificationManager. I will not cover this here, go check the docs on
         * NotificationManager.
         *
         * What you HAVE to do is call stopSelf() after you've pushed your
         * notification(s). This will:
         * 1) Kill the service so it doesn't waste precious resources
         * 2) Call onDestroy() which will release the wake lock, so the device
         *    can go to sleep again and save precious battery.
         */
        @Override
        protected void onPostExecute(String notifications) {

        }
    }

    /**
     * This is deprecated, but you have to implement it if you're planning on
     * supporting devices with an API level lower than 5 (Android 2.0).
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        if (!running)
        handleIntent();
    }

    /**
     * This is called on 2.0+ (API level 5 or higher). Returning
     * START_NOT_STICKY tells the system to not restart the service if it is
     * killed because of poor resource (memory/cpu) conditions.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!running)
        handleIntent();
        return START_STICKY;
    }

    /**
     * In onDestroy() we release our wake lock. This ensures that whenever the
     * Service stops (killed for resources, stopSelf() called, etc.), the wake
     * lock will be released.
     */
    public void onDestroy() {
        running=false;
        super.onDestroy();
        mWakeLock.release();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        int mId = 1;
        String channelId = "anyservice_channel";
        //Set up the notification
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("Anyservice is running in Background");
        bigText.setBigContentTitle("Anyservice");
        bigText.setSummaryText("Anyservice");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.any_service_icon_foreground)
                .setTicker("Anyservice")
                .setContentTitle("Anyservice")
                .setContentText("Anyservice is running in Background")
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setStyle(bigText);
//        Notification notification = builder.setOngoing(true).build();
        //At most three action buttons can be added
        //.addAction(android.R.drawable.ic_menu_camera, "Action 1", contentIntent)
        //.addAction(android.R.drawable.ic_menu_compass, "Action 2", contentIntent)
        //.addAction(android.R.drawable.ic_menu_info_details, "Action 3", contentIntent)
        //                            .setAutoCancel(true).build();

        //Show the notification
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(channel==null) {
                channel = new NotificationChannel(channelId, "Anyservice Main", NotificationManager.IMPORTANCE_HIGH);
            }
//            NotificationChannel channel = new NotificationChannel(channelId, "Anyservice", NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }
        mNotificationManager.notify(mId, builder.build());
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            startForeground(1, builder.build());
        }
    }
}