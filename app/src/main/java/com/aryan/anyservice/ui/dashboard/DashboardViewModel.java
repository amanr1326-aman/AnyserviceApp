package com.aryan.anyservice.ui.dashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.aryan.anyservice.LoginActivity;
import com.aryan.anyservice.OrderStatusActivity;
import com.aryan.anyservice.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;
import helper.Order;

public class DashboardViewModel extends ViewModel {


    private MutableLiveData<List<Order>> doneOrders;
    private MutableLiveData<List<Order>> openOrders;
    OdooRPC odooRPC;
    String uid;

    public DashboardViewModel() {
        openOrders = new MutableLiveData<>();
        doneOrders = new MutableLiveData<>();
    }


    public MutableLiveData<List<Order>> getDoneServices(Context context) {
        SharedPreferences sp = context.getSharedPreferences("odoologin", Context.MODE_PRIVATE);
        uid = sp.getString("login",null);


        HashMap map = new HashMap<String,String>();
        map.put("method","get_user_orders");
        map.put("model","anyservice.order");
        map.put("login",uid);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
        return doneOrders;
    }

    public MutableLiveData<List<Order>> getOpenServices() {
        HashMap map = new HashMap<String,String>();
        map.put("method","get_user_done_orders");
        map.put("model","anyservice.order");
        map.put("login",uid);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
        return openOrders;
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
                    if (method.equals("get_user_done_orders")) {
                        Object[] objects = (Object[]) result.get("orders");
                        List<Order> list = new ArrayList<>();
                        for (Object obj : objects) {
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
                            orderObj.setCustPhone(order.get("cust_phone"));
                            orderObj.setAgentPhone(order.get("agent_phone"));
                            orderObj.setRating(Float.parseFloat(String.valueOf(order.get("rating"))));
                            try {
                                orderObj.setOrderDate(new SimpleDateFormat("yyyy-mm-dd").parse(order.get("order_date")));
                                orderObj.setFinalDate(new SimpleDateFormat("yyyy-mm-dd").parse(order.get("final_date")));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            list.add(orderObj);
                        }
                        doneOrders.postValue(list);


                    } else if (method.equals("get_user_orders")) {
                        Object[] objects = (Object[]) result.get("orders");
                        List<Order> list = new ArrayList<>();
                        for (Object obj : objects) {
                            HashMap<String, String> order = (HashMap<String, String>) obj;
                            Order orderObj = new Order();
                            orderObj.setId(Integer.parseInt(String.valueOf(order.get("id"))));
                            orderObj.setCustID(Integer.parseInt(String.valueOf(order.get("cust_id"))));
                            orderObj.setAgentID(Integer.parseInt(String.valueOf(order.get("agent_id"))));
                            orderObj.setTotalPrice(Double.parseDouble(String.valueOf(order.get("total_price"))));
                            orderObj.setName(order.get("name"));
                            orderObj.setIcon(order.get("image"));
                            orderObj.setCustName(order.get("cust_name"));
                            orderObj.setAgentName(order.get("agent_name"));
                            orderObj.setDescription(order.get("description"));
                            orderObj.setGpsAddress(order.get("gps_address"));
                            orderObj.setFullAddress(order.get("full_address"));
                            orderObj.setState(order.get("state"));
                            orderObj.setCustPhone(order.get("cust_phone"));
                            orderObj.setAgentPhone(order.get("agent_phone"));
                            orderObj.setRating(Float.parseFloat(String.valueOf(order.get("rating"))));
                            try {
                                orderObj.setOrderDate(new SimpleDateFormat("yyyy-mm-dd").parse(order.get("order_date")));
                                orderObj.setFinalDate(new SimpleDateFormat("yyyy-mm-dd").parse(order.get("final_date")));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            list.add(orderObj);
                        }
                        openOrders.postValue(list);

                    }

                }
            }catch(Exception e){
                Log.e("ODOO RPC :",e.getMessage());

            }

            return result;
        }
    }
}