package com.aryan.anyservice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {
    WebView webview;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        webview = findViewById(R.id.web);
        webview.setInitialScale(1);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setAllowContentAccess(true);
        webview.getSettings().setUseWideViewPort(true);
        startWebView(getIntent().getStringExtra("link"));
    }
    private void startWebView(String url) {
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                try {
                    if(url.contains("/website_payment/confirm")){
                        webview.clearHistory();
                        Intent intent=new Intent();
                        intent.putExtra("result","Success");
                        setResult(Activity.RESULT_OK,intent);
                        finish();
                    }else if(url.contains("/payment/payumoney/error") ){
                        webview.clearHistory();
                        Intent intent=new Intent();
                        intent.putExtra("result","Fail");
                        setResult(Activity.RESULT_OK,intent);
                        finish();

                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        webview.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        webview.clearHistory();
        Intent intent=new Intent();
        intent.putExtra("result","Fail");
        setResult(Activity.RESULT_OK,intent);
        finish();
    }
}