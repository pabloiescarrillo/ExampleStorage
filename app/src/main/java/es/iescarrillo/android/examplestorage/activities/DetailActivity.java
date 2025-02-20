package es.iescarrillo.android.examplestorage.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;

import es.iescarrillo.android.examplestorage.R;
import es.iescarrillo.android.examplestorage.models.Customer;
import es.iescarrillo.android.examplestorage.services.CustomerService;
import es.iescarrillo.android.examplestorage.services.FirebaseHelper;

public class DetailActivity extends AppCompatActivity {

    private Customer customer;
    private EditText etName, etSurname, etPhone;
    private ImageView ivPhoto;
    private Button btnSave, btnCancel, btnCamera;
    private CustomerService customerService;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    // Creamos la referencia a Firebase Storage
    private StorageReference storageReference;
    private Uri imageUri;
    private Uri photoUri;
    private File photoFile;

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

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (photoUri != null) {
                            // Cargar imagen en el ImageView con Picasso
                            Picasso.get().load(photoUri).into(ivPhoto);
                        } else {
                            Toast.makeText(this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
            }
        });

    }

    private void loadComponents(){
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnCamera = findViewById(R.id.btnCamera);
        ivPhoto = findViewById(R.id.ivPhoto);

        customerService = new CustomerService(getApplicationContext());

        // Inicializamos la referencia a nuestra aplicación Firebase Storage
        storageReference = FirebaseHelper.initializeFirebaseStorage(getApplicationContext()).getReference().child("customers");
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
        almacenada en un ImagenView, le pasamos también el id del customer ya que el nombre
        de la imagen almacenada se identificará con el id del customer
        * */
        Uri file = getImageUri(this, ivPhoto, id);

        // Obtenemos la nueva referencia
        StorageReference storageRefCustomer = storageReference.child(id);

        // Llamamos al método putFile, el cuál recibe un objeto URI, el que hemos obtenido anteriormente
        storageRefCustomer.putFile(file).addOnFailureListener(new OnFailureListener() {
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

    // Abrir la cámara y guardar la foto en un archivo
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, "es.iescarrillo.android.examplestorage.fileprovider", photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    cameraLauncher.launch(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Crear un archivo temporal para guardar la imagen
    private File createImageFile() throws IOException {
        String timeStamp = LocalDateTime.now().toString();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // Verificar permiso de la cámara
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Solicitar permiso de la cámara
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    // Manejar la respuesta del usuario
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }

}