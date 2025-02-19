package es.iescarrillo.android.examplestorage.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import es.iescarrillo.android.examplestorage.R;
import es.iescarrillo.android.examplestorage.models.Customer;

public class CustomerAdapter extends ArrayAdapter<Customer> {

    public CustomerAdapter(Context context, List<Customer> persons){
        super(context, 0, persons);
    }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
        Customer c = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_customer, parent, false);

        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvPhone = convertView.findViewById(R.id.tvPhone);

        tvName.setText(c.getName() + " " + c.getSurname());

        if (c.getPhone() == null)
            tvPhone.setVisibility(android.view.View.GONE);
        else
            tvPhone.setText(String.valueOf(c.getPhone()));

        return convertView;
    }
}
