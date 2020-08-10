package com.aryan.anyservice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;

public class HomeActivity extends AppCompatActivity {
    AppCompatSpinner locationSpinner;
    final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1011;
    final int WALLET_UPDATE=151;
    GoogleApiClient googleApiClient;
    boolean app_startup = true;
    String uid;
    ProgressBar locationProgressBar;
    TextView refreshTextView,userprofileTextView,wallet;

    OdooRPC odooRPC;

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        requestPermission();
        if(!isMyServiceRunning(NotifyService.class)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(HomeActivity.this, NotifyService.class));
            } else {
                startService(new Intent(HomeActivity.this, NotifyService.class));
            }
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.top_menu_layout);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_SHOW_HOME);
            locationSpinner = actionBar.getCustomView().findViewById(R.id.location_spinner);
            locationSpinner.setEnabled(false);
            locationSpinner.setClickable(false);
            userprofileTextView = actionBar.getCustomView().findViewById(R.id.user_icon_textview);
            refreshTextView = actionBar.getCustomView().findViewById(R.id.refresh_icon_textview);
            locationProgressBar = actionBar.getCustomView().findViewById(R.id.refresh_progress);
            wallet = actionBar.getCustomView().findViewById(R.id.wallet);
            wallet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(getApplicationContext(), WalletActivity.class);
                    startActivityForResult(intent,WALLET_UPDATE);

                }
            });
            userprofileTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent=new Intent(getApplicationContext(),ProfileActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            });


            refreshTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshTextView.setVisibility(View.GONE);
                                    locationProgressBar.setVisibility(View.VISIBLE);
                                    locationSpinner.setVisibility(View.GONE);

                                }
                            });
                            updateLocationOnUI();
                        }
                    }).start();
                }
            });

        }


        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        if(uid==null) {
            SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
            uid = sp.getString("login", null);
        }
        HashMap map = new HashMap<String,String>();
        map.put("method","get_user_details");
        map.put("model","res.partner");
        map.put("login",uid);
        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);

    }

    private void updateLocationOnUI() {

        Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLastLocation != null) {
            try {
                final List<Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()


                if(uid==null) {
                    SharedPreferences sp = getApplicationContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
                    uid = sp.getString("login", null);
                }
                HashMap map = new HashMap<String,String>();
                map.put("method","update_location");
                map.put("login",uid);
                map.put("place",address);
                map.put("lat",mLastLocation.getLatitude());
                map.put("long",mLastLocation.getLongitude());
                final HashMap<String,Object> result = (HashMap<String, Object>) new AsyncOdooRPCcall().execute(map).get();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<String> arrayList=new ArrayList<>();
                        for(Address value:addresses){
                            arrayList.add(value.getAddressLine(0).toString());
                        }
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(HomeActivity.this, R.layout.spinner_item, arrayList);
                        locationSpinner.setAdapter(arrayAdapter);
                        refreshTextView.setVisibility(View.VISIBLE);
                        locationProgressBar.setVisibility(View.GONE);
                        locationSpinner.setVisibility(View.VISIBLE);

                    }
                });

            }catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void updateLocation() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                requestPermission();
                                return;
                            }
                            if(app_startup) {
                                updateLocationOnUI();
                            }

                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        }

        googleApiClient.connect();
    }

    private void requestPermission() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 121);
            }
        }else{
            updateLocation();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 121) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "This app requires location permissions to detect your location!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                updateLocation();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    class AsyncOdooRPCcall extends AsyncTask<HashMap<String,Object>,ProgressBar,HashMap<String,Object>>{

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
                hashMaps[0].remove("method");
                result = (HashMap<String, Object>) odooRPC.callOdoo("res.partner", method, hashMaps[0]);
                if (method.equals("get_user_details") && result!=null) {
                    final HashMap<String, Object> finalResult = result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wallet.setText(String.format("%s %s", getResources().getString(R.string.icon_wallet), String.valueOf(finalResult.get("balance"))));
                        }
                    });

                }
            }catch (Exception e){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(HomeActivity.this)
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == WALLET_UPDATE || requestCode == 121) {

            HashMap map = new HashMap<String,String>();
            map.put("method","get_user_details");
            map.put("model","res.partner");
            map.put("login",uid);
            AsyncOdooRPCcall task = new AsyncOdooRPCcall();
            task.execute(map);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}