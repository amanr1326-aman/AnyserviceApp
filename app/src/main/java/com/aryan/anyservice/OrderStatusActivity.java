package com.aryan.anyservice;

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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;


import com.transferwise.sequencelayout.SequenceLayout;

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

//    String[] descriptionData = {"Submitted", "Pending From Agent","Accepted By Agent","In Progress","Awaiting Payment","Paid"};

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        icon = findViewById(R.id.icon);
        company = findViewById(R.id.order_company);
        name = findViewById(R.id.name);
        totalPrice = findViewById(R.id.order_price);
        description = findViewById(R.id.description);
        rating = findViewById(R.id.rating);
        backButton = findViewById(R.id.back_button);
        confirm = findViewById(R.id.confirm);
        callButton = findViewById(R.id.call);
        AppCompatTextView details = findViewById(R.id.details);
        final Order order= (Order) getIntent().getSerializableExtra("order");
        company.setText(order.getAgentName());
        name.setText(order.getName());
        description.setText(order.getDescription());
        state = (SequenceLayout)findViewById(R.id.state);
        totalPrice.setText(""+order.getTotalPrice());
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:+91"+order.getAgentPhone();
                if(order.getAgentPhone()==null) uri = "tel:+918218781495";
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
    class AsyncOdooRPCcall extends AsyncTask<HashMap<String,Object>, ProgressBar,HashMap<String,Object>> {

        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String, Object>... hashMaps) {
            if(odooRPC==null){
                odooRPC=new OdooRPC();
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
            final HashMap<String,Object> result = (HashMap<String, Object>) odooRPC.callOdoo(model, method,hashMaps[0]);

            if(result.get("result").equals("Success")) {
                if (method.equals("get_order_status")) {
                    Object[] objects = (Object[]) result.get("states");
                    final List<OrderStates> list = new ArrayList<>();
                    for(Object object:objects){
                        OrderStates stateObj = new OrderStates();
                        HashMap<String, Object> stateRes = (HashMap<String, Object>) object;
                        stateObj.setTitle(String.valueOf(stateRes.get("name")));
                        stateObj.setAnchor(String.valueOf(stateRes.get("date")));
                        stateObj.setSubTitle(String.valueOf(stateRes.get("msg")));
                        stateObj.setActive((boolean)stateRes.get("active"));
                        list.add(stateObj);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            StateAdaptor adaptor=new StateAdaptor(list);
                            state.setAdapter(adaptor);
                        }
                    });

                }

            }

            return result;
        }
    }
}