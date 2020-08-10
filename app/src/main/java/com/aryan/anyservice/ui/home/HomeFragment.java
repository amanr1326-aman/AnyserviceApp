package com.aryan.anyservice.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aryan.anyservice.CreateServiceActivity;
import com.aryan.anyservice.R;
import com.aryan.anyservice.ServiceActivity;
import com.aryan.anyservice.WalletActivity;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import adaptor.ServiceDetailsAdaptor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import helper.IconTextView;
import helper.ServiceDetails;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    AppCompatEditText searchEditText;
    AppCompatSpinner categories,sortyby;
    SwitchCompat active;
    int radius=2;
    ProgressBar progressBar;
    final int WALLET_UPDATE=151;
    boolean isagent=false;
    IconTextView wallet;
    RadioButton productradio;
    RadioButton serviceradio;
    ArrayList<ServiceDetails> serviceDetailsList=new ArrayList<>();
    ArrayList<ServiceDetails> productList=new ArrayList<>();
    ListView serviceListView,productListView;
    ServiceDetailsAdaptor serviceDetailsAdaptor;
    ServiceDetailsAdaptor productAdaptor;
    final int limit=10;
    HashMap<String,String> lastmap;
    boolean loading=true,loadNew=false;
    int lastvisibleItemP=0,lastvisibleItemS=0;
    int key=0;
    ShowcaseView sv;
    Timer timer=null;

    HashMap<String,String> SORTBY = new HashMap<String, String>(){{
        put("Relevance","id desc");
        put("Price Low to High","price asc");
        put("Price High to Low","price desc");
    }};


    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        final ScrollView sellerLayout = root.findViewById(R.id.seller_layout);
        final LinearLayout customerLayout = root.findViewById(R.id.customer_layout);
        final ProgressBar mainProgressBar = root.findViewById(R.id.main_progress);
        serviceListView = root.findViewById(R.id.services_listview);
        productListView = root.findViewById(R.id.product_listview);
        wallet = root.findViewById(R.id.wallet);
        productradio = root.findViewById(R.id.product_radio);
        serviceradio = root.findViewById(R.id.service_radio);

        searchEditText = root.findViewById(R.id.search_edittext);

        serviceDetailsAdaptor = new ServiceDetailsAdaptor(getContext(), R.layout.search_service_list_item, serviceDetailsList,false);
        productAdaptor = new ServiceDetailsAdaptor(getContext(), R.layout.product_item, productList,false);
        serviceListView.setAdapter(serviceDetailsAdaptor);
        productListView.setAdapter(productAdaptor);
        serviceListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                final int currentFirstVisibleItem = serviceListView.getFirstVisiblePosition();
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
                        homeViewModel.updateServices(lastmap,totalItemCount,limit,SORTBY.get(sortyby.getSelectedItem().toString()));
                    }
                }

            }
        });

        productListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                final int currentFirstVisibleItem = productListView.getFirstVisiblePosition();
                if(currentFirstVisibleItem>lastvisibleItemP){
                    searchEditText.setVisibility(View.GONE);
                }else if(currentFirstVisibleItem<lastvisibleItemP){
                    searchEditText.setVisibility(View.VISIBLE);

                }
                lastvisibleItemP = currentFirstVisibleItem;

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount==totalItemCount-3){
                    if(!loading){
                        loading=true;
                        loadNew=true;
                        progressBar.setVisibility(View.VISIBLE);
                        homeViewModel.updateServices(lastmap,totalItemCount,limit,SORTBY.get(sortyby.getSelectedItem().toString()));
                    }
                }

            }
        });

        final TextView warning = root.findViewById(R.id.warning);

        productradio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                productradio.setEnabled(false);
                serviceradio.setEnabled(false);
                if(sv!=null){
                    if(sv.isShowing()) sv.hide();
                }

                if(productradio.isChecked()){
                    productList.clear();
                    productList.addAll(serviceDetailsList);
                    serviceListView.setVisibility(View.GONE);
                    productListView.setVisibility(View.VISIBLE);
                    productAdaptor.notifyDataSetChanged();
                    productradio.setEnabled(true);
                    serviceradio.setEnabled(true);
                }else{
                    serviceDetailsList.clear();
                    serviceDetailsList.addAll(productList);
                    productListView.setVisibility(View.GONE);
                    serviceListView.setVisibility(View.VISIBLE);
                    serviceDetailsAdaptor.notifyDataSetChanged();
                    productradio.setEnabled(true);
                    serviceradio.setEnabled(true);


                }

            }
        });
        homeViewModel.getWalletamount().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                wallet.setText(String.format("%s %s", getResources().getString(R.string.icon_wallet), aDouble));
                if(aDouble<0){
                    warning.setVisibility(View.VISIBLE);
                    warning.setTextColor(Color.RED);
                }
            }
        });
        wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getContext(), WalletActivity.class);
                startActivityForResult(intent,WALLET_UPDATE);

            }
        });
        progressBar = root.findViewById(R.id.progress);
        homeViewModel.getIsAgent().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                isagent=aBoolean;
                if(aBoolean){
                    customerLayout.setVisibility(View.GONE);
                    sellerLayout.setVisibility(View.VISIBLE);
                    Button distance = root.findViewById(R.id.distance);
                    Button services = root.findViewById(R.id.services);
                    active= root.findViewById(R.id.active);
                    final DrawerLayout scanner = root.findViewById(R.id.scanner);
                    final Animation rotate = AnimationUtils.loadAnimation(getContext(),R.anim.clockwise);
                    homeViewModel.getActiveAgent().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean aBoolean) {
                            if(aBoolean!=active.isChecked()){
                                active.setChecked(aBoolean);
                            }
                        }
                    });
                    active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked){
                                scanner.startAnimation(rotate);
                            }else{
                                scanner.clearAnimation();
                            }
                            homeViewModel.setActive(isChecked);
                        }
                    });

                    distance.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateDistance(false);
                        }
                    });
                    services.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(getContext(), CreateServiceActivity.class);
                            startActivity(intent);
                        }
                    });

                    sv = new ShowcaseView.Builder(getActivity())
                            .setTarget(new ViewTarget(distance))
                            .setContentTitle("Services in Local Area!")
                            .setContentText("Hi!.\nClick here to update the service radius to search customers.\nHere, You can select unlimited distance. \nif you can sell your product all over india.\n And You have delivery facility.")
                            .singleShot(202) // provide a unique ID used to ensure it is only shown once
                            .withHoloShowcase()
                            .setStyle(R.style.showcaseTheme)
                            .build();

                }else{
                    customerLayout.setVisibility(View.VISIBLE);
                    sellerLayout.setVisibility(View.GONE);

                    categories = root.findViewById(R.id.categories_spinner);
                    sortyby = root.findViewById(R.id.sort_spinner);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, new String[]{"Relevance","Price Low to High","Price High to Low"});
                    sortyby.setAdapter(arrayAdapter);
                    sortyby.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if(!sortyby.getSelectedItem().toString().equals("")){
                                progressBar.setVisibility(View.VISIBLE);
                                HashMap<String,String> map=new HashMap<>();
                                if(categories.getSelectedItem()!=null) {
                                    map.put("categ", categories.getSelectedItem().toString());
                                }else{
                                    map.put("categ", "All");

                                }
                                loading=true;
                                key++;
                                homeViewModel.updateServices(map,0,limit,SORTBY.get(sortyby.getSelectedItem().toString()));

                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    final IconTextView distance = root.findViewById(R.id.distance_icon_textview);

                    distance.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateDistance(true);
                        }
                    });

                    serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent=new Intent(getContext(), ServiceActivity.class);
                            ServiceDetails service = (ServiceDetails) serviceListView.getAdapter().getItem(position);

                            intent.putExtra("service",service);
                            intent.putExtra("agent_id",service.getAgent_id());
                            intent.putExtra("imageview",false);
                            startActivity(intent);
                        }
                    });


                    categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            searchEditText.setText("");
                            key++;
                            HashMap<String,String> map=new HashMap<>();
                            map.put("categ",categories.getSelectedItem().toString());
                            progressBar.setVisibility(View.VISIBLE);

                            loading=true;
                            homeViewModel.updateServices(map,0,limit,SORTBY.get(sortyby.getSelectedItem().toString()));
                            lastmap = map;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    searchEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if(searchEditText.getText().length()>=3) {
                                final HashMap<String, String> map = new HashMap<>();
                                map.put("key", searchEditText.getText().toString());
                                progressBar.setVisibility(View.VISIBLE);
                                loading=true;
                                if(timer!=null)timer.cancel();
                                timer=new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        homeViewModel.updateServices(map,0,limit,SORTBY.get(sortyby.getSelectedItem().toString()));
                                        key++;
                                    }
                                },1000);
                                lastmap = map;
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if(searchEditText.getText().length()==2){
                                final HashMap<String, String> map = new HashMap<>();
                                map.put("categ", categories.getSelectedItem().toString());
                                progressBar.setVisibility(View.VISIBLE);
                                loading=true;
                                if(timer!=null)timer.cancel();
                                timer=new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        homeViewModel.updateServices(map,0,limit,SORTBY.get(sortyby.getSelectedItem().toString()));
                                        key++;
                                    }
                                },1000);
                                lastmap = map;
                            }

                        }
                    });

                    homeViewModel.getServices().observe(getViewLifecycleOwner(), new Observer<List<ServiceDetails>>() {
                        @Override
                        public void onChanged(List<ServiceDetails> list) {
                            if(key>0)
                            key--;
                            if (key==0) {
                                progressBar.setVisibility(View.GONE);
                                if (productradio.isChecked()) {
                                    if (!loadNew) {
                                        productList.clear();
                                        productListView.getHandler().post(new Runnable() {
                                            @Override
                                            public void run() {

                                                productListView.setSelectionAfterHeaderView();
                                            }
                                        });
                                    } else {
                                        loadNew = false;
                                    }
                                    productList.addAll(list);
                                    productAdaptor.notifyDataSetChanged();

                                } else {
                                    if (!loadNew) {
                                        serviceDetailsList.clear();
                                        serviceListView.getHandler().post(new Runnable() {
                                            @Override
                                            public void run() {

                                                serviceListView.setSelectionAfterHeaderView();
                                            }
                                        });
                                    } else {
                                        loadNew = false;
                                    }
                                    serviceDetailsList.addAll(list);
                                    serviceDetailsAdaptor.notifyDataSetChanged();
                                    serviceListView.setSelectionAfterHeaderView();

                                }
                                if (list.size() >= limit) {
                                    loading = false;
                                }

                            }
                        }
                    });
                    homeViewModel.getCategories().observe(getViewLifecycleOwner(), new Observer<List>() {
                        @Override
                        public void onChanged(List arrayList) {
                            ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, arrayList);
                            categories.setAdapter(arrayAdapter);
                        }
                    });
                    sv = new ShowcaseView.Builder(getActivity())
                            .setTarget(new ViewTarget(distance))
                            .setContentTitle("Services in Local Area!")
                            .setContentText("Hi!.\nClick here to change the distance for your local services.\nStill, Services and Products provided in unlimited distance will be available to you after any distance restrictions provided by you.")
                            .singleShot(201) // provide a unique ID used to ensure it is only shown once
                            .withHoloShowcase()
                            .setStyle(R.style.showcaseTheme)
                            .build();
                    if(sv==null){
                        sv = new ShowcaseView.Builder(getActivity())
                                .setTarget(new ViewTarget(serviceradio))
                                .setContentTitle("Service View")
                                .setContentText("Here, You can switch to service view. if you dont need pictures of services or products")
                                .singleShot(204) // provide a unique ID used to ensure it is only shown once
                                .withHoloShowcase()
                .setStyle(R.style.showcaseTheme)
                                .build();

                    }else{
                        if(!sv.isShowing()){
                            sv = new ShowcaseView.Builder(getActivity())
                                    .setTarget(new ViewTarget(serviceradio))
                                    .setContentTitle("Service View")
                                    .setContentText("Here, You can switch to service view. if you dont need pictures of services or products")
                                    .singleShot(204) // provide a unique ID used to ensure it is only shown once
                                    .withHoloShowcase()
                .setStyle(R.style.showcaseTheme)
                                    .build();
                        }
                    }

                }

                mainProgressBar.setVisibility(View.GONE);
            }
        });
        homeViewModel.getRadius().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                radius =integer;
                if(customerLayout.getVisibility()==View.VISIBLE) {
                    Toast.makeText(getContext(),"Searching services in "+integer+" Km.",Toast.LENGTH_SHORT).show();
                } else{
                    if(active.isChecked()){
                        Toast.makeText(getContext(),"Searching customers in "+integer+" Km.",Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
        homeViewModel.getText(getContext()).observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        homeViewModel.getToastText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getContext(),s,Toast.LENGTH_SHORT).show();

            }
        });
        return root;
    }
    void updateDistance(final boolean updateSearch){
        if(sv!=null){
            if(sv.isShowing()) sv.hide();
            sv=null;
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout = inflater.inflate(R.layout.distance_dialog_layout,null);
        TextView left=layout.findViewById(R.id.left_text);
        TextView right=layout.findViewById(R.id.right_text);
        final CheckBox unlimitedDistance = layout.findViewById(R.id.unlimited_check);
        final LinearLayout unlimitedLayout = layout.findViewById(R.id.unlimited_layout);
        final LinearLayout seekbarLayout = layout.findViewById(R.id.seekbar_layout);
        final TextView value_text=layout.findViewById(R.id.text_value);
        final TextView chargeTextView=layout.findViewById(R.id.charge_textview);
        final SeekBar seekBar=layout.findViewById(R.id.distance_seekbar);
        final EditText deliveryCharge = layout.findViewById(R.id.visiting_charge);
        if(isagent){
            unlimitedDistance.setVisibility(View.VISIBLE);
            homeViewModel.getUnlimitedDistance().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    unlimitedDistance.setChecked(aBoolean);

                }
            });
            homeViewModel.getDeliveryCharge().observe(getViewLifecycleOwner(), new Observer<Double>() {
                @Override
                public void onChanged(Double aDouble) {
                    deliveryCharge.setText(String.valueOf(aDouble));
                    if(unlimitedDistance.isChecked()){
                        seekbarLayout.setVisibility(View.GONE);
                        unlimitedLayout.setVisibility(View.VISIBLE);
                    }else{
                        seekbarLayout.setVisibility(View.VISIBLE);
                        unlimitedLayout.setVisibility(View.GONE);
                    }
                }
            });
        }
        unlimitedDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(sv!=null){
                    if(sv.isShowing()) sv.hide();
                    sv=null;
                }
                if(unlimitedDistance.isChecked()){
                    seekbarLayout.setVisibility(View.GONE);
                    unlimitedLayout.setVisibility(View.VISIBLE);
                }else{
                    seekbarLayout.setVisibility(View.VISIBLE);
                    unlimitedLayout.setVisibility(View.GONE);
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMin(2);
            left.setText("2 Km");
        }else{
            left.setText("1 Km");
        }
        seekBar.setMax(25);
        right.setText("25 Km");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seekBar.setProgress(radius,true);
        }else{
            seekBar.setProgress(radius);
        }
        int visitCharge = 10+radius*4;
        chargeTextView.setText("Expected Visit charge Rs "+visitCharge+".");
        value_text.setText(radius+" Km");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value_text.setText(progress+" km");
                int visitCharge = 10+progress*4;
                chargeTextView.setText("Expected Visit charge Rs "+visitCharge+".");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        new AlertDialog.Builder(getContext())
                .setTitle(" Service Distance")
                .setMessage("Services in distance(km)")
                .setIcon(R.mipmap.any_service_icon_foreground)
                .setView(layout)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!unlimitedDistance.isChecked()) {
                            homeViewModel.setRadius(seekBar.getProgress(),false,0.0);
                        }else{
                            homeViewModel.setRadius(seekBar.getProgress(),unlimitedDistance.isChecked(), Double.parseDouble(deliveryCharge.getText().toString()));

                        }
                        if(isagent) {
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            fragmentTransaction.detach(HomeFragment.this).attach(HomeFragment.this).commit();
                        }else{
                            if (updateSearch) {
                                if (searchEditText.getText().length() >= 3) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("key", searchEditText.getText().toString());
                                    progressBar.setVisibility(View.VISIBLE);

                                    loading=true;
                                    key++;
                                    homeViewModel.updateServices(map,0,limit,SORTBY.get(sortyby.getSelectedItem().toString()));
                                    lastmap = map;
                                } else {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("categ", categories.getSelectedItem().toString());
                                    progressBar.setVisibility(View.VISIBLE);
                                    loading=true;
                                    key++;
                                    homeViewModel.updateServices(map,0,limit,SORTBY.get(sortyby.getSelectedItem().toString()));
                                    lastmap = map;
                                }
                            }

                        }
                    }
                })
                .setNegativeButton("Cancel",null)
                .show();
    }

}