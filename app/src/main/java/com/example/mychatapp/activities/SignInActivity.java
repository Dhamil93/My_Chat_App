package com.example.mychatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mychatapp.databinding.ActivitySignInBinding;
import com.example.mychatapp.utilities.Database;
import com.example.mychatapp.utilities.Preference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private Preference preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preference = new Preference(getApplicationContext());
        if(preference.getBoolean(Database.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        
    }

    private void setListeners(){
        binding.textOpenNewAccount.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonLogIn.setOnClickListener(view ->{
            if (isValidDetails()){
                signIn();
            }
        } );
    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Database.KEY_USERS)
                .whereEqualTo(Database.KEY_EMAIL, binding.enterEmail.getText().toString())
                .whereEqualTo(Database.KEY_PASSWORD, binding.enterPassword.getText().toString())
                .get()
                .addOnCompleteListener(task ->{
                   if(task.isComplete() && task.getResult() != null && task.getResult().getDocuments()
                           .size() > 0){
                       DocumentSnapshot documentSnapshot =  task.getResult().getDocuments().get(0);
                       preference.putBoolean(Database.KEY_IS_SIGNED_IN, true);
                       preference.putString(Database.USER_ID, documentSnapshot.getId());
                       preference.putString(Database.KEY_NAME, documentSnapshot.getString(Database.KEY_NAME));
                       preference.putString(Database.KEY_IMAGE, documentSnapshot.getString(Database.KEY_IMAGE));
                       Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                   }else {
                       loading(false);
                       showToast("You can't be signed In");
                   }
                });
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonLogIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonLogIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidDetails(){
        if(binding.enterEmail.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.enterEmail.getText().toString()).matches()){
            showToast("Enter a Valid Email");
            return false;
        }else if(binding.enterPassword.getText().toString().trim().isEmpty()){
            showToast("Insert Password");
            return false;
        } else {
            return true;
        }
    }
}