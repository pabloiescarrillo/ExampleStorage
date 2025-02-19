package es.iescarrillo.android.examplestorage.services;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.iescarrillo.android.examplestorage.models.Customer;

public class CustomerService {
    private DatabaseReference reference;

    public CustomerService(Context context){
        reference = FirebaseDatabase.getInstance().getReference("customers");
    }

    public String insert(Customer customer){
        DatabaseReference newReference = reference.push();
        String id = newReference.getKey();
        customer.setId(id);

        newReference.setValue(customer);

        return id;
    }

    public void update(Customer customer){
        reference.child(customer.getId()).setValue(customer);
    }

    public void delete(Customer customer){
        reference.child(customer.getId()).removeValue();
    }
}
