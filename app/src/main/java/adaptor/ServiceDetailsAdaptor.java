package adaptor;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.aryan.anyservice.LargeImageActivity;
import com.aryan.anyservice.R;
import com.aryan.anyservice.ServiceActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import helper.IconTextView;
import helper.ServiceDetails;

public class ServiceDetailsAdaptor extends ArrayAdapter<ServiceDetails> {
    int resourceLayout;
    private Context mContext;
    boolean addtoCart;
    public ServiceDetailsAdaptor(@NonNull Context context, int resource, @NonNull List<ServiceDetails> objects,boolean addtoCart) {
        super(context, resource, objects);
        this.resourceLayout = resource;
        this.mContext = context;
        this.addtoCart = addtoCart;
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
        if(p!=null && resourceLayout==R.layout.product_item){
            TextView company = v.findViewById(R.id.service_company);
            TextView name = v.findViewById(R.id.service_name);
            TextView category = v.findViewById(R.id.service_category);
            final TextView price = v.findViewById(R.id.service_price);
            TextView deliveryprice = v.findViewById(R.id.delivery_price);
            RatingBar rating = v.findViewById(R.id.service_rating);
            ImageView icon = v.findViewById(R.id.service_icon);
            ImageView verified = v.findViewById(R.id.verified);

            if(p.isVerified()){
                verified.setVisibility(View.VISIBLE);
            }else{
                verified.setVisibility(View.GONE);
            }
            if(p.getCompany()!=null) company.setText(p.getCompany());
            if(p.getName()!=null) name.setText(p.getName());

            final TextView description = v.findViewById(R.id.description);
            if(description!=null) {
                if (p.getDescription() != null) {
                    description.setText(p.getDescription());
                    if (p.getDescription().equals("")) {
                        description.setVisibility(View.GONE);
                    } else {
                        description.setVisibility(View.VISIBLE);
                    }
                } else {
                    description.setVisibility(View.GONE);
                }

                description.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(description.getMaxLines()==3) {
                            description.setMaxLines(80);
                        }else{
                            description.setMaxLines(3);
                        }
                    }
                });
            }
            if(p.getCategory()!=null) category.setText(p.getCategory());
            price.setText(""+p.getPrice());
            deliveryprice.setText(""+p.getDeliveryCost());
            rating.setRating(p.getRating());
            try {
                byte[] decodedString = Base64.decode(p.getIcon(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                icon.setImageBitmap(decodedBitmap);
            }catch(Exception e) {
                e.printStackTrace();
            }
            final TextView quantity = v.findViewById(R.id.quantity_textview);
            final LinearLayout quantityLayout = v.findViewById(R.id.quantity_layout);
            final Button plus = v.findViewById(R.id.plus_button);
            final Button minus = v.findViewById(R.id.minus_button);
            if(p.getName()!=null) name.setText(p.getName());
            if(p.getCategory()!=null) category.setText(p.getCategory());
            price.setText(""+p.getTotal_price());
            if(p.getUnit()!=0) {
                quantity.setText(""+p.getUnit());
                if(p.getUnit()==1){
                    price.setText(""+p.getTotal_price());
                }else {
                    price.setText("" + p.getTotal_price() + " (@ 1 Unit = " + p.getPrice() + ")");
                }
            }else{
                quantity.setText("1");

            }
            if(p.isMeasurable()) quantityLayout.setVisibility(View.VISIBLE);
            if(!p.isMeasurable()) quantityLayout.setVisibility(View.INVISIBLE);
            final Button addtoCartButton = v.findViewById(R.id.add_to_cart);
            IconTextView checkout = v.findViewById(R.id.checkout_button);
            if(!addtoCart) {
                company.setVisibility(View.VISIBLE);
                rating.setVisibility(View.VISIBLE);
                addtoCartButton.setVisibility(View.GONE);
                checkout.setVisibility(View.VISIBLE);
                checkout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(mContext, ServiceActivity.class);
//                        ServiceDetails service = (ServiceDetails) serviceListView.getAdapter().getItem(position);

                        intent.putExtra("service",p);
                        intent.putExtra("agent_id",p.getAgent_id());
                        intent.putExtra("imageview",true);
                        mContext.startActivity(intent);
//                        ArrayList<ServiceDetails> serviceDetailsArrayList = new ArrayList<>();
//                        serviceDetailsArrayList.add(p);
//                        Intent intent = new Intent(mContext, YourOrderActivity.class);
//                        intent.putExtra("visitPrice", p.getDeliveryCost());
//                        intent.putExtra("pending", p.getBalance());
//                        intent.putExtra("price", p.getTotal_price());
//                        intent.putExtra("company", p.getCompany());
//                        intent.putExtra("totalPrice", p.getTotal_price() + p.getBalance() + p.getDeliveryCost());
//                        intent.putExtra("services", serviceDetailsArrayList);
//                        mContext.startActivity(intent);
                    }
                });
            }else{
                verified.setVisibility(View.GONE);
                company.setVisibility(View.GONE);
//                rating.setVisibility(View.GONE);
                addtoCartButton.setVisibility(View.VISIBLE);
                checkout.setVisibility(View.GONE);
                if(!p.isSelected()) {
                    addtoCartButton.setText(String.format("%s Add", mContext.getResources().getString(R.string.cart)));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        addtoCartButton.setBackgroundTintList(ColorStateList.valueOf(mContext.getResources().getColor(R.color.colorAccent)));
                    }else{
                        addtoCartButton.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
                    }
                }else{
                    addtoCartButton.setText(String.format("%s Remove", mContext.getResources().getString(R.string.cart)));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        addtoCartButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    }else{
                        addtoCartButton.setBackgroundColor(Color.RED);
                    }
                }
                addtoCartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        p.setSelected(!p.isSelected());
                        if(!p.isSelected()&& p.isMeasurable()){
                            p.setUnit(1);
                            quantity.setText("1");
                        }
                        if(!p.isSelected()) {
                            addtoCartButton.setText(String.format("%s Add", mContext.getResources().getString(R.string.cart)));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                addtoCartButton.setBackgroundTintList(ColorStateList.valueOf(mContext.getResources().getColor(R.color.colorAccent)));
                            }else{
                                addtoCartButton.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
                            }
                        }else{
                            addtoCartButton.setText(String.format("%s Remove", mContext.getResources().getString(R.string.cart)));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                addtoCartButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                            }else{
                                addtoCartButton.setBackgroundColor(Color.RED);
                            }
                        }
                        notifyDataSetChanged();

                    }
                });
            }

            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(v.getContext(), LargeImageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("image",p.getId());
                    v.getContext().startActivity(intent);

                }
            });

            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int num = Integer.parseInt(quantity.getText().toString());
                    if(num<10) {
                        num++;
                        quantity.setText("" + num);
                        p.setUnit(num);;
                        if(p.getUnit()==1){
                            price.setText(""+p.getTotal_price());
                        }else {
                            price.setText("" + p.getTotal_price() + " (@ 1 Unit = " + p.getPrice() + ")");
                        }
                        notifyDataSetChanged();
                    }
                }
            });
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int num = Integer.parseInt(quantity.getText().toString());
                    if(num>1) {
                        num--;
                        p.setUnit(num);
                        quantity.setText("" + num);;
                        if(p.getUnit()==1){
                            price.setText(""+p.getTotal_price());
                        }else {
                            price.setText("" + p.getTotal_price() + " (@ 1 Unit = " + p.getPrice() + ")");
                        }
                        notifyDataSetChanged();
                    }
                }
            });
        }else if(p!=null && resourceLayout==R.layout.search_service_list_item){
            TextView company = v.findViewById(R.id.service_company);
            TextView name = v.findViewById(R.id.service_name);
            TextView category = v.findViewById(R.id.service_category);
            TextView price = v.findViewById(R.id.service_price);
            TextView deliveryprice = v.findViewById(R.id.delivery_price);
            RatingBar rating = v.findViewById(R.id.service_rating);
            ImageView icon = v.findViewById(R.id.service_icon);
            ImageView verified = v.findViewById(R.id.verified);

            if(p.isVerified() && ! addtoCart){
                verified.setVisibility(View.VISIBLE);
            }else{
                verified.setVisibility(View.GONE);
            }
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
        }else if(p!=null && resourceLayout==R.layout.agent_service_item){
            TextView name = v.findViewById(R.id.service_name);
            TextView category = v.findViewById(R.id.service_category);
            TextView price = v.findViewById(R.id.service_price);
            CheckBox delivery = v.findViewById(R.id.delivery);
            CheckBox measurable = v.findViewById(R.id.measurable);
            ImageView icon = v.findViewById(R.id.service_icon);
            RatingBar rating = v.findViewById(R.id.service_rating);
            rating.setRating(p.getRating());
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(v.getContext(), LargeImageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("image",p.getId());
                    v.getContext().startActivity(intent);

                }
            });
            final TextView description = v.findViewById(R.id.description);
            if(p.getDescription()!=null){
                description.setText(p.getDescription());
                if(p.getDescription().equals("")){
                    description.setVisibility(View.GONE);
                }else{
                    description.setVisibility(View.VISIBLE);
                }
            }else{
                description.setVisibility(View.GONE);
            }
            description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(description.getMaxLines()==3) {
                        description.setMaxLines(80);
                    }else{
                        description.setMaxLines(3);
                    }
                }
            });
            if(p.getName()!=null) name.setText(p.getName());
            if(p.getCategory()!=null) category.setText(p.getCategory());
            price.setText(""+p.getPrice());
            delivery.setChecked(p.isDeliveryCostable());
            measurable.setChecked(p.isMeasurable());
            try {
                byte[] decodedString = Base64.decode(p.getIcon(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                icon.setImageBitmap(decodedBitmap);
            }catch(Exception e){

            }
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(v.getContext(), LargeImageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("image",p.getId());
                    v.getContext().startActivity(intent);

                }
            });
        }else if(p!=null && resourceLayout==R.layout.select_service_list_item){
            TextView name = v.findViewById(R.id.select_service_name);
            TextView category = v.findViewById(R.id.select_service_category);
            final CheckBox select = v.findViewById(R.id.select_service_checkbox);
            final TextView price = v.findViewById(R.id.select_service_price);
            final TextView quantity = v.findViewById(R.id.quantity_textview);
            final LinearLayout quantityLayout = v.findViewById(R.id.quantity_layout);
            final Button plus = v.findViewById(R.id.plus_button);
            final Button minus = v.findViewById(R.id.minus_button);
            ImageView icon = v.findViewById(R.id.service_icon);
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

            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(v.getContext(), LargeImageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("image",p.getId());
                    v.getContext().startActivity(intent);

                }
            });

            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent =new Intent(v.getContext(), LargeImageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("image",p.getId());
                    v.getContext().startActivity(intent);

                    return false;
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
            try {
                byte[] decodedString = Base64.decode(p.getIcon(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                icon.setImageBitmap(decodedBitmap);
            }catch(Exception e) {
                e.printStackTrace();
            }


            notifyDataSetChanged();
        }else if(p!=null && resourceLayout==R.layout.service_list_item){
            TextView name = v.findViewById(R.id.select_service_name);
            final TextView price = v.findViewById(R.id.select_service_price);
            if(p.getUnit()>1){
                if (p.getName() != null) name.setText(p.getName()+"(@ "+p.getUnit()+" Units)");
            }else {
                if (p.getName() != null) name.setText(p.getName());
            }
            price.setText(""+p.getTotal_price());

        }
        return v;

    }


}
