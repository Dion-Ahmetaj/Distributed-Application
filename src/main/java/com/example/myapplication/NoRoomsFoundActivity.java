package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

public class NoRoomsFoundActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_room_found);
    }

    public void onRetryClicked(View view) {
        // Redirect back to the BookActivity
        Intent intent = new Intent(NoRoomsFoundActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
