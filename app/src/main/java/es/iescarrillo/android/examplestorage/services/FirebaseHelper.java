package es.iescarrillo.android.examplestorage.services;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseHelper {

    public static FirebaseStorage initializeFirebaseStorage(Context context){
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:98573484667:android:f1dd888892d6613745d8de")
                .setApiKey("AIzaSyDjfUJ_-bOrb09xXqYDAXxeK_RHjGYIbuo")
                .setStorageBucket("gs://ejemplobbddfirebase.appspot.com")
                .build();

        FirebaseApp appFirebaseStorage = FirebaseApp.getInstance();
        try{
            appFirebaseStorage = FirebaseApp.initializeApp(context, options);
        }catch (IllegalStateException e){

        }

        return FirebaseStorage.getInstance(appFirebaseStorage);
    }
}
