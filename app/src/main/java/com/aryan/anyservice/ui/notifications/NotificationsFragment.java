package com.aryan.anyservice.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import adaptor.NotificationAdaptor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import helper.AnyserviceNotification;

import com.aryan.anyservice.R;
import com.aryan.anyservice.ui.dashboard.DashboardFragment;

import java.util.List;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        final ListView listView = root.findViewById(R.id.notification_listview);
        final SwipeRefreshLayout swipeRefreshLayout= root.findViewById(R.id.refresh_layout_notification);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.detach(NotificationsFragment.this).attach(NotificationsFragment.this).commit();

            }
        });
        notificationsViewModel.getNotification(getContext()).observe(getViewLifecycleOwner(), new Observer<List<AnyserviceNotification>>() {
            @Override
            public void onChanged(List<AnyserviceNotification> notifications) {
                NotificationAdaptor adaptor=new NotificationAdaptor(getContext(),R.layout.notification_item,notifications);
                listView.setAdapter(adaptor);
            }
        });
        return root;
    }
}