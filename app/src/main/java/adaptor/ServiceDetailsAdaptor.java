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

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import helper.ServiceDetails;

public class ServiceDetailsAdaptor extends ArrayAdapter<ServiceDetails> {
    int resourceLayout;
    private Context mContext;
    public ServiceDetailsAdaptor(@NonNull Context context, int resource, @NonNull List<ServiceDetails> objects) {
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
        final ServiceDetails p = getItem(position);
        if(p!=null && resourceLayout==R.layout.search_service_list_item){
            TextView company = v.findViewById(R.id.service_company);
            TextView name = v.findViewById(R.id.service_name);
            TextView category = v.findViewById(R.id.service_category);
            TextView price = v.findViewById(R.id.service_price);
            TextView deliveryprice = v.findViewById(R.id.delivery_price);
            RatingBar rating = v.findViewById(R.id.service_rating);
            ImageView icon = v.findViewById(R.id.service_icon);
            if(p.getCompany()!=null) company.setText(p.getCompany());
            if(p.getName()!=null) name.setText(p.getName());
            if(p.getCategory()!=null) category.setText(p.getCategory());
            price.setText(""+p.getPrice());
            deliveryprice.setText(""+p.getDeliveryCost());
            rating.setRating(p.getRating());
            try {
                byte[] decodedString = Base64.decode(p.getIcon(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                icon.setImageBitmap(decodedBitmap);
            }catch(Exception e){

            }
        }else if(p!=null && resourceLayout==R.layout.select_service_list_item){
            TextView name = v.findViewById(R.id.select_service_name);
            TextView category = v.findViewById(R.id.select_service_category);
            final CheckBox select = v.findViewById(R.id.select_service_checkbox);
            final TextView price = v.findViewById(R.id.select_service_price);
            final TextView quantity = v.findViewById(R.id.quantity_textview);
            final LinearLayout quantityLayout = v.findViewById(R.id.quantity_layout);
            final Button plus = v.findViewById(R.id.plus_button);
            final Button minus = v.findViewById(R.id.minus_button);
            if(p.getName()!=null) name.setText(p.getName());
            if(p.getCategory()!=null) category.setText(p.getCategory());
            price.setText(""+p.getTotal_price());
            select.setChecked(p.isSelected());
            if(p.isMeasurable()) quantityLayout.setVisibility(View.VISIBLE);
            if(!p.isMeasurable()) quantityLayout.setVisibility(View.GONE);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    p.setSelected(!select.isChecked());
                    select.setChecked(p.isSelected());
                    if(!select.isChecked() && p.isMeasurable()){
                        p.setUnit(1);
                        quantity.setText("1");
                    }
                    notifyDataSetChanged();

                }
            });
            select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        p.setSelected(select.isChecked());
                        if(!select.isChecked() && p.isMeasurable()){
                            p.setUnit(1);
                            quantity.setText("1");
                        }
                    notifyDataSetChanged();
                }
            });
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int num = Integer.parseInt(quantity.getText().toString());
                    if(num==1){
                        if(!p.isSelected()){
                            p.setSelected(true);
                            select.setChecked(true);
                        }
                    }
                    if(num<10) {
                        num++;
                        quantity.setText("" + num);
                        p.setUnit(num);
                        price.setText("" + p.getTotal_price());
                    }
                    notifyDataSetChanged();
                }
            });
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int num = Integer.parseInt(quantity.getText().toString());
                    if(num==1){
                        if(p.isSelected()){
                            p.setSelected(false);
                            select.setChecked(false);
                        }
                    }
                    if(num>1) {
                        num--;
                        p.setUnit(num);
                        quantity.setText("" + num);
                        price.setText("" + p.getTotal_price());
                    }
                    notifyDataSetChanged();
                }
            });


            notifyDataSetChanged();
        }else if(p!=null && resourceLayout==R.layout.service_list_item){
            TextView name = v.findViewById(R.id.select_service_name);
            final TextView price = v.findViewById(R.id.select_service_price);
            if(p.getName()!=null) name.setText(p.getName());
            price.setText(""+p.getTotal_price());

        }
        return v;
    }

}
