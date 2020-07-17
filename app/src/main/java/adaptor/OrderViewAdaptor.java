package adaptor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.aryan.anyservice.R;

import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import helper.Order;

public class OrderViewAdaptor extends ArrayAdapter<Order> {
    int resourceLayout;
    private Context mContext;
    public OrderViewAdaptor(@NonNull Context context, int resource, @NonNull List<Order> objects) {
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
        final Order p = getItem(position);
        if(p!=null && resourceLayout==R.layout.order_item){
            TextView name = v.findViewById(R.id.order_name);
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
            if(p.getOrderDate()!=null) date.setText(new SimpleDateFormat("EEE, dd MMM yyyy").format(p.getOrderDate()));
            try {
                byte[] decodedString = Base64.decode(p.getIcon(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                icon.setImageBitmap(decodedBitmap);
            }catch(Exception e){

            }
        }
        return v;
    }

}
