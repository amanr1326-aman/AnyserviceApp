package com.aryan.anyservice;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.core.app.NotificationCompat;
import de.timroes.axmlrpc.XMLRPCException;
import helper.AnyserviceNotification;
import helper.OdooRPC;
import helper.Order;

public class NotifyService extends Service {


    private PowerManager.WakeLock mWakeLock;
    OdooRPC odooRPC;
    String uid;
    boolean running=false;
    NotificationChannel channel=null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("InvalidWakeLockTag")
    private void handleIntent() {
        running=true;
        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Partial");
            mWakeLock.acquire(10*60*1000L /*10 minutes*/);
        }catch (Exception e) {
            e.printStackTrace();
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (!cm.getBackgroundDataSetting()) {
            stopSelf();
            return;
        }

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
                                AnyserviceNotification notification = new AnyserviceNotification();
                                notification.setTitle(notificationResp.get("title"));
                                notification.setMessage(notificationResp.get("message"));
                                notification.setModel(notificationResp.get("model"));
                                notification.setRecord(String.valueOf(notificationResp.get("record")));
                                if(!String.valueOf(notificationResp.get("order")).equals("false")){
                                    Object obj = notificationResp.get("order");
                                    HashMap<String, String> order = (HashMap<String, String>) obj;
                                    Order orderObj = new Order();
                                    orderObj.setId(Integer.parseInt(String.valueOf(order.get("id"))));
                                    orderObj.setCustID(Integer.parseInt(String.valueOf(order.get("cust_id"))));
                                    orderObj.setAgentID(Integer.parseInt(String.valueOf(order.get("agent_id"))));
                                    orderObj.setTotalPrice(Double.parseDouble(String.valueOf(order.get("total_price"))));
                                    orderObj.setName(order.get("name"));
                                    orderObj.setCustName(order.get("cust_name"));
                                    orderObj.setAgentName(order.get("agent_name"));
                                    orderObj.setDescription(order.get("description"));
                                    orderObj.setGpsAddress(order.get("gps_address"));
                                    orderObj.setFullAddress(order.get("full_address"));
                                    orderObj.setState(order.get("state"));
                                    orderObj.setCode(order.get("code"));
                                    orderObj.setCustPhone(order.get("cust_phone"));
                                    orderObj.setAgentPhone(order.get("agent_phone"));
                                    orderObj.setRating(Float.parseFloat(String.valueOf(order.get("rating"))));
                                    orderObj.setInvoicdId(Integer.parseInt(String.valueOf(order.get("invoice_id"))));
                                    orderObj.setOrderDate(order.get("order_date"));
                                    orderObj.setFinalDate(order.get("final_date"));
                                    notification.setOrder(orderObj);
                                }
                                notifications.add(notification);
                                PendingIntent contentIntent=null;

                                if(notification.getOrder()!=null){
                                    Intent notificationIntent = new Intent(getApplicationContext(), OrderStatusActivity.class);
                                    notificationIntent.putExtra("order",notification.getOrder());
                                    contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                }else {
                                    Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);
                                    contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                }

                                NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                                bigText.bigText(notification.getMessage());
                                bigText.setBigContentTitle(notification.getTitle());
                                bigText.setSummaryText(notification.getTitle());
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
                                builder.setSmallIcon(R.mipmap.any_service_icon_foreground)
                                        .setTicker(notification.getTitle())
                                        .setContentTitle("Anyservice")
                                        .setContentText(notification.getMessage())
                                        .setPriority(Notification.PRIORITY_DEFAULT)
                                        .setContentIntent(contentIntent)
                                        .setAutoCancel(true)
                                        .setStyle(bigText);
                                NotificationManager mNotificationManager =
                                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    if(channel==null) {
                                        channel = new NotificationChannel(channelId, "Anyservice Main", NotificationManager.IMPORTANCE_HIGH);
                                    }
                                    mNotificationManager.createNotificationChannel(channel);
                                }
                                mNotificationManager.notify(mId, builder.build());
                                mId++;
                            }
                        }

                    }
                    Thread.sleep(40000);
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


        @Override
        protected void onPostExecute(String notifications) {

        }
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        if (!running)
        handleIntent();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!running)
        handleIntent();
        return START_NOT_STICKY;
    }

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
        String channelId = "anyservice_channel_foreground";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.any_service_icon_foreground)
                .setTicker("Anyservice")
                .setContentTitle("Anyservice")
                .setContentText("Anyservice is running for notification updates.")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel2 = new NotificationChannel(channelId, "Anyservice Foreground", NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel2);
        }
        mNotificationManager.notify(mId, builder.build());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, builder.build());
        }
    }
}