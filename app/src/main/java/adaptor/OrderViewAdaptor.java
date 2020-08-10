package adaptor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aryan.anyservice.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import helper.Order;

public class OrderViewAdaptor extends ArrayAdapter<Order> {
    int resourceLayout;
    private Context mContext;
    int uid;
    public OrderViewAdaptor(@NonNull Context context, int resource, @NonNull List<Order> objects,String uid) {
        super(context, resource, objects);
        this.resourceLayout = resource;
        this.mContext = context;
        this.uid=Integer.parseInt(uid);
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
        final Order p = getItem(position);
        if(p!=null && resourceLayout==R.layout.order_item){
            TextView name = v.findViewById(R.id.order_name);
            TextView displayName = v.findViewById(R.id.display_name);
            TextView desc = v.findViewById(R.id.description);
            TextView state = v.findViewById(R.id.state);
            TextView date = v.findViewById(R.id.date);
            ImageView icon = v.findViewById(R.id.order_icon);
            name.setText(p.getName());
            desc.setText(p.getDescription());
            state.setText(p.getState());
            if(p.getDescription().equals("")){
                desc.setVisibility(View.GONE);
            }else{
                desc.setVisibility(View.VISIBLE);

            }
            if(p.getOrderDate()!=null) date.setText(p.getOrderDate());
            if(uid==p.getAgentID()){
                displayName.setText(String.format("Customer - %s", p.getCustName()));
            }else{
                displayName.setText(String.format("Agent - %s", p.getAgentName()));
            }
            try {
                byte[] decodedString = Base64.decode(p.getIcon(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                icon.setImageBitmap(decodedBitmap);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        return v;
    }

}
