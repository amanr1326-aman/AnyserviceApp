package adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aryan.anyservice.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import helper.AnyserviceNotification;

public class NotificationAdaptor extends ArrayAdapter<AnyserviceNotification> {
    int resourceLayout;
    private Context mContext;
    public NotificationAdaptor(@NonNull Context context, int resource, @NonNull List<AnyserviceNotification> objects) {
        super(context, resource, objects);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if(v==null){
            LayoutInflater layoutInflater;
            layoutInflater = LayoutInflater.from(mContext);
            v=layoutInflater.inflate(resourceLayout,null);
        }
        final AnyserviceNotification p = getItem(position);
        if(p!=null && resourceLayout==R.layout.notification_item){
            TextView name = v.findViewById(R.id.title);
            TextView msg = v.findViewById(R.id.message);
            TextView date = v.findViewById(R.id.date);
            name.setText(p.getTitle());
            msg.setText(p.getMessage());
            date.setText(p.getDate());
        }
        return v;
    }

}
