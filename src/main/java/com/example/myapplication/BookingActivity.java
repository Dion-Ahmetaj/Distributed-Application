package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.example.Filter;
import org.example.Room;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class BookingActivity extends AppCompatActivity {

    private TextView textViewRoomName;
    private EditText editTextNumberOfRooms;
    private Button buttonBook,ReturnButton;
    private Room selectedRoom;
    private static final String TAG = "SocketClient";
    private static final String SERVER_IP = "192.168.56.1"; // Replace with your server's IP address
    private static final int SERVER_PORT = 1234;

    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

       initViews();

        // Get the selected room from the intent
        Intent intent = getIntent();
        selectedRoom = (Room) intent.getSerializableExtra("selectedRoom");

        // Display the selected room name
        textViewRoomName.setText(selectedRoom.getRoomName());
        buttonBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text=editTextNumberOfRooms.getText().toString();
                Room room= new Room();
                room=selectedRoom;
                String[] parts;
                parts = text.split("-");
                Log.d(TAG, "Time:"+editTextNumberOfRooms.getText().toString());
                Log.d(TAG,selectedRoom.getRoomName() + ":" + parts[0] + ":" + parts[1]+ ":" + "1");
                // Perform booking using the selected room and number of rooms
                String bookedRoom= selectedRoom.getRoomName() + ":" + parts[0] + ":" + parts[1]+ ":" + "1";
                ClientThread clientThread=new ClientThread(bookedRoom);
                clientThread.start();
                try {
                    clientThread.join();
                    int Cond=clientThread.getNumber();
                    if(Cond==2){
                        Toast.makeText(BookingActivity.this,"The room is booked", Toast.LENGTH_SHORT).show();
                        buttonBook.setVisibility(View.GONE);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        ReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void initViews(){
        textViewRoomName = findViewById(R.id.textViewRoomName);
        editTextNumberOfRooms = findViewById(R.id.editTextNumberOfRooms);
        buttonBook = findViewById(R.id.buttonBook);
        ReturnButton=findViewById(R.id.ReturnButton);

    }
    class ClientThread extends Thread {

        private String room;

        private int number;

        private ClientThread(String room){
            this.room=room;
            this.number=0;
        }
        private void setNumber(int number){
            this.number=number;
        }
        public int getNumber(){
            return number;
        }
        @Override
        public void run() {
            try{
                Log.d(TAG, "Send to Server");
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                // Send message to server
                out.writeInt(4);
                out.flush();

                out.writeObject(room);
                out.flush();

                ObjectInputStream in=new ObjectInputStream(socket.getInputStream());
                int number=in.readInt();
                setNumber(number);


            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            }

        }
    }
}
