package com.aryan.anyservice;

import adaptor.ServiceDetailsAdaptor;
import adaptor.StateAdaptor;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;
import de.timroes.axmlrpc.XMLRPCException;
import helper.IconTextView;
import helper.OdooRPC;
import helper.Order;
import helper.OrderStates;
import helper.ServiceDetails;
import helper.Utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.lang.UCharacter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.transferwise.sequencelayout.SequenceLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderStatusActivity extends AppCompatActivity {
    AppCompatImageView icon;
    AppCompatTextView company,name,description,totalPrice;
    AppCompatRatingBar rating;
    Button backButton,confirm;
    IconTextView callButton;
    OdooRPC odooRPC;
    SequenceLayout state;
    String uid;
    boolean isAgent=false;
    Order order;
    ProgressBar progressBar;
    ScrollView scrollView;
    ListView services;

//    String[] descriptionData = {"Submitted", "Pending From Agent","Accepted By Agent","In Progress","Awaiting Payment","Paid"};

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        setResult(121,intent);
        finish();

//        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
        uid = sp.getString("login",null);
        icon = findViewById(R.id.icon);
        company = findViewById(R.id.order_company);
        name = findViewById(R.id.name);
        totalPrice = findViewById(R.id.order_price);
        description = findViewById(R.id.description);
        rating = findViewById(R.id.rating);
        backButton = findViewById(R.id.back_button);
        confirm = findViewById(R.id.submit_button);
        callButton = findViewById(R.id.call);
        scrollView = findViewById(R.id.scrollview);
        progressBar = findViewById(R.id.progressbar);
        AppCompatTextView details = findViewById(R.id.details);
        if(order==null) {
            order = (Order) getIntent().getSerializableExtra("order");
        }

        if(uid.equals(""+order.getAgentID())){
            isAgent=true;
            company.setText(order.getCustName());
        }else{
            company.setText(order.getAgentName());
        }
        name.setText(order.getName());
        description.setText(order.getDescription());
        state = (SequenceLayout)findViewById(R.id.state);
        totalPrice.setText(""+order.getTotalPrice());
        rating.setRating(order.getRating());

        if(isAgent){
            LinearLayout actionAgentLayout = findViewById(R.id.action_agent_layout);
            Button cancelAgent = findViewById(R.id.cancel_agent);
            Button confirmAgent = findViewById(R.id.submit_agent);
            Button changePrice = findViewById(R.id.change_price);
            changePrice.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    LinearLayout linearLayout=new LinearLayout(getApplicationContext());
                    LinearLayout linearLayout2=new LinearLayout(getApplicationContext());
                    linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
                    final EditText updatedPrice=new EditText(getApplicationContext());
                    updatedPrice.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
                    updatedPrice.setInputType(UCharacter.NumericType.NUMERIC);
                    updatedPrice.setHint("New Price");
                    linearLayout2.addView(updatedPrice);
                    Button getCode = new Button(getApplicationContext());
                    getCode.setText("Get Code");
                    getCode.setBackground(getResources().getDrawable(R.drawable.round_button));
                    linearLayout2.addView(getCode);
                    final String[] code = new String[1];
                    getCode.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(updatedPrice.getText().toString().equals("") || Double.parseDouble(updatedPrice.getText().toString())==0.0){
                                updatedPrice.setError("This Field is Required");
                            }else{
                                code[0] =generateCode(6);
                                updatedPrice.setEnabled(false);
                                HashMap<String, Object> map2 = new HashMap<String, Object>();
                                map2.put("method", "create_notification");
                                map2.put("model", "anyservice.order");
                                map2.put("id", order.getCustID());
                                map2.put("name", "Update Price Request");
                                map2.put("msg", "Agent want to update price. Please send this code to the agent.\nIf you are satisfies with the price.\nOtherwise you can cancel the order.\nCODE -"+ code[0]);
                                AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                                task2.execute(map2);

                            }
                        }
                    });
                    final EditText remark=new EditText(getApplicationContext());
                    remark.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
                    remark.setHint("Code");
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(linearLayout2);
                    linearLayout.addView(remark);
                    linearLayout.setPadding(20,20,20,20);
                    new AlertDialog.Builder(OrderStatusActivity.this)
                            .setTitle("Order Cancel")
                            .setMessage("Cancelling Order")
                            .setIcon(R.mipmap.any_service_icon_foreground)
                            .setView(linearLayout)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(code[0]==null){
                                        Toast.makeText(getApplicationContext(),"Please generate Code First",Toast.LENGTH_LONG).show();
                                    }else if(remark.getText().toString().equals(code[0])) {
                                        HashMap map2 = new HashMap<String, Object>();
                                        map2.put("method", "update_order");
                                        map2.put("model", "anyservice.order");
                                        map2.put("login", uid);
                                        map2.put("id", order.getId());
                                        map2.put("price", Double.parseDouble(updatedPrice.getText().toString()));
                                        map2.put("msg", remark.getText().toString());
                                        AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                                        task2.execute(map2);
                                    }else{
                                        remark.setError("Incorrect Code.");
                                        Toast.makeText(getApplicationContext(),"Incorrect code",Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            });
            confirm.setVisibility(View.GONE);
            actionAgentLayout.setVisibility(View.VISIBLE);
            if (order.getState().equals("Paid") || order.getState().equals("Awaiting Payment")|| order.getState().equals("Cancelled")) {
                cancelAgent.setVisibility(View.GONE);
                if(order.getState().equals("Awaiting Payment")){
                    confirmAgent.setText("Mark Paid");
                    confirmAgent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            agentAction(order,"Mark Paid","Did you got Your Payment by cash?","paid");
                        }
                    });
                }
                if(order.getState().equals("Cancelled")||order.getState().equals("Paid")){
                    confirmAgent.setVisibility(View.GONE);
                }
            }else{
                switch (order.getState()) {
                    case "Submitted":
                    case "Pending From Agent":
                        changePrice.setVisibility(View.VISIBLE);
                        confirmAgent.setText("Accept");
                        confirmAgent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                agentAction(order, "Accept Order", "Do You want to Accept Order?", "accept");
                            }
                        });
                        break;
                    case "Accepted By Agent":
                        confirmAgent.setText("Start Service");
                        confirmAgent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                agentAction(order, "Start Service", "Do You want to start Service(Please Enter the secret Code from customer)?", "progress");
                            }
                        });
                        break;
                    case "In Progress":
                        confirmAgent.setText("Mark done");
                        confirmAgent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                agentAction(order, "Mark Done", "Did you completed service for the customer?", "done");
                            }
                        });
                        break;
                }
            }
            cancelAgent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout linearLayout=new LinearLayout(getApplicationContext());
                    final EditText remark=new EditText(getApplicationContext());
                    remark.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
                    remark.setHint("Remark");
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(remark);
                    linearLayout.setPadding(20,20,20,20);
                    new AlertDialog.Builder(OrderStatusActivity.this)
                            .setTitle("Order Cancel")
                            .setMessage("Cancelling Order")
                            .setIcon(R.mipmap.any_service_icon_foreground)
                            .setView(linearLayout)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(!remark.getText().toString().equals("")) {
                                        HashMap map2 = new HashMap<String, Object>();
                                        map2.put("method", "update_order");
                                        map2.put("model", "anyservice.order");
                                        map2.put("login", uid);
                                        map2.put("id", order.getId());
                                        map2.put("cancel", true);
                                        map2.put("charge", 0);
                                        map2.put("msg", remark.getText().toString());
                                        AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                                        task2.execute(map2);
                                    }else{
                                        remark.setError("This field is required.");
                                        Toast.makeText(getApplicationContext(),"All fields are required",Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .setNegativeButton("No",null)
                            .show();

                }
            });

        }else {
            if (order.getState().equals("Submitted") || order.getState().equals("Pending From Agent")) {

            } else if (order.getState().equals("Accepted By Agent")) {
                double price = 0;
                if (order.getTotalPrice() > 300) {
                    price = 300;
                } else {
                    price = order.getTotalPrice();
                }
                confirm.setText("Cancel (Charge - Rs. " + price * 10 / 100 + ")");
            } else if (order.getState().equals("Paid")) {
                confirm.setText("Rate - " + order.getAgentName());
            } else {
                confirm.setVisibility(View.INVISIBLE);
            }

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(order.getState().equals("Submitted") || order.getState().equals("Pending From Agent") || order.getState().equals("Accepted By Agent")){

                        double price = 0;
                        if(order.getState().equals("Accepted By Agent")) {
                            if (order.getTotalPrice() > 300) {
                                price = 300;
                            } else {
                                price = order.getTotalPrice();
                            }
                        }
                        final double finalPrice = price*10/100;
                        LinearLayout linearLayout=new LinearLayout(getApplicationContext());
                        final EditText remark=new EditText(getApplicationContext());
                    remark.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
                        remark.setHint("Remark");
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.addView(remark);
                        linearLayout.setPadding(20,20,20,20);
                        new AlertDialog.Builder(OrderStatusActivity.this)
                                .setTitle("Order Cancel")
                                .setMessage("Cancelling Order with Charge  - "+finalPrice)
                                .setIcon(R.mipmap.any_service_icon_foreground)
                                .setView(linearLayout)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(!remark.getText().toString().equals("")) {
                                            HashMap map2 = new HashMap<String, Object>();
                                            map2.put("method", "update_order");
                                            map2.put("model", "anyservice.order");
                                            map2.put("login", uid);
                                            map2.put("id", order.getId());
                                            map2.put("cancel", true);
                                            map2.put("charge", finalPrice);
                                            map2.put("msg", remark.getText().toString());
                                            AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                                            task2.execute(map2);
                                        }else{
                                            remark.setError("This field is required.");
                                            Toast.makeText(getApplicationContext(),"All fields are required",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                                .setNegativeButton("No",null)
                                .show();

                    }
                    if(order.getState().equals("Paid")){
                        LinearLayout linearLayout=new LinearLayout(getApplicationContext());
                        linearLayout.setPadding(40,20,40,20);
                        final RatingBar ratingBar=new RatingBar(getApplicationContext());
                        final EditText remark=new EditText(getApplicationContext());
                    remark.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
                        remark.setHint("Remark");
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.addView(ratingBar);
                        linearLayout.addView(remark);
                        ratingBar.setClickable(true);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            ratingBar.setMin(1);
//                            ratingBar.setMax(5);
//                        }else{
//                            ratingBar.setMax(4);
//                        }
                        ratingBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        ratingBar.setNumStars(5);
                        new AlertDialog.Builder(OrderStatusActivity.this)
                                .setTitle("Rating")
                                .setMessage("Please Rate - "+order.getAgentName())
                                .setIcon(R.mipmap.any_service_icon_foreground)
                                .setView(linearLayout)
                                .setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(ratingBar.getRating()!=0 && ! remark.getText().toString().equals("")) {
                                            HashMap map = new HashMap<String, String>();
                                            map.put("method", "update_user_details");
                                            map.put("model", "res.partner");
                                            map.put("login", order.getAgentID());
                                            map.put("rating", ratingBar.getRating());
                                            AsyncOdooRPCcall task = new AsyncOdooRPCcall();
                                            task.execute(map);
                                            HashMap map2 = new HashMap<String, String>();
                                            map2.put("method", "update_order");
                                            map2.put("model", "anyservice.order");
                                            map2.put("login", uid);
                                            map2.put("id", order.getId());
                                            map2.put("rating", ratingBar.getRating());
                                            map2.put("msg", remark.getText().toString());
                                            AsyncOdooRPCcall task2= new AsyncOdooRPCcall();
                                            task2.execute(map2);
                                        }else{
                                            if(remark.getText().toString().equals("")){
                                                remark.setError("Remark Field is required");
                                            }
                                            Toast.makeText(getApplicationContext(),"All fields are required..",Toast.LENGTH_LONG).show();

                                        }
                                    }
                                })
                                .setCancelable(false)
                                .setNegativeButton("Cancel",null)
                                .show();
                    }
                }
            });
        }
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:+918218781495";
                if(isAgent){
                    uri = "tel:+91" + order.getCustPhone();
                }else {
                    uri = "tel:+91" + order.getAgentPhone();
                }
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });

        try {
            byte[] decodedString = Base64.decode(order.getIcon(), Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            icon.setImageBitmap(decodedBitmap);
        }catch(Exception e){

        }
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View layout = getLayoutInflater().inflate(R.layout.order_description,null);
                final EditText gpsAddress=layout.findViewById(R.id.gps_address);
                final EditText fullAddress=layout.findViewById(R.id.full_address);
                final EditText remark=layout.findViewById(R.id.remark);
                final RadioButton cod=layout.findViewById(R.id.cod);
                final Button placeOrder=layout.findViewById(R.id.place_order);
                final TextView yourItems=layout.findViewById(R.id.your_items);
                yourItems.setVisibility(View.VISIBLE);
                services=layout.findViewById(R.id.services);
                services.setVisibility(View.VISIBLE);
                HashMap map = new HashMap<String,String>();
                map.put("method","get_order_services");
                map.put("id",order.getId());
                map.put("model","anyservice.order");

                AsyncOdooRPCcall task = new AsyncOdooRPCcall();
                task.execute(map);
                gpsAddress.setText(order.getGpsAddress());
                fullAddress.setText(order.getFullAddress());
                fullAddress.setEnabled(false);
                remark.setText(order.getDescription());
                remark.setEnabled(false);
                cod.setEnabled(false);
                placeOrder.setVisibility(View.GONE);
                new AlertDialog.Builder(OrderStatusActivity.this)
                        .setTitle("Order Details")
                        .setIcon(R.mipmap.any_service_icon_foreground)
                        .setView(layout)
                        .setPositiveButton("Ok",null)
                        .show();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        HashMap map = new HashMap<String,String>();
        map.put("method","get_order_status");
        map.put("id",order.getId());
        map.put("model","anyservice.order");

        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
    }
    @SuppressLint("StaticFieldLeak")
    class AsyncOdooRPCcall extends AsyncTask<HashMap<String,Object>, ProgressBar,HashMap<String,Object>> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String, Object>... hashMaps) {
            HashMap<String, Object> result = null;
            try {

                if (odooRPC == null) {
                    odooRPC = new OdooRPC();
                    try {
                        odooRPC.login();
                    } catch (XMLRPCException e) {
                        e.printStackTrace();
                    }
                }
                if (order == null) {
                    order = (Order) getIntent().getSerializableExtra("order");
                }
                String method = (String) hashMaps[0].get("method");
                String model = (String) hashMaps[0].get("model");
                hashMaps[0].remove("method");
                hashMaps[0].remove("model");
                result = (HashMap<String, Object>) odooRPC.callOdoo(model, method, hashMaps[0]);

                if (result.get("result").equals("Success")) {
                    if (method.equals("get_order_status")) {
                        Object[] objects = (Object[]) result.get("states");
                        final List<OrderStates> list = new ArrayList<>();
                        for (Object object : objects) {
                            OrderStates stateObj = new OrderStates();
                            HashMap<String, Object> stateRes = (HashMap<String, Object>) object;
                            stateObj.setTitle(String.valueOf(stateRes.get("name")));
                            stateObj.setAnchor(String.valueOf(stateRes.get("date")));
                            stateObj.setSubTitle(String.valueOf(stateRes.get("msg")));
                            stateObj.setActive((boolean) stateRes.get("active"));
                            list.add(stateObj);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StateAdaptor adaptor = new StateAdaptor(list);
                                state.setAdapter(adaptor);
                            }
                        });

                    } else if (method.equals("update_user_details")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Thank You! Rated Successfully.", Toast.LENGTH_LONG).show();

                            }
                        });

                    } else if (method.equals("update_order")) {
                    if(hashMaps[0].get("rating")!=null){
                        order.setRating((Float) hashMaps[0].get("rating"));
                    }if(hashMaps[0].get("price")!=null){
                        order.setTotalPrice((Double) hashMaps[0].get("price"));
                    }
                    if(hashMaps[0].get("cancel")!=null){
                        order.setState("Cancelled");
                    }
                    if(hashMaps[0].get("accept")!=null){
                        order.setState("Accepted By Agent");
                    }
                    if(hashMaps[0].get("progress")!=null){
                        order.setState("In Progress");
                    }
                    if(hashMaps[0].get("done")!=null){
                        order.setState("Awaiting Payment");
                    }
                    if( hashMaps[0].get("paid")!=null){
                        order.setState("Paid");
                    }
                    reloadActivity(order);
                        final String msg = String.valueOf(result.get("msg"));
                        if(!msg.equals("null")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                                }
                            });
                        }

                    } else if (method.equals("get_order_services")) {
                        Object[] objects = (Object[]) result.get("services");
                        final List<ServiceDetails> list = new ArrayList<>();
                        for (Object obj : objects) {
                            HashMap<String, String> service = (HashMap<String, String>) obj;
                            ServiceDetails serviceDetails = new ServiceDetails();
                            serviceDetails.setName(service.get("name"));
                            serviceDetails.setPrice(Double.parseDouble(String.valueOf(service.get("price"))));
                            serviceDetails.setUnit(Integer.parseInt(String.valueOf(service.get("quantity"))));
                            list.add(serviceDetails);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ServiceDetailsAdaptor serviceDetailsAdaptor = new ServiceDetailsAdaptor(getApplicationContext(), R.layout.service_list_item, list);
                                services.setAdapter(serviceDetailsAdaptor);
                                Utility.setListViewHeightBasedOnChildren(services);
                            }
                        });
                    } else if (method.equals("notify_onchange_order")) {
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
                        Log.e("Notification",""+list.get(0).getId());
                        if (list.size() > 0) {
                            reloadActivity(list.get(0));
                        }

                    }

                } else {
                    final String msg = String.valueOf(result.get("msg"));
                    if(!msg.equals("null")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                            }
                        });
                    }

                }
                if(method.equals("notify_onchange_order")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(scrollView.getVisibility()==View.GONE) {
                                progressBar.setVisibility(View.GONE);
                                scrollView.setVisibility(View.VISIBLE);
                            }

                        }
                    });
                }
            }catch (Exception e){
                Log.e("ODOO RPC :",e.getMessage());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(OrderStatusActivity.this)
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

        @Override
        protected void onPostExecute(HashMap<String, Object> stringObjectHashMap) {

        }
    }

    void agentAction(final Order orderobj, String title, String msg, final String action){
        LinearLayout linearLayout=new LinearLayout(getApplicationContext());
        final EditText remark=new EditText(getApplicationContext());
                    remark.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
        if(action.equals("progress")) {
            remark.setHint("Secret Code");
        }else{
            remark.setHint("Remark");
        }
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(remark);
        linearLayout.setPadding(20,20,20,20);
        new AlertDialog.Builder(OrderStatusActivity.this)
                .setTitle(title)
                .setMessage(msg)
                .setIcon(R.mipmap.any_service_icon_foreground)
                .setView(linearLayout)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!remark.getText().toString().equals("")) {
                            HashMap map2 = new HashMap<String, Object>();
                            map2.put("method", "update_order");
                            map2.put("model", "anyservice.order");
                            map2.put("login", uid);
                            map2.put("id", orderobj.getId());
                            map2.put(action, true);
                            map2.put("msg", remark.getText().toString());
                            if(action.equals("progress")) {
                                map2.put("msg", "Work Start attempt by the Agent");
                                map2.put("code", remark.getText().toString());
                            }
                            AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                            task2.execute(map2);
                        }else{
                            remark.setError("This field is required.");
                            Toast.makeText(getApplicationContext(),"All fields are required",Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("No",null)
                .show();
    }
    void reloadActivity(Order orderobj){
        Intent intent =getIntent();
        intent.putExtra("order",orderobj);
        finish();
        overridePendingTransition(0,0);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    String generateCode(int n){

        String alpha="1234567890";
        StringBuilder builder=new StringBuilder();
        for (int i=0;i<n;i++){
            int index=(int)(alpha.length()*Math.random());
            builder.append(alpha.charAt(index));
        }
        return builder.toString();
    }
    boolean active=false;
    @Override
    protected void onStart() {
        super.onStart();
        active=true;
        runUpdateThread(getApplicationContext());

    }

    @Override
    protected void onStop() {
        super.onStop();
        active=false;
    }

    void runUpdateThread(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (active) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    try {

                        if (uid == null) {
                            SharedPreferences sp = context.getSharedPreferences("odoologin", Context.MODE_PRIVATE);
                            uid = sp.getString("login", null);
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                HashMap map = new HashMap<String, Object>();
                                map.put("login", uid);
                                map.put("id", order.getId());
                                map.put("model", "anyservice.order");
                                map.put("method","notify_onchange_order");
                                AsyncOdooRPCcall task = new AsyncOdooRPCcall();
                                task.execute(map);
                            }
                        });
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            }
        }).start();
    }
}