package com.aryan.anyservice;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import helper.OdooRPC;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private String state="split",user_otp="123456";
    Boolean imageFront=false,imageBack=false;
    OdooRPC odooRPC;
    Uri frontUri,backUri;


    LinearLayout loginLinearLayout,phoneLinearLayout,otpLinearLayout,createLinearLayout,sellerLinearLayout;
    Button backButton,continuePhoneButton,requestOtpButton,submitOtpButton,registerButton;
    ProgressBar continueProgressBar,submitProgressBar, otpProgressBar,createProgressBar;
    EditText phoneEditText,otpEditText,fullnameEditText,emailEditText,aadharEditText,companynameEditText,street1EditText,street2EditText,cityEditText,gstEditText,pinEditText;
    TextView otpTextView,resendotpTextView;
    RadioGroup accountTypeRadioGroup;
    RadioButton customerRadioButton,sellerRadioButton;
    ImageView aadharfrontImageView,aadharbackImageView,companylogoImageView;
    AppCompatSpinner stateAppCompatSpinner;
    private static final int CAMERA_PERMISSION_CODE = 100, IMAGE_AADHAR_FRONT_CODE=1001, IMAGE_AADHAR_BACK_CODE=1002, RC_SIGN_IN=1003, COMPANY_LOGO=1004,CAMERA_COMPANY_LOGO=1005;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        if(!isTaskRoot()
//                    && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
//                    && getIntent().getAction()!=null
//                    && getIntent().getAction().equals(Intent.ACTION_MAIN)
//                    ){
//            finish();
//            return;
//        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

        SignInButton signInButton = findViewById(R.id.google_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });


        final ImageView logoImageView = findViewById(R.id.logo);

        loginLinearLayout = findViewById(R.id.login_layout);
        phoneLinearLayout = findViewById(R.id.phone_layout);
        otpLinearLayout = findViewById(R.id.otp_layout);
        createLinearLayout = findViewById(R.id.account_create_layout);
        sellerLinearLayout = findViewById(R.id.seller_layout);

        phoneEditText = findViewById(R.id.phone_edittext);
        otpEditText = findViewById(R.id.otp_edittext);
        fullnameEditText = findViewById(R.id.fullname_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        aadharEditText = findViewById(R.id.aadhar_edittext);
        companynameEditText = findViewById(R.id.company_name);
        street1EditText = findViewById(R.id.street1);
        street2EditText = findViewById(R.id.street2);
        cityEditText = findViewById(R.id.city);
        gstEditText = findViewById(R.id.gst);
        pinEditText = findViewById(R.id.pin);

        stateAppCompatSpinner = findViewById(R.id.state);


        final TextView phoneTextView = findViewById(R.id.phone_textview);
        otpTextView = findViewById(R.id.otp_textview);
        resendotpTextView = findViewById(R.id.resend_otp_textview);

        continueProgressBar =  findViewById(R.id.progress_continue);
        submitProgressBar =  findViewById(R.id.progress_submit);
        otpProgressBar =  findViewById(R.id.otp_progressbar);
        createProgressBar =  findViewById(R.id.create_progressbar);

        accountTypeRadioGroup =findViewById(R.id.account_type);

        customerRadioButton = findViewById(R.id.customer_radiobutton);
        sellerRadioButton = findViewById(R.id.seller_radionbutton);


        aadharfrontImageView = findViewById(R.id.aadhar_front);
        aadharbackImageView = findViewById(R.id.aadhar_back);
        companylogoImageView = findViewById(R.id.company_logo);

        final View split = findViewById(R.id.split_layout);

        continuePhoneButton = findViewById(R.id.continue_button);
        requestOtpButton = findViewById(R.id.request_otp_button);
        submitOtpButton = findViewById(R.id.submit_otp_button);
        registerButton = findViewById(R.id.register);
        backButton = findViewById(R.id.back_button);


        final ProgressBar loadingProgressBar = findViewById(R.id.loading);


        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(phoneEditText.getText().length()==10){
                    requestOtpButton.setEnabled(true);
                }else{
                    requestOtpButton.setEnabled(false);
                }
            }
        });
        otpEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(otpEditText.getText().length()==6){
                    submitOtpButton.setEnabled(true);
                }else{
                    submitOtpButton.setEnabled(false);
                }
            }
        });

        fullnameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(fullnameEditText.getText().length()>=2){
                    registerButton.setEnabled(true);
                }else{
                    registerButton.setEnabled(false);
                }

            }
        });

        continuePhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButton.setEnabled(false);
                continuePhoneButton.setEnabled(false);
                continueProgressBar.setVisibility(View.VISIBLE);
                AsyncSplitTask task = new AsyncSplitTask();
                task.execute(null,phoneLinearLayout,loginLinearLayout);
                backButton.setVisibility(View.VISIBLE);

            }
        });
        requestOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButton.setEnabled(false);
                phoneEditText.setEnabled(false);
                requestOtpButton.setEnabled(false);
                submitProgressBar.setVisibility(View.VISIBLE);
                phoneTextView.setText("+91 "+phoneEditText.getText());
                AsyncSplitTask task = new AsyncSplitTask();
                task.execute(null,otpLinearLayout,phoneLinearLayout);
                Thread t =new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String mainOTPText = otpTextView.getText().toString();
                        for(int i=59; i>0; i--) {
                            if(!state.equals("otp")){

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        otpTextView.setText("Auto Verifying your OTP in ");

                                    }
                                });
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            final int finalI = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    otpTextView.setText(String.format("%s (0:%d)", mainOTPText, finalI));

                                }
                            });
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                otpTextView.setText("Didn't receive OTP?");
                                resendotpTextView.setVisibility(View.VISIBLE );

                            }
                        });

                    }
                });
                t.start();
            }
        });
        submitOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButton.setEnabled(false);
                otpEditText.setEnabled(false);
                submitOtpButton.setEnabled(false);
                otpProgressBar.setVisibility(View.VISIBLE);
                AsyncSplitTask task = new AsyncSplitTask();
                task.execute(null,null,otpLinearLayout);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if(customerRadioButton.isChecked()||(aadharEditText.getText().length()==12 && imageBack && imageFront && ! cityEditText.getText().equals("") && ! companynameEditText.getText().equals(""))){
                        backButton.setEnabled(false);
                        emailEditText.setEnabled(false);
                        fullnameEditText.setEnabled(false);
                        registerButton.setEnabled(false);

                        companynameEditText.setEnabled(false);
                        street1EditText.setEnabled(false);
                        street2EditText.setEnabled(false);
                        cityEditText.setEnabled(false);
                        gstEditText.setEnabled(false);
                        pinEditText.setEnabled(false);
                        stateAppCompatSpinner.setEnabled(false);

                        createProgressBar.setVisibility(View.VISIBLE);
                        AsyncSplitTask task = new AsyncSplitTask();
                        task.execute(null,null,createLinearLayout);

                    }else{
                        if(aadharEditText.getText().length()!=12){
                            aadharEditText.setError("This is required field (16 digits)");
                        }else {
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Required Fields")
                                    .setMessage("Please provide all required fields.")
                                    .setIcon(R.mipmap.any_service_icon_foreground)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }else{
                    backButton.setEnabled(false);
                    emailEditText.setEnabled(false);
                    fullnameEditText.setEnabled(false);
                    registerButton.setEnabled(false);
                    createProgressBar.setVisibility(View.VISIBLE);
                    AsyncSplitTask task = new AsyncSplitTask();
                    task.execute(null,null,createLinearLayout);
                }

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButton.setEnabled(false);
                backPressed();
            }
        });

        accountTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(sellerRadioButton.isChecked()){
                    sellerLinearLayout.setVisibility(View.VISIBLE);
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Agent Account")
                            .setMessage("if you want to register as an agent, please provide correct information related to Aadhar and Your Company/Shop. Your e-KYC will be activated within minutes.")
                            .setIcon(R.mipmap.any_service_icon_foreground)
                            .setPositiveButton("OK",null)
                            .show();

                }else{
                    imageBack=false;
                    imageFront=false;
                    aadharfrontImageView.setImageResource(R.drawable.ic_menu_camera);
                    aadharbackImageView.setImageResource(R.drawable.ic_menu_camera);
                    aadharEditText.setText("");
                    sellerLinearLayout.setVisibility(View.GONE);

                }
            }
        });

        companylogoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},CAMERA_COMPANY_LOGO);
                    }
                    else
                    {

                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, COMPANY_LOGO);
                    }
                }

            }
        });

        aadharfrontImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
                    }
                    else
                    {
                        cameraIntent("aadharfront",IMAGE_AADHAR_FRONT_CODE);
//                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        startActivityForResult(cameraIntent, IMAGE_AADHAR_FRONT_CODE);
                    }
                }
            }
        });
        aadharbackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
                    }
                    else
                    {
                        cameraIntent("aadharback",IMAGE_AADHAR_BACK_CODE);

//                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        startActivityForResult(cameraIntent, IMAGE_AADHAR_BACK_CODE);
                    }
                }
            }
        });
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        updateUI(account);


        AsyncSplitTask task = new AsyncSplitTask();
        task.execute(logoImageView,loginLinearLayout,split);
    }

    private void updateUI(GoogleSignInAccount account) {
        if(account!=null) {
            fullnameEditText.setText(account.getDisplayName());
            emailEditText.setText(account.getEmail());
            emailEditText.setEnabled(false);
            backButton.setEnabled(false);
            continuePhoneButton.setEnabled(false);
            continueProgressBar.setVisibility(View.VISIBLE);
            AsyncSplitTask task = new AsyncSplitTask();
            task.execute(null, phoneLinearLayout, loginLinearLayout);
            backButton.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        if(state=="root"){
            super.onBackPressed();
        }else{
            backPressed();
        }
    }

    public void backPressed(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(loginLinearLayout.getApplicationWindowToken(),0);
        if(state=="phone"){
            phoneEditText.setText("");
            requestOtpButton.setEnabled(false);
            continuePhoneButton.setEnabled(true);
            continueProgressBar.setVisibility(View.GONE);
            slideUp(null,loginLinearLayout,phoneLinearLayout);
            state="root";
            backButton.setVisibility(View.GONE);
        }else if(state.equals("otp")){
            state = "phone";
//            if(otpTask!=null){
//                otpTask.cancel(true);
//            }
//             do{
                 otpEditText.setText("");
                phoneEditText.setText("");
                phoneEditText.setEnabled(true);
                requestOtpButton.setEnabled(false);
                resendotpTextView.setVisibility(View.GONE);
                otpTextView.setText("Auto Verifying your OTP in ");
                submitProgressBar.setVisibility(View.GONE);
                slideUp(null, phoneLinearLayout, otpLinearLayout);
                state = "phone";
//            }while(!otpTask.isCancelled());

        }else if(state.equals("create_account")){
            createLinearLayout.setVisibility(View.GONE);
            fullnameEditText.setText("");
            fullnameEditText.setEnabled(true);
            emailEditText.setText("");
            emailEditText.setEnabled(true);
            registerButton.setEnabled(false);
            otpEditText.setText("");
            otpEditText.setEnabled(true);
            phoneEditText.setText("");
            phoneEditText.setEnabled(true);
            submitOtpButton.setEnabled(false);
            requestOtpButton.setEnabled(false);
            resendotpTextView.setVisibility(View.GONE);
            otpTextView.setText("Auto Verifying your OTP in ");
            submitProgressBar.setVisibility(View.GONE);
            otpProgressBar.setVisibility(View.GONE);
            slideUp(null, phoneLinearLayout, null);

            imageBack=false;
            imageFront=false;
            aadharfrontImageView.setImageResource(R.drawable.ic_menu_camera);
            aadharbackImageView.setImageResource(R.drawable.ic_menu_camera);
            aadharEditText.setText("");


            companynameEditText.setText("");
            street1EditText.setText("");
            street2EditText.setText("");
            cityEditText.setText("");
            pinEditText.setText("");
            gstEditText.setText("");
            stateAppCompatSpinner.postInvalidate();

            companynameEditText.setEnabled(true);
            street1EditText.setEnabled(true);
            street2EditText.setEnabled(true);
            cityEditText.setEnabled(true);
            pinEditText.setEnabled(true);
            gstEditText.setEnabled(true);
            stateAppCompatSpinner.setEnabled(true);
            sellerLinearLayout.setVisibility(View.GONE);
            customerRadioButton.setChecked(true);
            state="phone";

        }
        backButton.setEnabled(true);
    }

    // slide the view from its current position to below itself
    public void slideUp(final View view, final View llDomestic, final View GONEView){
        final ImageView logoImageView = findViewById(R.id.logo);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if(GONEView!=null) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(GONEView, "translationY", metrics.heightPixels / 2 - logoImageView.getHeight() / 2);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(1000);
            animation.start();
            GONEView.getLayoutParams().height = metrics.heightPixels / 2 - logoImageView.getHeight() / 2;
            GONEView.requestLayout();
            GONEView.setVisibility(View.GONE);
        }


//        Toast.makeText(getApplicationContext(),""+view.getTranslationX()+"\n"+view.getHeight()+"\n"+view.getBaseline(),Toast.LENGTH_LONG).show();
        if(view!=null) {
            ObjectAnimator animation2 = ObjectAnimator.ofFloat(view, "translationY", logoImageView.getHeight() / 2 - metrics.heightPixels / 2);
            animation2.setInterpolator(new AccelerateDecelerateInterpolator());
            animation2.setDuration(1000);
            animation2.start();
            view.getLayoutParams().height = logoImageView.getHeight() / 2 - metrics.heightPixels / 2;
            view.requestLayout();
        }
        if(llDomestic!=null) {
            ObjectAnimator animation2 = ObjectAnimator.ofFloat(llDomestic, "translationY", logoImageView.getHeight() / 2 - metrics.heightPixels / 2);
            animation2.setInterpolator(new AccelerateDecelerateInterpolator());
            animation2.setDuration(1000);
            animation2.start();
            llDomestic.getLayoutParams().height = logoImageView.getHeight() / 2 - metrics.heightPixels / 2;
            llDomestic.requestLayout();
            llDomestic.setVisibility(View.VISIBLE);
        }

    }


    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncSplitTask extends AsyncTask<View,View,String>{
        @Override
        protected void onPreExecute() {
//            super.onPreExecute();
        }

        @Override
        protected String doInBackground(final View... views) {
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            try {
                if(state=="split"){
                        SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
                        String uid = sp.getString("login",null);
                        odooRPC = new OdooRPC();
                        String login = odooRPC.login();
                        Log.d("ODOO RPC :", login);
                        HashMap map = new HashMap<String,String>();
                        String version = BuildConfig.VERSION_NAME;
                        map.put("app_version",version);
                        if(uid!=null){
                            map.put("login",uid);
                        }
                        final HashMap<String,Object> result = (HashMap<String, Object>) odooRPC.callOdoo("res.partner","check_app_details",map);
                        if(result.get("result").equals("Fail") && result.get("code").equals(101)){
                            state="root";
                        }else if(result.get("result").equals("Fail") && result.get("code").equals(103)){
                            Intent intent=new Intent(LoginActivity.this,WelcomeActivity.class);
                            intent.putExtra("msg",result.get("msg").toString());
                            finish();
                            startActivity(intent);
                            return null;
                        }else if(result.get("result").equals("Success")){
                            Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
                            finish();
                            startActivity(intent);
                            return null;

                        }
                        else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(LoginActivity.this)
                                            .setTitle("App Info")
                                            .setMessage(result.get("msg").toString())
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
                            return null;
                        }

                }else if (state.equals("root")){
                    state="phone";
                }else if (state.equals("phone")){
                    user_otp="123456";
                    state="otp";
                }else if (state.equals("otp")){
                    String otp = otpEditText.getText().toString();
                    if(otp.equals(user_otp)) {
                        HashMap map = new HashMap<String,String>();
                        String version = BuildConfig.VERSION_NAME;
                        map.put("phone",phoneEditText.getText().toString());
                        final HashMap<String,Object> result = (HashMap<String, Object>) odooRPC.callOdoo("res.partner","check_user",map);

                        if(result.get("result").equals("Success")){
                            state="done";
                            String uid=result.get("login").toString();
                            SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("login", uid);
                            editor.apply();
                            state = "done";
                            Intent intent=new Intent(LoginActivity.this,WelcomeActivity.class);
                            intent.putExtra("result",true);
                            finish();
                            startActivity(intent);
                            return null;
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    new AlertDialog.Builder(LoginActivity.this)
//                                            .setTitle("App Info")
//                                            .setMessage("ID :"+result.get("login").toString()+"\nName :"+result.get("name").toString())
//                                            .setIcon(R.mipmap.any_service_icon_foreground)
//                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    finishAffinity();
//                                                }
//                                            })
//                                            .setCancelable(false)
//                                            .show();
//                                }
//                            });
                        }else if(result.get("result").equals("Fail") && result.get("code").equals(103)){

                            String uid=result.get("login").toString();
                            SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("login", uid);
                            editor.commit();
                            Intent intent=new Intent(LoginActivity.this,WelcomeActivity.class);
                            intent.putExtra("msg",result.get("msg").toString());
                            finish();
                            startActivity(intent);
                            return null;
                        }else {
                            state = "create_account";
                            final HashMap<String,Object> state_result = (HashMap<String, Object>) odooRPC.callOdoo("res.partner","get_states",new HashMap<String, Object>());
                            if(state_result.get("result").equals("Success")) {
                                final Object[] states = (Object[]) state_result.get("states");


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayAdapter<Object> arrayAdapter = new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_spinner_item, states);
                                        stateAppCompatSpinner.setAdapter(arrayAdapter);
                                        createLinearLayout.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                otpEditText.setEnabled(true);
                                submitOtpButton.setEnabled(true);
                                otpProgressBar.setVisibility(View.GONE);

                                new AlertDialog.Builder(LoginActivity.this)
                                        .setTitle("OTP Validation")
                                        .setMessage("Please enter the Correct OTP")
                                        .setIcon(R.mipmap.any_service_icon_foreground)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        });
                        return null;
                    }
                }else if (state=="create_account"){
                    String uid="";
                    HashMap map = new HashMap<String,String>();
                    String version = BuildConfig.VERSION_NAME;
                    map.put("name",fullnameEditText.getText().toString());
                    map.put("phone",phoneEditText.getText().toString());
                    if(sellerRadioButton.isChecked()){
                        map.put("user_type","agent");
                        map.put("aadhar_number",aadharEditText.getText().toString());
                        map.put("aadhar_front",imageview_to_base64(frontUri));
                        map.put("aadhar_back",imageview_to_base64(backUri));
                        map.put("company_name",companynameEditText.getText().toString());
                        map.put("street1",street1EditText.getText().toString());
                        map.put("street2",street2EditText.getText().toString());
                        map.put("city",cityEditText.getText().toString());
                        map.put("pin",pinEditText.getText().toString());
                        map.put("gst",gstEditText.getText().toString());
                        map.put("logo",fromimageview_to_base64(companylogoImageView));
                        map.put("state",stateAppCompatSpinner.getSelectedItem().toString());
                    }else{
                        map.put("user_type","client");
                    }
                    map.put("email",emailEditText.getText().toString());
                    final HashMap<String,Object> result = (HashMap<String, Object>) odooRPC.callOdoo("res.partner","create_anyservice_partner",map);
                    if(result.get("result").equals("Success")) {
                        uid=result.get("login").toString();
                        SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("login", uid);
                        editor.commit();
                        state = "done";
                        if(sellerRadioButton.isChecked()){
                            Intent intent=new Intent(LoginActivity.this,WelcomeActivity.class);
                            intent.putExtra("msg","Your e-KYC is still Pending");
                            finish();
                            startActivity(intent);
                            return null;

                        }else {

                            Intent intent=new Intent(LoginActivity.this,WelcomeActivity.class);
                            intent.putExtra("result",true);
                            finish();
                            return null;
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    new AlertDialog.Builder(LoginActivity.this)
//                                            .setTitle("App Info")
//                                            .setMessage(result.get("login").toString())
//                                            .setIcon(R.mipmap.any_service_icon_foreground)
//                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    finishAffinity();
//                                                }
//                                            })
//                                            .setCancelable(false)
//                                            .show();
//                                }
//                            });
                        }

                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(LoginActivity.this)
                                        .setTitle("App Info")
                                        .setMessage(result.get("msg").toString())
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
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slideUp(views[0],views[1],views[2]);
                    }
                });

            }catch (Exception e){
                Log.e("ODOO RPC :",e.getMessage());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("App Info")
                                .setMessage("No internet Connection or No server Found.")
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
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            backButton.setEnabled(true);
//            super.onPostExecute(s);
        }
    }
//    private class AsyncOTPTask extends AsyncTask<View,View,String>{
//        @Override
//        protected void onPreExecute() {
////            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(final View... views) {
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
////            super.onPostExecute(s);
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, IMAGE_AADHAR_FRONT_CODE);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }else{
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, CAMERA_COMPANY_LOGO);
                }
                else
                {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }



        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == COMPANY_LOGO && resultCode == Activity.RESULT_OK && data!=null){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            if (selectedImage != null) {
                cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            }
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = 0;
                columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                companylogoImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            }
        }
        if (requestCode == IMAGE_AADHAR_FRONT_CODE && resultCode == Activity.RESULT_OK)
        {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");

            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), frontUri);
                if(photo.getHeight()>photo.getWidth()) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(photo, photo.getWidth(),photo.getHeight(),true);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap,0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight(),matrix,true);
                    aadharfrontImageView.setImageBitmap(rotatedBitmap);
                }else {
                    aadharfrontImageView.setImageBitmap(photo);
                }
                imageFront = true;
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == IMAGE_AADHAR_BACK_CODE && resultCode == Activity.RESULT_OK)
        {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");

            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), backUri);
                if(photo.getHeight()>photo.getWidth()) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(photo, photo.getWidth(),photo.getHeight(),true);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap,0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight(),matrix,true);
                    aadharbackImageView.setImageBitmap(rotatedBitmap);
                }else {
                    aadharbackImageView.setImageBitmap(photo);
                }
                imageBack = true;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("GOOGLE SIGNIN", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void cameraIntent(String name,int code){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, name);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image from Anyservice");
        Uri imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, code);
        if(code==IMAGE_AADHAR_FRONT_CODE){
            frontUri=imageUri;
        }
        else{
            backUri=imageUri;
        }
    }

    private String imageview_to_base64(Uri uri){
        String base64=null;
        String path = getRealPathFromURI(uri);
        Bitmap bitmap= BitmapFactory.decodeFile(path);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] image = stream.toByteArray();
        base64 = Base64.encodeToString(image,0);

        return base64;
    }

    private String fromimageview_to_base64(ImageView iv){
        String base64=null;
        iv.buildDrawingCache();
        Bitmap bitmap=iv.getDrawingCache();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] image = stream.toByteArray();
        base64 = Base64.encodeToString(image,0);

        return base64;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}

