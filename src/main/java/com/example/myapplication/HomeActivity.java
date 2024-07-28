package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import org.example.Filter;
import org.example.Room;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "SocketClient";
    private static final String SERVER_IP = "192.168.56.1"; // Replace with your server's IP address
    private static final int SERVER_PORT = 1234;
    private DrawerLayout drawerLayout;
    protected EditText EditArea,EditTime,EditNumberOfTenats,EditPrice,EditStars;
    protected Button SumbitButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);

        // Set up the hamburger icon to open and close the drawer
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle navigation view item clicks here.
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Handle the home action
            } else if (id == R.id.nav_contact) {
                // Handle the contact action
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        SumbitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Filter filter=new Filter();
                filter.setArea(EditArea.getText().toString());
                filter.setTime(EditTime.getText().toString());
                filter.setNumb(Integer.parseInt(EditNumberOfTenats.getText().toString()));
                filter.setPrice(Double.parseDouble(EditPrice.getText().toString()));
                filter.setStars(Double.parseDouble(EditStars.getText().toString()));
                ClientThread clientThread=new ClientThread(filter);
                clientThread.start();
                try {
                    clientThread.join();
                    ArrayList<Room> rooms=new ArrayList<>();
                    rooms=clientThread.getRooms();
                    if(!rooms.isEmpty()){
                        Log.d(TAG, "Room Exist");
                        Intent intent=new Intent(HomeActivity.this, AvailableRoomsActivity.class);
                        intent.putExtra("rooms", rooms );
                        intent.putExtra("time", EditTime.getText().toString());
                        startActivity(intent);
                    }else{
                        Log.d(TAG, "Room Empty");
                        Intent intent=new Intent(HomeActivity.this, NoRoomsFoundActivity.class);
                        startActivity(intent);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    class ClientThread extends Thread {

        private int num;
        private Filter filter;
        private ArrayList<Room> Rooms;
        private ClientThread(Filter filter1){
            this.filter=filter1;
        }
        private void setRooms(ArrayList<Room> rooms){
            this.Rooms=rooms;
        }
        public ArrayList<Room> getRooms(){
            return this.Rooms;
        }
        @Override
        public void run() {
            try{
                Log.d(TAG, "Send to Server");
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                // Send message to server
                out.writeInt(3);
                out.flush();

                out.writeObject(filter);
                out.flush();

                ObjectInputStream in=new ObjectInputStream(socket.getInputStream());

                ArrayList<Room> rooms= new ArrayList<>();
                rooms = (ArrayList<Room>) in.readObject();

                setRooms(rooms);

            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void initViews(){
        EditArea= findViewById(R.id.editTextArea);
        EditTime= findViewById(R.id.editTextTime);
        EditNumberOfTenats= findViewById(R.id.editTextNumberOfTenants);
        EditPrice= findViewById(R.id.editTextPrice);
        EditStars= findViewById(R.id.editTextStars);
        SumbitButton= findViewById(R.id.buttonSubmit);
    }
}
