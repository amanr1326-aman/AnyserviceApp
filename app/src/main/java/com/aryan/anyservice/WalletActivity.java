package com.aryan.anyservice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import adaptor.TransactionAdaptor;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;
import helper.TransactionAnyservice;

public class WalletActivity extends AppCompatActivity {
    OdooRPC odooRPC;
    ProgressBar progressBar;
    LinearLayout mainLinearLayout;
    ListView transactionListView;
    TextView amount;
    Button payButton;
    String uid;
    double totalAmount =0;
    final int PAYMENTCODE=141;
    ArrayList<TransactionAnyservice> transactions=new ArrayList<>();
    TransactionAdaptor adaptor;
    final int limit=30;
    boolean loading=true;
    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        setResult(Activity.RESULT_OK,intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        payButton=findViewById(R.id.pay);
        Button back = findViewById(R.id.back_button);
        mainLinearLayout=findViewById(R.id.main_layout);
        progressBar=findViewById(R.id.progress);
        amount=findViewById(R.id.amount);
        transactionListView=findViewById(R.id.transactions);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if(uid==null) {
            SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
            uid = sp.getString("login", null);
        }
        adaptor=new TransactionAdaptor(getApplicationContext(),R.layout.transaction_item,transactions);
        transactionListView.setAdapter(adaptor);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap map3 = new HashMap<String, Object>();
                map3.put("method", "get_payment_link");
                map3.put("model", "anyservice.transaction");
                map3.put("login", uid);
                map3.put("amount", totalAmount);
                AsyncOdooRPCcall task3 = new AsyncOdooRPCcall();
                task3.execute(map3);
            }
        });


        HashMap map = new HashMap<String,String>();
        map.put("method","get_user_details");
        map.put("model","res.partner");
        map.put("login",uid);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);

    addTransactions(0,limit);
        transactionListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount==totalItemCount){
                    if(!loading){
                        loading=true;
                        addTransactions(totalItemCount,limit);
                    }
                }
            }
        });

    }
    class AsyncOdooRPCcall extends AsyncTask<HashMap<String,Object>, ProgressBar,HashMap<String,Object>> {
        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    transactionListView.setVisibility(View.GONE);
                    mainLinearLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }

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
                    if (method.equals("get_user_details")) {
                        amount.setText(String.valueOf(result.get("balance")));
                        if(Double.parseDouble(String.valueOf(result.get("balance")))<0){
                            totalAmount = -1*Double.parseDouble(String.valueOf(result.get("balance")));
                            totalAmount = -1*Double.parseDouble(String.valueOf(result.get("balance")));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    payButton.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                    }else if(method.equals("get_transactions")){
                        final ArrayList<TransactionAnyservice> transactionsList=new ArrayList<>();
                        Object[] objects = (Object[]) result.get("transactions");
                        for(Object obj:objects){
                            HashMap<String,String> trans = (HashMap<String, String>) obj;
                            TransactionAnyservice transactionAnyservice=new TransactionAnyservice();
                            transactionAnyservice.setName(trans.get("name"));
                            transactionAnyservice.setDebit(trans.get("debit"));
                            transactionAnyservice.setCredit(trans.get("credit"));
                            transactionAnyservice.setTotal(trans.get("total"));
                            transactionsList.add(transactionAnyservice);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transactions.addAll(transactionsList);
                                adaptor.notifyDataSetChanged();
                                if(transactionsList.size()>=limit){
                                    loading=false;
                                }

                            }
                        });

                    }else  if(method.equals("get_payment_link")){
                        Intent intent =new Intent(getApplicationContext(),PaymentActivity.class);
                        intent.putExtra("link",String.valueOf(result.get("link")));
                        startActivityForResult(intent,PAYMENTCODE);
                    }else if(method.equals("post_payment")){
                        final HashMap<String, Object> finalResult = result;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), finalResult.get("msg").toString(),Toast.LENGTH_LONG).show();
                                finish();
                                overridePendingTransition(0,0);
                                startActivity(getIntent());
                                overridePendingTransition(0,0);

                            }
                        });
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> stringObjectHashMap) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    transactionListView.setVisibility(View.VISIBLE);
                    mainLinearLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PAYMENTCODE) {
            if (data.getStringExtra("result").equals("Success")) {
                HashMap map2 = new HashMap<String, Object>();
                map2.put("method", "post_payment");
                map2.put("model", "anyservice.transaction");
                map2.put("login", uid);
                map2.put("amount", totalAmount);
                AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
                task2.execute(map2);
                Toast.makeText(this, "Payment Received" + data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void addTransactions(int offset,int limit){

        if(uid==null) {
            SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
            uid = sp.getString("login", null);
        }

        HashMap map = new HashMap<String,String>();
        map.put("method","get_transactions");
        map.put("model","anyservice.transaction");
        map.put("login",uid);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
        map.put("offset",offset);
        map.put("limit",limit);

    }
}