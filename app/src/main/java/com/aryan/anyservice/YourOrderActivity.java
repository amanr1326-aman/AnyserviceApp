package com.aryan.anyservice;

import adaptor.ServiceDetailsAdaptor;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.LayoutInflaterCompat;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;
import helper.ServiceDetails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aryan.anyservice.ui.home.HomeViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class YourOrderActivity extends AppCompatActivity {
    String uid;
    ArrayList<? extends ServiceDetails> services;
    Button backButton,confirmButton;
    TextView companyTextView,totalpriceTextView,priceTextView,visitcostTextView,pendingTextView;
    ListView serviceListView;
    OdooRPC odooRPC;
    String address;
    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_order);
        SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
        uid = sp.getString("login",null);

        double price = getIntent().getDoubleExtra("price",0);
        final double visitPrice = getIntent().getDoubleExtra("visitPrice",0);
        final double prevPrice = getIntent().getDoubleExtra("pending",0);
        double totalPrice = getIntent().getDoubleExtra("totalPrice",0);
        String company = getIntent().getStringExtra("company");
        services = getIntent().getParcelableArrayListExtra("services");

        backButton = findViewById(R.id.back_button);
        confirmButton = findViewById(R.id.confirm);
        companyTextView = findViewById(R.id.company);
        totalpriceTextView = findViewById(R.id.total_cost);
        priceTextView = findViewById(R.id.price);
        visitcostTextView = findViewById(R.id.delivery_cost);
        pendingTextView = findViewById(R.id.pending);
        serviceListView = findViewById(R.id.services);
        priceTextView.setText(""+price);
        visitcostTextView.setText(""+visitPrice);
        totalpriceTextView.setText(""+totalPrice);
        companyTextView.setText(company);
        pendingTextView.setText(""+prevPrice);

        ServiceDetailsAdaptor serviceDetailsAdaptor=new ServiceDetailsAdaptor(getApplicationContext(),R.layout.service_list_item, (List<ServiceDetails>) services);
        serviceListView.setAdapter(serviceDetailsAdaptor);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                LayoutInflater inflater = LayoutInflaterCompat..from(getApplicationContext());
                View layout = getLayoutInflater().inflate(R.layout.order_description,null);
                final EditText gpsAddress=layout.findViewById(R.id.gps_address);
                final EditText fullAddress=layout.findViewById(R.id.full_address);
                final EditText remark=layout.findViewById(R.id.remark);
                final RadioButton cod=layout.findViewById(R.id.cod);
                final Button placeOrder=layout.findViewById(R.id.place_order);
                final ProgressBar progress=layout.findViewById(R.id.progressbar);
                final ArrayList<HashMap<String,Integer>> serviceIds=new ArrayList<>();
                final AlertDialog alertDialog = new AlertDialog.Builder(YourOrderActivity.this)
                                .setTitle("Place Order")
                                .setIcon(R.mipmap.any_service_icon_foreground)
                                .setView(layout)
                                .show();
                gpsAddress.setText(address);
                for(ServiceDetails service:services){
                    HashMap<String,Integer> map=new HashMap();
                    map.put("id",service.getId());
                    map.put("quantity",service.getUnit());
                    serviceIds.add(map);
                }
                placeOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(cod.isChecked() && !fullAddress.getText().toString().equals("") && !remark.getText().toString().equals("")) {
                            progress.setVisibility(View.VISIBLE);
                            placeOrder.setEnabled(false);
                            HashMap map = new HashMap<String, String>();
                            map.put("method", "xmlrpc_create_order");
                            map.put("model", "anyservice.order");
                            map.put("login", uid);
                            map.put("services", serviceIds.toArray());
                            map.put("visiting_charge", visitPrice);
                            map.put("pending", prevPrice);
                            map.put("gps_address", gpsAddress.getText().toString());
                            map.put("full_address", fullAddress.getText().toString());
                            map.put("remark", remark.getText().toString());


                            AsyncOdooRPCcall task = new AsyncOdooRPCcall();
                            task.execute(map);
                            alertDialog.setCancelable(false);

                        }else if(fullAddress.getText().toString().equals("")){
                            fullAddress.setError("This Field is required");
                        }else if(remark.getText().toString().equals("")){
                            remark.setError("This Field is required");
                        }else if(!cod.isChecked()){
                            cod.setError("This Field is Required");
                        }
                    }
                });
            }
        });
        HashMap map = new HashMap<String,String>();
        map.put("method","get_user_details");
        map.put("model","res.partner");
        map.put("login",uid);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
    }
    @SuppressLint("StaticFieldLeak")
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
                    if (method.equals("xmlrpc_create_order")) {
                        final HashMap<String, Object> finalResult = result;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String orderId = String.valueOf(finalResult.get("order_id"));
                                Toast.makeText(getApplicationContext(), "Your Order is placed with order id - " + orderId, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(YourOrderActivity.this, HomeActivity.class);
                                finishAffinity();
                                startActivity(intent);

                            }
                        });
                    } else if (method.equals("get_user_details")) {
                        address = result.get("place").toString();

                    }
                }
            }catch (Exception e){
                Log.e("ODOO RPC :",e.getMessage());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(YourOrderActivity.this)
                                .setTitle("App Info")
                                .setMessage("No internet Connection Found.")
                                .setIcon(R.mipmap.any_service_icon_foreground)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finishAffinity();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }
                });
            }

            return result;
        }
    }
}