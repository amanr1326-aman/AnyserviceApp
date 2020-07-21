package com.aryan.anyservice.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import adaptor.ServiceDetailsAdaptor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import helper.IconTextView;
import helper.ServiceDetails;

import com.aryan.anyservice.HomeActivity;
import com.aryan.anyservice.LoginActivity;
import com.aryan.anyservice.R;
import com.aryan.anyservice.ServiceActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    AppCompatEditText searchEditText;
    AppCompatSpinner categories;
    SwitchCompat active;
    int radius=2;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        final LinearLayout sellerLayout = root.findViewById(R.id.seller_layout);
        final LinearLayout customerLayout = root.findViewById(R.id.customer_layout);
        homeViewModel.getIsAgent().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    customerLayout.setVisibility(View.GONE);
                    sellerLayout.setVisibility(View.VISIBLE);
                    Button distance = root.findViewById(R.id.distance);
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

                }else{
                    customerLayout.setVisibility(View.VISIBLE);
                    sellerLayout.setVisibility(View.GONE);

                    categories = root.findViewById(R.id.categories_spinner);
                    searchEditText = root.findViewById(R.id.search_edittext);
                    final ListView serviceListView = root.findViewById(R.id.services_listview);
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

                            intent.putExtra("id",service.getId());
                            intent.putExtra("agent_id",service.getAgent_id());
                            startActivity(intent);
                        }
                    });
                    categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            HashMap<String,String> map=new HashMap<>();
                            map.put("categ",categories.getSelectedItem().toString());
                            homeViewModel.updateServices(map);
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
                                HashMap<String, String> map = new HashMap<>();
                                map.put("key", searchEditText.getText().toString());
                                homeViewModel.updateServices(map);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if(searchEditText.getText().length()==2){
                                HashMap<String, String> map = new HashMap<>();
                                map.put("categ", categories.getSelectedItem().toString());
                                homeViewModel.updateServices(map);
                            }

                        }
                    });

                    homeViewModel.getServices().observe(getViewLifecycleOwner(), new Observer<List<ServiceDetails>>() {
                        @Override
                        public void onChanged(List<ServiceDetails> list) {
                            ServiceDetailsAdaptor serviceDetailsAdaptor=new ServiceDetailsAdaptor(getContext(),R.layout.search_service_list_item,list);
                            serviceListView.setAdapter(serviceDetailsAdaptor);
                        }
                    });
                    homeViewModel.getCategories().observe(getViewLifecycleOwner(), new Observer<List>() {
                        @Override
                        public void onChanged(List arrayList) {
                            ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, arrayList);
                            categories.setAdapter(arrayAdapter);
                        }
                    });

                }
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
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout = inflater.inflate(R.layout.distance_dialog_layout,null);
        TextView left=layout.findViewById(R.id.left_text);
        TextView right=layout.findViewById(R.id.right_text);
        final TextView value_text=layout.findViewById(R.id.text_value);
        final TextView chargeTextView=layout.findViewById(R.id.charge_textview);
        final SeekBar seekBar=layout.findViewById(R.id.distance_seekbar);
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
                .setTitle("Distance")
                .setMessage("Services in distance(km)")
                .setIcon(R.mipmap.any_service_icon_foreground)
                .setView(layout)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        homeViewModel.setRadius(seekBar.getProgress());
                        if(updateSearch) {
                            if (searchEditText.getText().length() >= 3) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("key", searchEditText.getText().toString());
                                homeViewModel.updateServices(map);
                            } else {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("categ", categories.getSelectedItem().toString());
                                homeViewModel.updateServices(map);
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel",null)
                .show();
    }
}