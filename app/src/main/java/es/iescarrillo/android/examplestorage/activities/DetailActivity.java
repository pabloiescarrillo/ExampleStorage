package es.iescarrillo.android.examplestorage.activities;

import static com.google.common.io.Files.getFileExtension;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

import es.iescarrillo.android.examplestorage.R;
import es.iescarrillo.android.examplestorage.models.Customer;
import es.iescarrillo.android.examplestorage.services.CustomerService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {

    private Customer customer;
    private EditText etName, etSurname, etPhone;
    private ImageView ivPhoto;
    private Button btnSave, btnCancel;
    private CustomerService customerService;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    // Creamos la referencia a Firebase Storage
    private StorageReference storageReference;
    private Uri imageUri;

    private static final String SUPABASE_URL = "https://gkypzpirjypdyalasznl.supabase.co/storage/v1/object/buckets/images";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdreXB6cGlyanlwZHlhbGFzem5sIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzg1ODY4MTcsImV4cCI6MjA1NDE2MjgxN30.q3EMJD6AYymxbOOX6waIaFK_jOwxEHIC0aUyKJQfeQw";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();
        loadCustomer();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCustomer();
                Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Picasso.get().load(imageUri).into(ivPhoto);
                    }
                });

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }

    private void loadComponents(){
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        ivPhoto = findViewById(R.id.ivPhoto);

        customerService = new CustomerService(getApplicationContext());

        // Inicializamos la referencia a nuestra aplicación Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference().child("customers/");
    }

    private void loadCustomer(){
        customer = (Customer) getIntent().getSerializableExtra("customer");
        etName.setText(customer.getName());
        etSurname.setText(customer.getSurname());
        if(customer.getPhone() != null)
            etPhone.setText(customer.getPhone());
        if(!customer.getPhoto().isEmpty()) // Cargamos la imagen con la librería de Picasso si tiene imagen asociada
            Picasso.get().load(Uri.parse(customer.getPhoto())).into(ivPhoto);
    }

    // Médoto para cargar al imagen en Firebase Storage
    private void uploadImage(String id){
        /* Llamamos al método getImagenUri creado por nosotros para obtener la URI de una imagen
        almacenada en un ImagenView, le pasamos también el id del superhérore ya que el nombre
        de la imagen almacenada se identificará con el id del superhérore
        * */
        Uri file = getImageUri(this, ivPhoto, id);

        // Obtenemos la nueva referencia
        StorageReference storageRefSuperhero = storageReference.child(id);

        // Llamamos al método putFile, el cuál recibe un objeto URI, el que hemos obtenido anteriormente
        storageRefSuperhero.putFile(file).addOnFailureListener(new OnFailureListener() {
            // Método que se ejecutará si se produce un fallo
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            // Método que se ejecutará si todo sale bien
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                if (uriTask.isSuccessful()) {
                    uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString(); // Aquí vamos a obtener la url de la imagen para actualizar la propiedad del customer
                            customer.setPhoto(url);
                            customerService.update(customer);
                        }
                    });
                }
            }
        });
    }

    private void saveCustomer(){
        uploadImage(customer.getId());
        customer.setName(etName.getText().toString());
        customer.setSurname(etSurname.getText().toString());
        customer.setPhone(etPhone.getText().toString());

        customerService.update(customer);
        Toast.makeText(DetailActivity.this, R.string.save_successfully, Toast.LENGTH_SHORT).show();
    }

    // Método para obtener la URI de la imagen de un ImageView
    private Uri getImageUri(Context context, ImageView imageView, String name) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, name, null);
        return Uri.parse(path);
    }

    // Método para seleccionar una imagen usando PhotoPicker
    private void selectImage(){
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // PhotoPicker en Android 13+
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            // Para versiones anteriores
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        pickImageLauncher.launch(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(ivPhoto);
        }
    }

}