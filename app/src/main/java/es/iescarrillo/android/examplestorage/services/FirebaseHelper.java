package es.iescarrillo.android.examplestorage.services;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseHelper {

    public static FirebaseStorage initializeFirebaseStorage(Context context){
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("APP_ID")
                .setApiKey("APP_KEY")
                .setStorageBucket("URL_STORAGE")
                .build();

        FirebaseApp appFirebaseStorage = FirebaseApp.getInstance();
        try{
            appFirebaseStorage = FirebaseApp.initializeApp(context, options);
        }catch (IllegalStateException e){

        }

        return FirebaseStorage.getInstance(appFirebaseStorage);
    }
}
