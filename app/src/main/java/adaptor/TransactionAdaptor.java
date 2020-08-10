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
import helper.TransactionAnyservice;

public class TransactionAdaptor extends ArrayAdapter<TransactionAnyservice> {
    int resourceLayout;
    private Context mContext;
    public TransactionAdaptor(@NonNull Context context, int resource, @NonNull List<TransactionAnyservice> objects) {
        super(context, resource,objects);
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
        final TransactionAnyservice p = getItem(position);
        if(p!=null && resourceLayout== R.layout.transaction_item){
            TextView name = v.findViewById(R.id.name);
            TextView credit = v.findViewById(R.id.credit);
            TextView debit = v.findViewById(R.id.debit);
            TextView total = v.findViewById(R.id.total);
            name.setText(p.getName());
            credit.setText(p.getCredit());
            debit.setText(p.getDebit());
            total.setText(p.getTotal());
        }
        return v;
    }
}
