package com.aryan.anyservice.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;
import helper.ServiceDetails;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<Double> walletamount;
    private MutableLiveData<Boolean> isAgent;
    private MutableLiveData<Boolean> activeAgent;
    private MutableLiveData<String> toastText;

    public void setRadius(int radius,boolean unlimited_distance,double deliverycharge) {
        HashMap map = new HashMap<String,String>();
        map.put("method","update_user_details");
        map.put("model","res.partner");
        map.put("login",uid);
        map.put("radius",radius);
        map.put("unlimted_distance",unlimited_distance);
        map.put("unlimted_distance_charge",deliverycharge);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
        this.radius.postValue(radius);
    }
    public void setActive(boolean active) {
        HashMap map = new HashMap<String,String>();
        map.put("method","update_user_details");
        map.put("model","res.partner");
        map.put("login",uid);
        map.put("active_mode",active);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
        this.activeAgent.postValue(active);
    }

    private MutableLiveData<Integer> radius;

    public MutableLiveData<Double> getDeliveryCharge() {
        return deliveryCharge;
    }

    public MutableLiveData<Boolean> getUnlimitedDistance() {
        return unlimitedDistance;
    }

    private MutableLiveData<Double> deliveryCharge;
    private MutableLiveData<Boolean> unlimitedDistance;
    private MutableLiveData<List> categories;
    private MutableLiveData<List<ServiceDetails>> services;
    OdooRPC odooRPC;
    String uid;


    public HomeViewModel( ){
        mText = new MutableLiveData<>();
        isAgent = new MutableLiveData<>();
        activeAgent = new MutableLiveData<>();
        radius = new MutableLiveData<>();
        unlimitedDistance = new MutableLiveData<>();
        deliveryCharge = new MutableLiveData<>();
        categories = new MutableLiveData<>();
        services = new MutableLiveData<>();
        toastText = new MutableLiveData<>();
        walletamount = new MutableLiveData<>();
    }

    public LiveData<String> getText(Context context) {
        if(uid==null) {
            SharedPreferences sp = context.getSharedPreferences("odoologin", Context.MODE_PRIVATE);
            uid = sp.getString("login", null);
        }


        HashMap map = new HashMap<String,String>();
        map.put("method","get_user_details");
        map.put("model","res.partner");
        map.put("login",uid);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);

        return mText;
    }
    public LiveData<List> getCategories( ){
        HashMap map = new HashMap<String,String>();
        map.put("method","get_categories");
        map.put("model","product.category");
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);


        return categories;
    }


    public void updateServices(HashMap<String,String> hashMap,int offset,int limit,String order){

        HashMap map = new HashMap<String,String>();
        map.put("method","search_services");
        map.put("model","res.partner");
        map.put("login",uid);
        map.put("offset",offset);
        map.put("limit",limit);
        map.put("order",order);
        for (Map.Entry<String,String> entry:hashMap.entrySet()) {
            map.put(entry.getKey(),entry.getValue());
        }

        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);

    }
    public LiveData<List<ServiceDetails>> getServices() {

        return services;
    }

    public MutableLiveData<Integer> getRadius() {
        return radius;
    }

    public MutableLiveData<String> getToastText() {
        return toastText;
    }

    public MutableLiveData<Boolean> getIsAgent() {
        return isAgent;
    }

    public MutableLiveData<Boolean> getActiveAgent() {
        return activeAgent;
    }

    public MutableLiveData<Double> getWalletamount() {
        return walletamount;
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
                    if (method.equals("get_categories")) {
                        Object[] objects = (Object[]) result.get("categories");
                        List catgoriesResult = Arrays.asList(objects);
                        categories.postValue(catgoriesResult);

                    } else if (method.equals("get_user_details")) {
                        mText.postValue("Welcome " + result.get("name").toString());
                        walletamount.postValue(Double.parseDouble(String.valueOf(result.get("balance"))));
                        deliveryCharge.postValue(Double.parseDouble(String.valueOf(result.get("delivery_charge"))));
                        isAgent.postValue((Boolean) result.get("agent"));
                        activeAgent.postValue((Boolean) result.get("active"));
                        unlimitedDistance.postValue((Boolean) result.get("unlimted_distance"));
                        radius.postValue(Integer.valueOf(String.valueOf(result.get("radius"))));

                    } else if (method.equals("search_services")) {
                        Object[] objects = (Object[]) result.get("services");
                        List<ServiceDetails> list = new ArrayList<>();
                        for (Object obj : objects) {
                            HashMap<String, String> service = (HashMap<String, String>) obj;
                            ServiceDetails serviceDetails = new ServiceDetails();
                            serviceDetails.setId(Integer.parseInt(String.valueOf(service.get("id"))));
                            serviceDetails.setAgent_id(Integer.parseInt(String.valueOf(service.get("agent_id"))));
                            serviceDetails.setCompany(service.get("company"));
                            serviceDetails.setVerified(Boolean.parseBoolean(String.valueOf(service.get("verified"))));
                            serviceDetails.setName(service.get("name"));
                            serviceDetails.setCategory(service.get("category"));
                            serviceDetails.setPrice(Double.parseDouble(String.valueOf(service.get("price"))));
                            serviceDetails.setDeliveryCost(Double.parseDouble(String.valueOf(service.get("charge"))));
                            serviceDetails.setBalance(Double.parseDouble(String.valueOf(service.get("balance"))));
                            serviceDetails.setMeasurable(Boolean.parseBoolean(String.valueOf(service.get("is_measurable"))));
                            serviceDetails.setDescription(String.valueOf(service.get("description")));
                            if (service.get("rating") != null) {
                                Object rating = service.get("rating");
                                serviceDetails.setRating(Float.parseFloat(rating.toString()));
                            }
                            Object image = service.get("image");
                            if (image.getClass() != Boolean.class)
                                serviceDetails.setIcon(service.get("image"));
                            list.add(serviceDetails);
                        }
                            services.postValue(list);


                    } else if (method.equals("update_user_details")) {
                        toastText.postValue("Updated successfully");

                    }
                }
            }catch(Exception e){

            }

            return result;
        }
    }
}