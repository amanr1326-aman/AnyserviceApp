package com.aryan.anyservice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.lang.UCharacter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.transferwise.sequencelayout.SequenceLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import adaptor.ServiceDetailsAdaptor;
import adaptor.StateAdaptor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;
import de.timroes.axmlrpc.XMLRPCException;
import helper.IconTextView;
import helper.OdooRPC;
import helper.Order;
import helper.OrderStates;
import helper.ServiceDetails;
import helper.Utility;

public class OrderStatusActivity extends AppCompatActivity {
    AppCompatImageView icon;
    AppCompatTextView company,name,description,totalPrice;
    AppCompatRatingBar rating;
    Button backButton,confirm,pay;
    AppCompatButton code;
    IconTextView callButton;
    OdooRPC odooRPC;
    SequenceLayout state;
    String uid;
    boolean isAgent=false;
    Order order;
    ProgressBar progressBar;
    ScrollView scrollView;
    ListView services;
    final int PAYMENTCODE=141;
    ImageView verified;
    ShowcaseView sv;


    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        setResult(121,intent);
        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
        uid = sp.getString("login",null);
        icon = findViewById(R.id.icon);
        pay = findViewById(R.id.pay_button);
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
        code = findViewById(R.id.code);
        verified = findViewById(R.id.verified);
        Button sendMessage = findViewById(R.id.send_message);
        AppCompatTextView details = findViewById(R.id.details);
        AppCompatTextView agentIDTv = findViewById(R.id.agent_id);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout linearLayout=new LinearLayout(getApplicationContext());
                final EditText remark=new EditText(getApplicationContext());
                remark.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
                remark.setHint("Message");
                remark.setInputType(Pattern.MULTILINE);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(remark);
                linearLayout.setPadding(50,20,50,20);
                final AlertDialog alert =new AlertDialog.Builder(OrderStatusActivity.this)
                        .setTitle("Send Message")
                        .setMessage("Send reminder or messages")
                        .setIcon(R.mipmap.any_service_icon_foreground)
                        .setView(linearLayout)
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!remark.getText().toString().equals("")) {
                                    HashMap map2 = new HashMap<String, Object>();
                                    map2.put("method", "xmlrpcadd_remark");
                                    map2.put("model", "anyservice.order");
                                    map2.put("login", uid);
                                    map2.put("id", order.getId());
                                    map2.put("msg", remark.getText().toString());
                                    AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                                    task2.execute(map2);
                                }else{
                                    remark.setError("This field is required.");
                                    Toast.makeText(getApplicationContext(),"All fields are required",Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .setCancelable(false)
                        .show();
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                remark.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(remark.getText().length()>0){
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }else{
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                        }

                    }
                });

            }
        });
        if(order==null) {
            order = (Order) getIntent().getSerializableExtra("order");
        }

        if(uid.equals(""+order.getAgentID())){
            isAgent=true;
            company.setText(order.getCustName());
        }else{
            company.setText(order.getAgentName());
        }

        agentIDTv.setText(String.format("ID - ASA%d", order.getAgentID()));
        name.setText(order.getName());
        description.setText(order.getDescription());
        state = (SequenceLayout)findViewById(R.id.state);
        totalPrice.setText(""+order.getTotalPrice());
        rating.setRating(order.getRating());
        if(order.getState().equals("Paid")){
            Button invoice=findViewById(R.id.invoice_button);
            invoice.setVisibility(View.VISIBLE);
            invoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPermision();
                }
            });
        }

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
                    linearLayout.setPadding(50,20,50,20);
                    final AlertDialog alert = new AlertDialog.Builder(OrderStatusActivity.this)
                            .setTitle("Price Update")
                            .setMessage("Update order price by using otp sent to the customer after get OTP button is pressed.")
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
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    remark.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if(remark.getText().length()>0){
                                alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            }else{
                                alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                            }

                        }
                    });
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
                                agentAction(order, "Start Service", "Do You want to start Service?", "progress");
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
                    linearLayout.setPadding(50,20,50,20);
                    final AlertDialog alert = new AlertDialog.Builder(OrderStatusActivity.this)
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
                            .setCancelable(false)
                            .show();
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    remark.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if(remark.getText().length()>0){
                                alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            }else{
                                alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                            }

                        }
                    });

                }
            });

        }else {////Customer start
            if(order.getState().equals("Accepted By Agent")) {
                code.setText("Code -" + order.getCode());
                code.setVisibility(View.VISIBLE);
                code.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(OrderStatusActivity.this)
                                .setTitle("Send Code to Agent")
                                .setMessage("Do you want to send the code to agent to start your service now?")
                                .setIcon(R.mipmap.any_service_icon_foreground)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        HashMap<String, Object> map2 = new HashMap<String, Object>();
                                        map2.put("method", "create_notification");
                                        map2.put("model", "anyservice.order");
                                        map2.put("id", order.getAgentID());
                                        map2.put("name", "Code for Order " + order.getName());
                                        map2.put("msg", "Code - " + order.getCode() + "\nCustomer sent you the code to start the service.\nPlease use this code to start the Service.");
                                        AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                                        task2.execute(map2);
                                        dialog.dismiss();

                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setCancelable(true)
                                .show();
                    }
                });
            }
            if(order.getState().equals("Awaiting Payment")){
                pay.setVisibility(View.VISIBLE);
                pay.setText(pay.getText().toString()+" ("+order.getTotalPrice()+")");
                pay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HashMap<String, Object> map2 = new HashMap<String, Object>();
                        map2.put("method", "get_payment_link");
                        map2.put("model", "anyservice.order");
                        map2.put("login", uid);
                        map2.put("id", order.getId());
                        AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                        task2.execute(map2);

                    }
                });
            }
            if (order.getState().equals("Submitted") || order.getState().equals("Pending From Agent")) {

            } else if (order.getState().equals("Accepted By Agent")) {
                double price = 0;
                if (order.getTotalPrice() > 300) {
                    price = 300;
                } else {
                    price = order.getTotalPrice();
                }
                confirm.setText("Cancel (Charge - Rs. " + price * 10 / 100 + ")");
            } else if (order.getState().equals("Paid") && order.getRating()==0) {
                confirm.setText("Rate - " + order.getAgentName());
            } else {
                confirm.setVisibility(View.GONE);
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
                        linearLayout.setPadding(50,20,50,20);
                        final AlertDialog alert = new AlertDialog.Builder(OrderStatusActivity.this)
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
                                .setCancelable(false)
                                .show();
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        remark.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if(remark.getText().length()>0){
                                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                }else{
                                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                                }

                            }
                        });

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
                        ratingBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        ratingBar.setNumStars(5);
                        final AlertDialog alert = new AlertDialog.Builder(OrderStatusActivity.this)
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
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        remark.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if(remark.getText().length()>0 && ratingBar.getRating()>0){
                                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                }else{
                                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                                }

                            }
                        });
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
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                order.
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
                if(sv!=null){
                    if(sv.isShowing()) sv.hide();
                    sv=null;
                }
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

        sv = new ShowcaseView.Builder(OrderStatusActivity.this)
                .setTarget(new ViewTarget(details))
                .setContentTitle("More Details")
                .setContentText("Click to all the details related to your order.")
                .singleShot(205) // provide a unique ID used to ensure it is only shown once
                .withHoloShowcase()
                .setStyle(R.style.showcaseTheme)
                .build();
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
                if(!method.equals("notify_onchange_order")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (scrollView.getVisibility() == View.VISIBLE) {
                                progressBar.setVisibility(View.VISIBLE);
                                scrollView.setVisibility(View.GONE);
                            }

                        }
                    });
                }
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
                        final HashMap<String, Object> finalResult = result;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(Boolean.parseBoolean(String.valueOf(finalResult.get("verified"))) && !isAgent){
                                    verified.setVisibility(View.VISIBLE);
                                }
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
                        if( hashMaps[0].get("paid")!=null || hashMaps[0].get("clientpaid")!=null){
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
                                ServiceDetailsAdaptor serviceDetailsAdaptor = new ServiceDetailsAdaptor(getApplicationContext(), R.layout.service_list_item, list,false);
                                services.setAdapter(serviceDetailsAdaptor);
                                Utility.setListViewHeightBasedOnChildren(services);
                            }
                        });
                    } else if (method.equals("notify_onchange_order")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (scrollView.getVisibility() == View.VISIBLE) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    scrollView.setVisibility(View.GONE);
                                }

                            }
                        });
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
                            orderObj.setCode(order.get("code"));
                            orderObj.setCustPhone(order.get("cust_phone"));
                            orderObj.setAgentPhone(order.get("agent_phone"));
                            orderObj.setRating(Float.parseFloat(String.valueOf(order.get("rating"))));
                            orderObj.setInvoicdId(Integer.parseInt(String.valueOf(order.get("invoice_id"))));
                            orderObj.setOrderDate(order.get("order_date"));
                            orderObj.setFinalDate(order.get("final_date"));
                            list.add(orderObj);
                        }
                        if (list.size() > 0) {
                            reloadActivity(list.get(0));
                        }

                    }else  if(method.equals("get_payment_link")){
                        Intent intent =new Intent(getApplicationContext(),PaymentActivity.class);
                        intent.putExtra("link",String.valueOf(result.get("link")));
                        startActivityForResult(intent,PAYMENTCODE);
                    }else if(method.equals("xmlrpcadd_remark")){
                        final String msg = String.valueOf(result.get("msg"));
                        if(!msg.equals("null")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                                }
                            });
                        }
                        reloadActivity(order);
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
            }catch (Exception e){

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
            }finally {
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
        remark.setHint("Remark");

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(remark);
        linearLayout.setPadding(50,20,50,20);
        final AlertDialog alert = new AlertDialog.Builder(OrderStatusActivity.this)
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
                                map2.put("code", order.getCode());
                            }
                            AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                            task2.execute(map2);
                        }else{
                            remark.setError("This field is required.");
                            Toast.makeText(getApplicationContext(),"All fields are required",Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        remark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(remark.getText().length()>0){
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }else{
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                }

            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        active=false;
    }

    void runUpdateThread(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (active) {
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
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            }
        }).start();
    }
    private class DownloadFile extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            String fileName = order.getName()+".pdf";  // -> maven.pdf
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = new File(extStorageDirectory, "Anyservice");
            folder.mkdir();

            File pdfFile = new File(folder, fileName);

            try{
                pdfFile.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
            odooRPC.downloadInvoice(order.getId(), pdfFile);
            return null;
        }

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(scrollView.getVisibility()==View.VISIBLE) {
                        progressBar.setVisibility(View.VISIBLE);
                        scrollView.setVisibility(View.GONE);
                    }

                }
            });
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(scrollView.getVisibility()==View.GONE) {
                        progressBar.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                    }

                }
            });
            File pdfFile = new File(Environment.getExternalStorageDirectory().toString() + "/Anyservice/" + order.getName()+".pdf");  // -> filename = maven.pdf
            Uri path = FileProvider.getUriForFile(getApplicationContext(),"com.aryan.anyservice.fileprovider",pdfFile);
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            try{
                startActivity(pdfIntent);
            }catch(ActivityNotFoundException e){
                Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(odooRPC.downloadURL(order.getId())));
                startActivity(i);
                Toast.makeText(getApplicationContext(), "No Application available to view PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }
    final int STORAGE_PERMISSION=13123;
    void requestPermision(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);
            }
            else
            {
                new DownloadFile().execute();

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                new DownloadFile().execute();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENTCODE) {
            if (data.getStringExtra("result").equals("Success")) {
                HashMap<String, Object> map2 = new HashMap<String, Object>();
                map2.put("method", "update_order");
                map2.put("model", "anyservice.order");
                map2.put("login", uid);
                map2.put("id", order.getId());
                map2.put("clientpaid", true);
                map2.put("msg", "Payment Received Through Online Mode");
                AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                task2.execute(map2);
                Toast.makeText(this, "Payment Received" + data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();

            }
        }
    }
}