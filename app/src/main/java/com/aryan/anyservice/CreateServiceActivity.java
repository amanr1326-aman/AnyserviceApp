package com.aryan.anyservice;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import adaptor.ServiceDetailsAdaptor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;
import helper.ServiceDetails;

public class CreateServiceActivity extends AppCompatActivity {
    String searchKey=null;
    int lastvisibleItemS=0;
    boolean click=false;
    EditText name,price,description;
    CheckBox measurable,deliverCost;
    AutoCompleteTextView category;
    OdooRPC odooRPC;
    ListView myservices;
    String uid;
    FloatingActionButton actionButton;
    ScrollView layout;
    ImageView icon;
    boolean imageLoaded=false;
    final int SERVICE_ICON=12321;
    ProgressBar progressBar;
    String serviceImage;
    ServiceDetailsAdaptor serviceDetailsAdaptor;
    final int limit=10;
    boolean loading=true,loadNew=false;
    AppCompatEditText searchEditText;
    Timer timer;

    List<ServiceDetails> listService = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_service);
        actionButton= findViewById(R.id.create_service);
        name= findViewById(R.id.name);
        price= findViewById(R.id.price);
        measurable= findViewById(R.id.measurable);
        deliverCost= findViewById(R.id.delivery);
        category= findViewById(R.id.category);
        myservices= findViewById(R.id.myservices);
        icon= findViewById(R.id.logo);
        layout= findViewById(R.id.viewB);
        progressBar= findViewById(R.id.progress);
        description= findViewById(R.id.description);
        searchEditText = findViewById(R.id.search_edittext);
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        HashMap map = new HashMap<String,String>();
        map.put("method","get_categories");
        map.put("model","product.category");
        map.put("all",true);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);

        if(uid==null) {
            SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
            uid = sp.getString("login", null);
        }

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},SERVICE_ICON);
                    }
                    else
                    {
                        ImagePicker.Companion.with(CreateServiceActivity.this)
                                .cropSquare()
                                .compress(1024)
                                .start(SERVICE_ICON);

                    }
                }
            }
        });
        myservices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final ServiceDetails serviceDetails= (ServiceDetails) myservices.getAdapter().getItem(position);
                new AlertDialog.Builder(CreateServiceActivity.this)
                        .setTitle("Delete Service/Product")
                        .setMessage("Do you want to delete - "+serviceDetails.getName())
                        .setIcon(R.mipmap.any_service_icon_foreground)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(uid==null) {
                                    SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
                                    uid = sp.getString("login", null);
                                }
                                HashMap map = new HashMap<String,String>();
                                map.put("method","delete_service");
                                map.put("model","anyservice.service");
                                map.put("id",serviceDetails.getId());
                                map.put("login",uid);

                                AsyncOdooRPCcall task = new AsyncOdooRPCcall();
                                task.execute(map);

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
                return false;
            }
        });
        serviceDetailsAdaptor=new ServiceDetailsAdaptor(getApplicationContext(),R.layout.agent_service_item,listService,false);
        myservices.setAdapter(serviceDetailsAdaptor);
        addUserServices(0,limit,null);
        myservices.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                final int currentFirstVisibleItem = myservices.getFirstVisiblePosition();
                if(currentFirstVisibleItem>lastvisibleItemS){
                    searchEditText.setVisibility(View.GONE);
                }else if(currentFirstVisibleItem<lastvisibleItemS){
                    searchEditText.setVisibility(View.VISIBLE);

                }
                lastvisibleItemS = currentFirstVisibleItem;

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount==totalItemCount-4){
                    if(!loading){
                        loading=true;
                        loadNew=true;
                        progressBar.setVisibility(View.VISIBLE);
                        addUserServices(totalItemCount,limit,searchKey);
                    }
                }
            }
        });
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(searchEditText.getText().length()>=3) {
                    progressBar.setVisibility(View.VISIBLE);
                    loading=true;
                    if(timer!=null)timer.cancel();
                    timer=new Timer();
                    final String key=searchEditText.getText().toString();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            addUserServices(0,limit,key);
                            searchKey=key;
                        }
                    },1000);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(searchEditText.getText().length()==2){
                    progressBar.setVisibility(View.VISIBLE);
                    loading=true;
                    if(timer!=null)timer.cancel();
                    timer=new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            addUserServices(0,limit,null);
                            searchKey=null;
                        }
                    },1000);
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(click) {
            click = false;

            actionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_24));
            layout.setVisibility(View.GONE);
        }else {
            finish();
        }
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
                result = (HashMap<String, Object>) odooRPC.callOdoo(model, method, hashMaps[0]);

                if (result.get("result").equals("Success")) {
                    if (method.equals("get_categories")) {
                        Object[] objects = (Object[]) result.get("categories");
                        final List categoriesResult = Arrays.asList(objects);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> adapter=new ArrayAdapter<>(getApplicationContext(),android.R.layout.select_dialog_item,categoriesResult);
                                category.setThreshold(1);
                                category.setAdapter(adapter);

                                category.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View v, boolean hasFocus) {
                                        if(!hasFocus){
                                            String selection= category.getText().toString();
                                            if(!categoriesResult.contains(selection)){
                                                category.setText("");
                                            }
                                        }
                                    }
                                });
                                actionButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(!click) {
                                            click=true;
                                            imageLoaded=false;
                                            actionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_done_24));
                                            layout.setVisibility(View.VISIBLE);
                                        }else{
                                            if(!name.getText().toString().equals("") && !price.getText().toString().equals("") && !category.getText().toString().equals("") && categoriesResult.contains(category.getText().toString())) {
                                                actionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_24));
                                                click = false;
                                                layout.setVisibility(View.GONE);

                                                if(uid==null) {
                                                    SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
                                                    uid = sp.getString("login", null);
                                                }
                                                HashMap<String, Object> map = new HashMap<String,Object>();
                                                map.put("method","create_service");
                                                map.put("model","anyservice.service");
                                                map.put("name",name.getText().toString());
                                                if(imageLoaded) map.put("image",imageview_to_base64(serviceImage));
                                                map.put("category",category.getText().toString());
                                                map.put("description",description.getText().toString());
                                                map.put("measurable",measurable.isChecked());
                                                map.put("delivery_charge",deliverCost.isChecked());
                                                map.put("login",uid);
                                                map.put("price",Double.parseDouble(price.getText().toString()));
                                                AsyncOdooRPCcall task = new AsyncOdooRPCcall();
                                                task.execute(map);

                                            }else{
                                                if(name.getText().toString().equals("")){
                                                    name.setError("This Field is required");
                                                }else if(price.getText().toString().equals("")){
                                                    price.setError("This Field is required");
                                                }else{
                                                    category.setText("");
                                                    category.setError("Select category from list.");
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        });

                    }else if (method.equals("get_user_services")) {
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
                            serviceDetails.setDeliveryCostable(Boolean.parseBoolean(String.valueOf(service.get("delivery_costable"))));
                            serviceDetails.setIcon(String.valueOf(service.get("image")));
                            serviceDetails.setDescription(String.valueOf(service.get("description")));
                            if (service.get("rating") != null) {
                                Object rating = service.get("rating");
                                serviceDetails.setRating(Float.parseFloat(rating.toString()));
                            }
                            list.add(serviceDetails);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!loadNew){
                                    listService.clear();
                                }else{
                                    loadNew=false;
                                }
                                listService.addAll(list);
                                serviceDetailsAdaptor.notifyDataSetChanged();
                                if(list.size()>=limit){
                                    loading=false;
                                }

                                progressBar.setVisibility(View.GONE);

                            }
                        });

                    }else if(method.equals("create_service") || method.equals("delete_service")){
                        final String msg = (String) result.get("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(layout,msg,Snackbar.LENGTH_LONG).show();

                            }
                        });

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                        finish();
                        overridePendingTransition(0,0);
                        startActivity(getIntent());
                        overridePendingTransition(0,0);
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> stringObjectHashMap) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == SERVICE_ICON)
            {
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "Storage permission granted", Toast.LENGTH_LONG).show();
                    ImagePicker.Companion.with(CreateServiceActivity.this)
                            .cropSquare()
                            .compress(2048)
                            .start(SERVICE_ICON);
                }
                else
                {
                    Toast.makeText(this, "storage permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == SERVICE_ICON && resultCode == Activity.RESULT_OK && data!=null){
            Uri selectedImage = data.getData();
            serviceImage = ImagePicker.Companion.getFilePath(data);
                icon.setImageURI(selectedImage);
                imageLoaded=true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private String imageview_to_base64(String path){
        String base64=null;
        Bitmap bitmap= BitmapFactory.decodeFile(path);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] image = stream.toByteArray();
        base64 = Base64.encodeToString(image,0);

        return base64;
    }

    void addUserServices(int offset,int slimit, String key){
        HashMap map = new HashMap<String,String>();
        map.put("method","get_user_services");
        map.put("model","res.partner");
        map.put("login",uid);
        map.put("agent_id",uid);
        map.put("offset",offset);
        map.put("limit",slimit);
        if(key!=null)
        map.put("key",key);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);
    }
}