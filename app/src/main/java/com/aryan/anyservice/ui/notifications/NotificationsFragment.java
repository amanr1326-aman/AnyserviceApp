package com.aryan.anyservice.ui.notifications;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aryan.anyservice.OrderStatusActivity;
import com.aryan.anyservice.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import adaptor.NotificationAdaptor;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import helper.AnyserviceNotification;

public class NotificationsFragment extends Fragment {
    NotificationAdaptor adaptor;

    private NotificationsViewModel notificationsViewModel;
    ArrayList<AnyserviceNotification> anyserviceNotifications = new ArrayList<>();
    ProgressBar progressBar;
    final int limit=20;
    boolean loading=true,loadNew=false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        final ListView listView = root.findViewById(R.id.notification_listview);
        progressBar = root.findViewById(R.id.progress);
        adaptor=new NotificationAdaptor(Objects.requireNonNull(getContext()),R.layout.notification_item,anyserviceNotifications);
        listView.setAdapter(adaptor);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount==totalItemCount-5){
                    if(!loading){
                        loading=true;
                        loadNew=true;
                        progressBar.setVisibility(View.VISIBLE);
                        notificationsViewModel.add_notifications(getContext(),totalItemCount,limit);
                    }
                }

            }
        });
        final SwipeRefreshLayout swipeRefreshLayout= root.findViewById(R.id.refresh_layout_notification);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.VISIBLE);
                loading=true;
                FragmentTransaction fragmentTransaction = null;
                if (getFragmentManager() != null) {
                    fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.detach(NotificationsFragment.this).attach(NotificationsFragment.this).commit();
                }

            }
        });
        notificationsViewModel.getNotification().observe(getViewLifecycleOwner(), new Observer<List<AnyserviceNotification>>() {
            @Override
            public void onChanged(List<AnyserviceNotification> notifications) {
                if(!loadNew){
                    anyserviceNotifications.clear();
                }else{
                    loadNew=false;
                }
                anyserviceNotifications.addAll(notifications);
                adaptor.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if(notifications.size()>=limit-5){
                    loading=false;
                }
                if(notifications.size()>0){
                    TextView empty = getActivity().findViewById(R.id.empty);
                    empty.setVisibility(View.GONE);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AnyserviceNotification notification= (AnyserviceNotification) listView.getAdapter().getItem(position);
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(notification.getTitle(),notification.getTitle()+"\n"+notification.getMessage());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Text copied to clipboard.", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AnyserviceNotification notification= (AnyserviceNotification) listView.getAdapter().getItem(position);
                if(notification.getOrder()!=null){
                    Intent notificationIntent = new Intent(getContext(), OrderStatusActivity.class);
                    notificationIntent.putExtra("order",notification.getOrder());
                    startActivity(notificationIntent);
                }else {
                    Toast.makeText(getContext(),"No Extra Details Found",Toast.LENGTH_LONG).show();
                }
            }
        });
        notificationsViewModel.add_notifications(getContext(),0,limit);
        return root;
    }
}