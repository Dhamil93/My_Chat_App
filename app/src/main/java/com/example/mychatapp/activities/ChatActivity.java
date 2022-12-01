package com.example.mychatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.mychatapp.databinding.ActivityChatBinding;
import com.example.mychatapp.models.User;
import com.example.mychatapp.utilities.Database;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiverUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
    }
    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Database.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }
    private void setListeners() {
        binding.imageBack.setOnClickListener(view -> onBackPressed());
    }
}