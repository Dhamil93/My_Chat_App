package com.example.mychatapp.activities;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Base64;
import android.widget.Toast;

import com.example.mychatapp.databinding.ActivityMainBinding;
import com.example.mychatapp.utilities.Database;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Preference preference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preference = new Preference(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
    }

    private void loadUserDetails(){
        binding.textName.setText(preference.getString(Database.KEY_NAME));
        byte[] bytes = Base64.decode(preference.getString(Database.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void setListeners(){
        binding.logout.setOnClickListener(view -> logOut());
    }

    public void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Database.KEY_USERS).document(
                        preference.getString(Database.USER_ID)
                );
        documentReference.update(Database.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> showToast("Token Updated"))
                .addOnFailureListener(e -> showToast("Token can't be updated"));
    }

    private void logOut(){
        showToast("Logging Out......😒😒😒");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Database.KEY_USERS).document(
                        preference.getString(Database.USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Database.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preference.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("You can't log Out..... 😁😁😁"));
    }

}