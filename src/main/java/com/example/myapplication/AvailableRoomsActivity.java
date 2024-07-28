package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.example.Room;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class AvailableRoomsActivity extends AppCompatActivity {

    private ArrayList<Room> availableRooms;
    private ListView listViewRooms;
    private Button ReturnButton;
    private static final String TAG = "SocketClient";
    private static final String SERVER_IP = "192.168.56.1"; // Replace with your server's IP address
    private static final int SERVER_PORT = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_rooms);

        listViewRooms = findViewById(R.id.listViewRooms);

        ReturnButton=findViewById(R.id.ReturnButton);

        // Get the list of rooms from the intent
        Intent intent = getIntent();
        availableRooms = (ArrayList<Room>) intent.getSerializableExtra("rooms");
        String Time= intent.getStringExtra("time");

        // Populate the ListView with room names
        ArrayList<String> roomNames = new ArrayList<>();
        for (Room room : availableRooms) {
            roomNames.add(room.getRoomName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roomNames);
        listViewRooms.setAdapter(adapter);

        listViewRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Room selectedRoom = availableRooms.get(position);

                Room room= new Room();
                room=selectedRoom;
                String[] parts;
                parts = Time.split("-");
                // Perform booking using the selected room and number of rooms
                String bookedRoom= selectedRoom.getRoomName() + ":" + parts[0] + ":" + parts[1]+ ":" + "0";
                ClientThread clientThread=new ClientThread(bookedRoom);
                clientThread.start();
                try {
                    clientThread.join();
                    int Cond=clientThread.getNumber();
                    if(Cond==1){
                        Toast.makeText(AvailableRoomsActivity.this,"The room is looked", Toast.LENGTH_SHORT).show();
                    }else{
                        Intent bookingIntent = new Intent(AvailableRoomsActivity.this, BookingActivity.class);
                        bookingIntent.putExtra("selectedRoom", selectedRoom);
                        startActivity(bookingIntent);
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
    class ClientThread extends Thread {

        private void setNumber(int number){
            this.number=number;
        }
        public int getNumber(){
            return number;
        }
        private String room;
        private int number;
        private ClientThread(String room){
            this.room=room;
            this.number=0;
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
