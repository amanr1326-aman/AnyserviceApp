package com.aryan.anyservice.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import adaptor.OrderViewAdaptor;
import adaptor.ServiceDetailsAdaptor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import helper.Order;
import helper.Utility;

import com.aryan.anyservice.OrderStatusActivity;
import com.aryan.anyservice.R;

import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final ListView openOrders = root.findViewById(R.id.open_services);
        final ListView doneOrders = root.findViewById(R.id.done_services);
        dashboardViewModel.getDoneServices(getContext()).observe(getViewLifecycleOwner(), new Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> orders) {
                OrderViewAdaptor serviceDetailsAdaptor=new OrderViewAdaptor(getContext(),R.layout.order_item,orders);
                doneOrders.setAdapter(serviceDetailsAdaptor);
                Utility.setListViewHeightBasedOnChildren(doneOrders);
            }
        });
        dashboardViewModel.getOpenServices().observe(getViewLifecycleOwner(), new Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> orders) {
                OrderViewAdaptor serviceDetailsAdaptor=new OrderViewAdaptor(getContext(),R.layout.order_item,orders);
                openOrders.setAdapter(serviceDetailsAdaptor);
                Utility.setListViewHeightBasedOnChildren(openOrders);
            }
        });
        openOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order= (Order) openOrders.getAdapter().getItem(position);
                Intent intent =new Intent(getContext(),OrderStatusActivity.class);
                intent.putExtra("order",order);
                startActivity(intent);
            }
        });
        doneOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order= (Order) doneOrders.getAdapter().getItem(position);
                Intent intent =new Intent(getContext(),OrderStatusActivity.class);
                intent.putExtra("order",order);
                startActivity(intent);
            }
        });
        return root;
    }
}