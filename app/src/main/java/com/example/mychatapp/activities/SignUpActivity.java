package com.example.mychatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.mychatapp.R;
import com.example.mychatapp.databinding.ActivitySignInBinding;
import com.example.mychatapp.databinding.ActivitySignUpBinding;
import com.example.mychatapp.utilities.Database;
import com.example.mychatapp.utilities.Preference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private Preference preference;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preference = new Preference(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(view -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(view -> {
            if (isValidSignUpDetails()){
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            selectImage.launch(intent);
        });
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp(){
        loading(true);
        FirebaseFirestore data = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Database.KEY_NAME, binding.enterName.getText().toString());
        user.put(Database.KEY_EMAIL, binding.enterEmail.getText().toString());
        user.put(Database.KEY_PASSWORD, binding.enterPassword.getText().toString());
        user.put(Database.KEY_IMAGE, encodedImage);
        data.collection(Database.KEY_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preference.putBoolean(Database.KEY_IS_SIGNED_IN, true);
                    preference.putString(Database.USER_ID, documentReference.getId());
                    preference.putString(Database.KEY_NAME, binding.enterName.getText().toString());
                    preference.putString(Database.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());

                });



    }

    private String encondeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> selectImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.dpic.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encondeImage(bitmap);
                        }catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private boolean isValidSignUpDetails(){
    if (encodedImage == null){
        showToast("Select Imaage");

        return false;
    }else if (binding.enterName.getText().toString().trim().isEmpty()){
        showToast("Enter Name");
        return false;
    }else if (binding.enterEmail.getText().toString().trim().isEmpty()){
        showToast("Enter Email");
        return false;
    } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.enterEmail.getText().toString()).matches()){
        showToast("Enter Valid Email");
        return false;
    } else if (binding.enterPassword.getText().toString().trim().isEmpty()){
        showToast("Enter Password");
        return false;
    } else if (binding.enterConfirmPassword.getText().toString().trim().isEmpty()){
        showToast("Confirm Password");
        return false;
    } else if (!binding.enterPassword.getText().toString().equals(binding.enterConfirmPassword.getText().toString())){
    showToast("Password must match");
    return false;
    } else {
        return true;
    }

    }

    private void loading(Boolean isLoading){
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }

    }
}