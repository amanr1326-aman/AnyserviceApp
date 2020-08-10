package com.aryan.anyservice.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aryan.anyservice.OrderStatusActivity;
import com.aryan.anyservice.R;

import java.util.ArrayList;
import java.util.List;

import adaptor.OrderViewAdaptor;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import helper.Order;
import helper.Utility;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    boolean active=false;
    ProgressBar progressBar;
    List<Order> openOrdersList = new ArrayList<>();
    List<Order> doneOrdersList = new ArrayList<>();
    OrderViewAdaptor openadaptor;
    OrderViewAdaptor doneadaptor;
    final int limit=10;
    boolean loadingopen=true,loadingdone=true,loadnewOpen=false,loadnewDone=false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final ListView openOrders = root.findViewById(R.id.open_services);
        final ListView doneOrders = root.findViewById(R.id.done_services);
        final SwipeRefreshLayout swipeRefreshLayout= root.findViewById(R.id.refresh_layout);
        progressBar= root.findViewById(R.id.progress);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.VISIBLE);
                loadingopen=true;
                loadingdone=true;
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.detach(DashboardFragment.this).attach(DashboardFragment.this).commit();

            }
        });
        SharedPreferences sp = getContext().getSharedPreferences("odoologin", Context.MODE_PRIVATE);
        final String uid = sp.getString("login",null);
        openadaptor = new OrderViewAdaptor(getContext(),R.layout.order_item, openOrdersList,uid);
        doneadaptor = new OrderViewAdaptor(getContext(),R.layout.order_item, doneOrdersList,uid);
        openOrders.setAdapter(openadaptor);
        doneOrders.setAdapter(doneadaptor);
        final ScrollView scrollView = root.findViewById(R.id.scrollview);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if(scrollView.getChildAt(0).getBottom()<=scrollView.getHeight()+scrollView.getScrollY()){
                    if(!loadingopen){
                        loadingopen=true;
                        loadnewOpen=true;
                        progressBar.setVisibility(View.VISIBLE);
                        dashboardViewModel.addOpenOrder(getContext(),openOrdersList.size(),limit);
                    }
                    if(!loadingdone){
                        loadingdone=true;
                        loadnewDone=true;
                        progressBar.setVisibility(View.VISIBLE);
                        dashboardViewModel.addDoneOrder(getContext(),doneOrdersList.size(),limit);
                    }

                }
            }
        });

        dashboardViewModel.getOpenServices().observe(getViewLifecycleOwner(), new Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> orders) {

                if(!loadnewOpen){
                    openOrdersList.clear();
                }else{
                    loadnewOpen=false;
                }
                openOrdersList.addAll(orders);
                openadaptor.notifyDataSetChanged();
                if(orders.size()>=limit){
                    loadingopen=false;
                }
                if(orders.size()>0){
                    TextView empty = getActivity().findViewById(R.id.empty);
                    empty.setVisibility(View.GONE);
                }
                Utility.setListViewHeightBasedOnChildren(openOrders);
                progressBar.setVisibility(View.GONE);
            }
        });
        dashboardViewModel.getDoneServices().observe(getViewLifecycleOwner(), new Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> orders) {

                if(!loadnewDone){
                    doneOrdersList.clear();
                }else{
                    loadnewDone=false;
                }
                doneOrdersList.addAll(orders);
                doneadaptor.notifyDataSetChanged();
                if(orders.size()>=limit){
                    loadingdone=false;
                }
                Utility.setListViewHeightBasedOnChildren(doneOrders);
                progressBar.setVisibility(View.GONE);

                if(orders.size()>0){
                    TextView empty = getActivity().findViewById(R.id.empty2);
                    empty.setVisibility(View.GONE);
                }
            }
        });
        openOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order= (Order) openOrders.getAdapter().getItem(position);
                Intent intent =new Intent(getContext(),OrderStatusActivity.class);
                intent.putExtra("order",order);
                startActivityForResult(intent,121);
            }
        });
        doneOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order= (Order) doneOrders.getAdapter().getItem(position);
                Intent intent =new Intent(getContext(),OrderStatusActivity.class);
                intent.putExtra("order",order);
                startActivityForResult(intent,121);
            }
        });
        dashboardViewModel.addDoneOrder(getContext(),0,limit);
        dashboardViewModel.addOpenOrder(getContext(),0,limit);

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==121 || requestCode==121){
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.detach(DashboardFragment.this).attach(DashboardFragment.this).commit();
        }
    }


}