package org.example;


import java.io.*;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ManagerApp extends Thread{

    public static void main(String[] args) throws InterruptedException {

        ArrayList<Room> rooms = new ArrayList<>();
        HashMap<String,ArrayList<Room>> Reservations=new HashMap<>();
        int number;
        do {
            Scanner sc=new Scanner(System.in);
            System.out.println("Please choose one of the following\n1)Add file\n2)Total Reservations by Region\n3)Exit");
            number = sc.nextInt();

            if (number == 1) {
                JSONParser parser = new JSONParser();

                try {
                    Object obj = parser.parse(new FileReader("C:\\Users\\user\\IdeaProjects\\Kata2024\\src\\main\\java\\org\\example\\room_details_expanded.json"));
                    JSONObject jsonObject = (JSONObject) obj;

                    JSONArray roomList = (JSONArray) jsonObject.get("rooms");
                    Iterator<JSONObject> iterator = roomList.iterator();
                    while (iterator.hasNext()) {
                        JSONObject roomJson = iterator.next();
                        Room room = new Room(
                                (String) roomJson.get("roomName"),
                                ((Long) roomJson.get("noOfPersons")).intValue(),
                                (String) roomJson.get("area"),
                                ((Long) roomJson.get("stars")).doubleValue(),
                                ((Long) roomJson.get("noOfReviews")).intValue(),
                                ((Long) roomJson.get("price")).doubleValue(),
                                (String) roomJson.get("roomImage")
                        );
                        rooms.add(room);
                        System.out.println(room.getRoomName());

                        ArrayList<Room> roomList1 = Reservations.get(room.getArea());
                        if (roomList1 == null) {
                            // Area not present, create new list and add the room
                            roomList1 = new ArrayList<>();
                            roomList1.add(room);
                            Reservations.put(room.getArea(), roomList1);
                        } else {
                            // Area present, add the room to the existing list
                            roomList1.add(room);
                        }

                    }
                    for(Room room:rooms){
                        sc.nextLine();
                        System.out.println("Add availability period for room with name "+room.getRoomName()+" in area "+room.getArea()+"in format:(DD/MM/YYYY-DD/MM/YYY)");
                        String timeperiod=sc.nextLine();
                        room.setTimePeriod(timeperiod);
                        new ManagerApp(room,1, rooms).start();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(number==2) {
                String time;
                sc.nextLine();
                System.out.println("Enter Time Period(DD/MM/YYYY-DD/MM/YYY)");
                time=sc.nextLine();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.US);

                // Split the input string into start and end date strings
                String[] parts = time.split("-");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid date range format. Expected format: dd/MM/yyyy-dd/MM/yyyy");
                }

                LocalDate startDate = LocalDate.parse(parts[0], formatter);
                LocalDate endDate = LocalDate.parse(parts[1], formatter);

                Room room2 = new Room(null, 0,null,0,0,0, null);
                ManagerApp manager = new ManagerApp(room2,2,rooms);
                manager.start();
                manager.join();
                rooms = manager.getRooms();
                Set<String> keys = Reservations.keySet();
                for (String key : keys) {
                    int count=0;
                    for(Room room1:rooms){
                        if(room1.getArea().equals(key)){
                            count=count+room1.isBookedInPeriod(startDate,endDate);
                        }
                    }
                    System.out.println(key+":"+count);
                }
            }
        } while (number != 3);

    }
    Socket requestSocket;

    ObjectInputStream in=null;
    ObjectOutputStream out=null;
    private Room room;
    private int num;
    private ArrayList<Room> Rooms;

    public ManagerApp(Room room, int i, ArrayList<Room> rooms){
        this.room=room;
        this.num = i;
        this.Rooms = rooms;
    }
    public void run() {

        try {

            requestSocket=new Socket("localhost",1234);

            this.out=new ObjectOutputStream(requestSocket.getOutputStream());

            this.out.writeInt(num);
            this.out.flush();
            this.out.writeObject(room);
            this.out.flush();

            if (this.num == 2){
                this.in = new ObjectInputStream(requestSocket.getInputStream());
                ArrayList<Room> updater = new ArrayList<>();
                updater= (ArrayList<Room>) this.in.readObject();
                for (Room room:updater){
                    for(int i=0; i<Rooms.size();i++){
                        if(Rooms.get(i).getRoomName().equals(room.getRoomName())){
                            Rooms.set(i,room);
                        }
                    }
                }

            }


        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private ArrayList<Room> getRooms(){
        return this.Rooms;
    }
}
