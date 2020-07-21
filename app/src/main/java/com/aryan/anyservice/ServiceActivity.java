package com.aryan.anyservice;

import adaptor.ServiceDetailsAdaptor;
import androidx.appcompat.app.AppCompatActivity;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;
import helper.ServiceDetails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aryan.anyservice.ui.home.HomeViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceActivity extends AppCompatActivity {
    int serviceId;
    int agentId;
    OdooRPC odooRPC;
    ListView servicesListView;
    TextView company,categories,address,totalCost;
    RatingBar rating;
    String uid;
    ImageView icon;
    Button checkoutButton,backButton;
    int radius = 5;

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);


        SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
        uid = sp.getString("login",null);

        serviceId = getIntent().getIntExtra("id",0);
        agentId = getIntent().getIntExtra("agent_id",0);
        servicesListView = findViewById(R.id.listview);
        company = findViewById(R.id.company);
        categories = findViewById(R.id.category);
        address = findViewById(R.id.address);
        rating = findViewById(R.id.rating);
        icon =findViewById(R.id.activityicon);
        checkoutButton = findViewById(R.id.checkout_button);
        backButton = findViewById(R.id.back_button);
        totalCost = findViewById(R.id.total_cost);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap map = new HashMap<String,String>();
                map.put("method","get_user_services");
                map.put("agent_id",agentId);
                map.put("model","res.partner");
                map.put("login",uid);

                AsyncOdooRPCcall task = new AsyncOdooRPCcall();
                task.execute(map);
            }
        }).start();

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<ServiceDetails> serviceDetailsArrayList = new ArrayList<>();
                double price=0,visitCost=0.0,prev_cost=0.0;
                for (int i=0;i<servicesListView.getAdapter().getCount();i++){
                    ServiceDetails serviceDetails = (ServiceDetails) servicesListView.getAdapter().getItem(i);
                    if(serviceDetails.isSelected()) {
                        serviceDetailsArrayList.add(serviceDetails);
                        price += serviceDetails.getTotal_price();
                        if(serviceDetails.getDeliveryCost()>0){
                            visitCost = serviceDetails.getDeliveryCost();
                        }
                        prev_cost = serviceDetails.getBalance();
                    }
                }
                Intent intent=new Intent(ServiceActivity.this,YourOrderActivity.class);
                intent.putExtra("visitPrice",visitCost);
                intent.putExtra("pending",prev_cost);
                intent.putExtra("price",price);
                intent.putExtra("company",company.getText().toString());
                intent.putExtra("totalPrice",price+visitCost+prev_cost);
                intent.putExtra("services",serviceDetailsArrayList);
                startActivity(intent);
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    class AsyncOdooRPCcall extends AsyncTask<HashMap<String,Object>, ProgressBar,HashMap<String,Object>> {

        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String, Object>... hashMaps) {
            HashMap<String,Object> result=null;
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
                    if (method.equals("get_user_services")) {
                        Object[] objects = (Object[]) result.get("services");
                        final List<ServiceDetails> list = new ArrayList<>();
                        for (Object obj : objects) {
                            HashMap<String, String> service = (HashMap<String, String>) obj;
                            ServiceDetails serviceDetails = new ServiceDetails();
                            serviceDetails.setId(Integer.parseInt(String.valueOf(service.get("id"))));
                            serviceDetails.setAgent_id(Integer.parseInt(String.valueOf(service.get("agent_id"))));
                            serviceDetails.setName(service.get("name"));
                            serviceDetails.setCategory(service.get("category"));
                            serviceDetails.setPrice(Double.parseDouble(String.valueOf(service.get("price"))));
                            serviceDetails.setDeliveryCost(Double.parseDouble(String.valueOf(service.get("charge"))));
                            serviceDetails.setBalance(Double.parseDouble(String.valueOf(service.get("balance"))));
                            serviceDetails.setMeasurable(Boolean.parseBoolean(String.valueOf(service.get("is_measurable"))));
                            if (serviceDetails.getId() == serviceId) {
                                serviceDetails.setSelected(true);
                            }
                            list.add(serviceDetails);
                        }
                        final HashMap<String, Object> finalResult = result;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                company.setText(finalResult.get("company").toString());
                                rating.setRating(Float.parseFloat(String.valueOf(finalResult.get("rating"))));
                                address.setText(finalResult.get("address").toString());
                                categories.setText(finalResult.get("categories").toString());
                                try {
                                    byte[] decodedString = Base64.decode(finalResult.get("image").toString(), Base64.DEFAULT);
                                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    icon.setImageBitmap(decodedBitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                ServiceDetailsAdaptor serviceDetailsAdaptor = new ServiceDetailsAdaptor(getApplicationContext(), R.layout.select_service_list_item, list);
                                servicesListView.setAdapter(serviceDetailsAdaptor);
                                serviceDetailsAdaptor.registerDataSetObserver(new DataSetObserver() {
                                    @Override
                                    public void onChanged() {
                                        ArrayList<ServiceDetails> serviceDetailsArrayList = new ArrayList<>();
                                        double price = 0, visitCost = 0.0, prev_cost = 0.0;
                                        for (int i = 0; i < servicesListView.getAdapter().getCount(); i++) {
                                            ServiceDetails serviceDetails = (ServiceDetails) servicesListView.getAdapter().getItem(i);
                                            if (serviceDetails.isSelected()) {
                                                serviceDetailsArrayList.add(serviceDetails);
                                                price += serviceDetails.getTotal_price();
                                                if (serviceDetails.getDeliveryCost() > 0) {
                                                    visitCost = serviceDetails.getDeliveryCost();
                                                }
                                                prev_cost = serviceDetails.getBalance();
                                            }
                                        }
                                        if (price > 0) {
                                            totalCost.setVisibility(View.VISIBLE);
                                            if (visitCost > 0) {
                                                if (prev_cost > 0) {
//                                                Toast.makeText(getApplicationContext(),"Previous Bill Cost will include in Delivery Cost.",Toast.LENGTH_LONG).show();
                                                    totalCost.setText("Total = " + price + " + " + visitCost + "(Visiting Cost)" + " + " + prev_cost + "(Pending Amount) =  Rs. " + (price + visitCost + prev_cost));
                                                } else {
                                                    totalCost.setText("Total = " + price + " + " + visitCost + "(Visiting Cost) =  Rs. " + (price + visitCost));
                                                }
                                            } else {
                                                if (prev_cost > 0) {
//                                                Toast.makeText(getApplicationContext(),"Previous Bill Cost will include in Delivery Cost.",Toast.LENGTH_LONG).show();
                                                    totalCost.setText("Total = " + price + " + " + prev_cost + "(Pending Amount) =  Rs. " + (price + prev_cost));
                                                } else {
                                                    totalCost.setText("Total =  Rs. " + price);
                                                }
                                            }
                                            checkoutButton.setEnabled(true);
                                        } else {
                                            totalCost.setText("");
                                            totalCost.setVisibility(View.GONE);
                                            checkoutButton.setEnabled(false);
                                        }
                                    }
                                });

                            }
                        });

                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }
    }
}