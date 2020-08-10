package com.aryan.anyservice;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;

public class WelcomeActivity extends AppCompatActivity {
    TextView instruction;
    OdooRPC odooRPC;
    ProgressBar progressBar;
    Boolean result;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ImageView imageView=findViewById(R.id.logo);
        progressBar = findViewById(R.id.progress);
        instruction=findViewById(R.id.instruction);
        LinearLayout linearLayout=findViewById(R.id.welcome_layout);
        button = findViewById(R.id.ok_button);
        result = getIntent().getBooleanExtra("result",false);
        if(result){
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(WelcomeActivity.this,HomeActivity.class);
                    finish();
                    startActivity(intent);

                }
            });

        }else {
            String msg = getIntent().getStringExtra("msg");
            TextView tv = findViewById(R.id.pending_textview);
            tv.setText(String.format("%s\nWhile we are checking the same.....\nPlease check after few minutes.", msg));
        }
        HashMap map = new HashMap<String,String>();
        map.put("method","get_instructions");
        map.put("model","res.partner");
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
        instruction.setMovementMethod(new ScrollingMovementMethod());

    }
    @SuppressLint("StaticFieldLeak")
    class AsyncOdooRPCcall extends AsyncTask<HashMap<String,Object>, ProgressBar,HashMap<String,String>> {

        @Override
        protected HashMap<String,String> doInBackground(HashMap<String, Object>... hashMaps) {
            HashMap<String, String> result=null;
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
                hashMaps[0].remove("method");
                result = (HashMap<String, String>) odooRPC.callOdoo("res.partner", method, hashMaps[0]);
                if (method.equals("get_instructions")) {
                    final HashMap<String, String> finalResult = result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            instruction.setText(finalResult.get("instruction"));
                        }
                    });

                }
            }catch (Exception e){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(WelcomeActivity.this)
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
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(HashMap<String, String> stringStringHashMap) {
            progressBar.setVisibility(View.GONE);
            if(result){
                button.setEnabled(true);
            }
        }
    }
}