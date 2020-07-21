package com.aryan.anyservice.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.aryan.anyservice.ui.dashboard.DashboardViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import de.timroes.axmlrpc.XMLRPCException;
import helper.AnyserviceNotification;
import helper.OdooRPC;
import helper.Order;

public class NotificationsViewModel extends ViewModel {

    private MutableLiveData<List<AnyserviceNotification>> notifications;
    OdooRPC odooRPC;
    String uid;

    public NotificationsViewModel() {
        notifications = new MutableLiveData<>();
    }

    public LiveData<List<AnyserviceNotification>> getNotification(Context context) {
        if(uid==null){
            SharedPreferences sp = context.getSharedPreferences("odoologin", Context.MODE_PRIVATE);
            uid = sp.getString("login",null);
        }

        HashMap map = new HashMap<String,Object>();
        map.put("method","get_user_notification");
        map.put("read",true);
        map.put("model","anyservice.notification");
        map.put("login",uid);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
        return notifications;
    }
    class AsyncOdooRPCcall extends AsyncTask<HashMap<String,Object>, ProgressBar,HashMap<String,Object>> {

        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String, Object>... hashMaps) {
            HashMap<String, Object> result=null;
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
                String model = (String) hashMaps[0].get("model");
                hashMaps[0].remove("method");
                hashMaps[0].remove("model");
                result = (HashMap) odooRPC.callOdoo(model, method, hashMaps[0]);

                if (result.get("result").equals("Success")) {
                    List<AnyserviceNotification> notificationslist = new ArrayList<>();
                    Object[] objects = (Object[]) result.get("notification");
                    for (Object object : objects) {
                        HashMap<String, String> notificationResp = (HashMap<String, String>) object;
                        AnyserviceNotification notification = new AnyserviceNotification();
                        notification.setTitle(notificationResp.get("title"));
                        notification.setMessage(notificationResp.get("message"));
                        notification.setModel(notificationResp.get("model"));
                        notification.setDate(notificationResp.get("date"));
                        notification.setRecord(String.valueOf(notificationResp.get("record")));
                        notificationslist.add(notification);
                    }
                    notifications.postValue(notificationslist);
                }
            }catch (Exception e){

            }

            return result;
        }
    }

}