package com.example.mychatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.mychatapp.adapters.UsersAdapter;
import com.example.mychatapp.databinding.ActivityUsersBinding;
import com.example.mychatapp.models.User;
import com.example.mychatapp.utilities.Database;
import com.example.mychatapp.utilities.Preference;


import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    private ActivityUsersBinding binding;
    private Preference preference ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preference = new Preference(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(view -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Database.KEY_USERS)
                .get()
                .addOnCompleteListener(task -> {
                   loading(false);
                   String currentUserId = preference.getString(Database.USER_ID);
                   if (task.isSuccessful() && task.getResult() != null){
                       List<User> users = new ArrayList<>();
                       for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                           if (currentUserId.equals(queryDocumentSnapshot.getId())){
                               continue;
                           }
                           User user = new User();
                           user.name = queryDocumentSnapshot.getString(Database.KEY_NAME);
                           user.email = queryDocumentSnapshot.getString(Database.KEY_EMAIL);
                           user.image = queryDocumentSnapshot.getString(Database.KEY_IMAGE);
                           user.token = queryDocumentSnapshot.getString(Database.KEY_FCM_TOKEN);
                           users.add(user);
                       }
                       if (users.size() > 0){
                           UsersAdapter usersAdapter = new UsersAdapter(users);
                           binding.usersRecyclerView.setAdapter(usersAdapter);
                           binding.usersRecyclerView.setVisibility(View.VISIBLE);
                       } else {
                           showErrorMessage();
                       }
                   }else {
                       showErrorMessage();
                   }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "Unavailable User...😟😟😟"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }

    }
}