package es.iescarrillo.android.examplestorage.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.iescarrillo.android.examplestorage.R;
import es.iescarrillo.android.examplestorage.adapters.CustomerAdapter;
import es.iescarrillo.android.examplestorage.models.Customer;

public class MainActivity extends AppCompatActivity {

    private ListView lvCustomers;
    private List<Customer> customers;
    private CustomerAdapter customerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("customers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customers.clear();

                snapshot.getChildren().forEach(node -> {
                    Log.i("Customer", Objects.requireNonNull(node.getValue(Customer.class)).toString());
                    customers.add(node.getValue(Customer.class));
                });

                customerAdapter = new CustomerAdapter(MainActivity.this, customers);
                lvCustomers.setAdapter(customerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error firebase", error.toString());
                Toast.makeText(MainActivity.this, R.string.error_firebase, Toast.LENGTH_SHORT).show();
            }
        });

        lvCustomers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("customer", customers.get(position));
                startActivity(intent);
                finish();
            }
        });

    }

    private void loadComponents(){
        lvCustomers = findViewById(R.id.lvCustomers);
        customers = new ArrayList<>();
    }
}