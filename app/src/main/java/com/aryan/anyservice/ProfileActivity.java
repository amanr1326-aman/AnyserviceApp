package com.aryan.anyservice;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;

public class ProfileActivity extends AppCompatActivity {
    OdooRPC odooRPC;
    Button registerButton;
    ProgressBar progressBar;
    RadioButton customerRadioButton,sellerRadioButton;
    ImageView companylogoImageView;
    LinearLayout sellerLayout;
    Button edittButton,logout,contactus;
    EditText stateEditText,fullnameEditText,emailEditText,aadharEditText,companynameEditText,street1EditText,street2EditText,cityEditText,gstEditText,pinEditText;
    String uid;
    boolean imageLoaded=false;
    TextView instructionTv;
    final int SERVICE_ICON=12321;

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fullnameEditText = findViewById(R.id.fullname_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        aadharEditText = findViewById(R.id.aadhar_edittext);
        companynameEditText = findViewById(R.id.company_name);
        street1EditText = findViewById(R.id.street1);
        street2EditText = findViewById(R.id.street2);
        cityEditText = findViewById(R.id.city);
        gstEditText = findViewById(R.id.gst);
        pinEditText = findViewById(R.id.pin);
        logout = findViewById(R.id.logout);
        contactus = findViewById(R.id.contactus);

        stateEditText = findViewById(R.id.state);

        customerRadioButton = findViewById(R.id.customer_radiobutton);
        sellerRadioButton = findViewById(R.id.seller_radionbutton);

        companylogoImageView = findViewById(R.id.company_logo);
        progressBar = findViewById(R.id.progressbar);

        registerButton = findViewById(R.id.submit);
        edittButton = findViewById(R.id.edit_button);

        sellerLayout =findViewById(R.id.seller_layout);
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Do you want to Logout?")
                        .setMessage("Are You sure you want to logout from the system?")
                        .setIcon(R.mipmap.any_service_icon_foreground)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.clear();
                                editor.apply();
                                finishAffinity();
                                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                                startActivity(intent);

                            }
                        })
                        .setNegativeButton("No",null)
                        .setCancelable(false)
                        .show();
            }
        });
        contactus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setTitle("Need Help ?")
                            .setMessage("Contact Us for Any Query/Feedback\nDeveloper - Aman Kumar\nMobile - +918218781495\nEmail - amanr1326@gmail.com")
                            .setIcon(R.mipmap.any_service_icon_foreground)
                            .setPositiveButton("OK", null)
                            .setCancelable(false)
                            .show();
            }
        });


        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        edittButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edittButton.setVisibility(View.GONE);
                fullnameEditText.setEnabled(true);
                emailEditText.setEnabled(true);
                street1EditText.setEnabled(true);
                street2EditText.setEnabled(true);
                pinEditText.setEnabled(true);
                registerButton.setVisibility(View.VISIBLE);
                gstEditText.setEnabled(true);
                companylogoImageView.setEnabled(true);
                companynameEditText.setEnabled(true);
            }
        });
        companylogoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},SERVICE_ICON);
                    }
                    else
                    {

                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, SERVICE_ICON);
                    }
                }

            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((customerRadioButton.isChecked() && !fullnameEditText.getText().toString().equals(""))||(!pinEditText.getText().equals("") && !street2EditText.getText().toString().equals("") && !street1EditText.getText().toString().equals("") && !fullnameEditText.getText().toString().equals(""))) {
                    emailEditText.setEnabled(false);
                    gstEditText.setEnabled(false);
                    fullnameEditText.setEnabled(false);
                    street1EditText.setEnabled(false);
                    street2EditText.setEnabled(false);
                    pinEditText.setEnabled(false);
                    companylogoImageView.setEnabled(false);
                    companynameEditText.setEnabled(false);
                    registerButton.setVisibility(View.GONE);
                    edittButton.setVisibility(View.VISIBLE);
                    HashMap map = new HashMap<String,String>();
                    map.put("method","update_user_details");
                    map.put("model","res.partner");
                    map.put("login",uid);
                    map.put("email",emailEditText.getText().toString());
                    map.put("name",fullnameEditText.getText().toString());
                    if(sellerRadioButton.isChecked()) {
                        map.put("company", companynameEditText.getText().toString());
                        map.put("street", street1EditText.getText().toString());
                        map.put("street2", street2EditText.getText().toString());
                        map.put("zip", pinEditText.getText().toString());
                        map.put("vat", gstEditText.getText().toString());
                        map.put("login", uid);
                        if (imageLoaded) {
                            map.put("image", fromimageview_to_base64(companylogoImageView));
                        }
                    }
                    AsyncOdooRPCcall task = new AsyncOdooRPCcall();
                    task.execute(map);

                } else {

                        new AlertDialog.Builder(ProfileActivity.this)
                                .setTitle("Required Fields")
                                .setMessage("Please provide all required fields.")
                                .setIcon(R.mipmap.any_service_icon_foreground)
                                .setPositiveButton("OK", null)
                                .show();
                    }

            }
        });
        sellerRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(sellerRadioButton.isChecked()){
                    sellerLayout.setVisibility(View.VISIBLE);
                }else{
                    sellerLayout.setVisibility(View.GONE);
                }
            }
        });
        if(uid==null) {
            SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
            uid = sp.getString("login", null);
        }


        instructionTv = findViewById(R.id.instruction_tv);
        

        HashMap map = new HashMap<String,String>();
        map.put("method","get_user_details");
        map.put("model","res.partner");
        map.put("login",uid);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
        companylogoImageView.setEnabled(false);
        HashMap map2 = new HashMap<String,String>();
        map2.put("method","get_instructions");
        map2.put("model","res.partner");
        AsyncOdooRPCcall task2 = new AsyncOdooRPCcall();
        task2.execute(map2);
    }

    class AsyncOdooRPCcall extends AsyncTask<HashMap<String,Object>, ProgressBar,HashMap<String,Object>> {
        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                        final HashMap<String, Object> finalResult = result;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fullnameEditText.setText(String.valueOf(finalResult.get("name")));
                                emailEditText.setText(String.valueOf(finalResult.get("email")));
                                street1EditText.setText(String.valueOf(finalResult.get("street1")));
                                street2EditText.setText(String.valueOf(finalResult.get("street2")));
                                cityEditText.setText(String.valueOf(finalResult.get("city")));
                                stateEditText.setText(String.valueOf(finalResult.get("state")));
                                aadharEditText.setText(String.valueOf(finalResult.get("aadhar")));
                                gstEditText.setText(String.valueOf(finalResult.get("gst")));
                                companynameEditText.setText(String.valueOf(finalResult.get("company")));
                                sellerRadioButton.setChecked(Boolean.parseBoolean(String.valueOf(finalResult.get("agent"))));
                                stateEditText.setText(String.valueOf(finalResult.get("state")));
                                try {
                                    byte[] decodedString = Base64.decode(String.valueOf(finalResult.get("image")), Base64.DEFAULT);
                                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    companylogoImageView.setImageBitmap(decodedBitmap);
                                }catch(Exception e){

                                }

                            }
                        });
                    } else if (method.equals("update_user_details")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(fullnameEditText,"Details Updated succesfully.",Snackbar.LENGTH_LONG).show();

                            }
                        });


                    }else if (method.equals("get_instructions")) {
                        final HashMap<String, Object> finalResult = result;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                instructionTv.setText(String.valueOf(finalResult.get("instruction")));
                            }
                        });

                    }
                }
            }catch(Exception e){
                e.printStackTrace();

            }

            return result;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> stringObjectHashMap) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == SERVICE_ICON && resultCode == Activity.RESULT_OK && data!=null){
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
                imageLoaded=true;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

}