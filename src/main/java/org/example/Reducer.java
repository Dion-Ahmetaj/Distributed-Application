package org.example;

import java.io.*;
import java.net.*;
import java.util.*;

public class Reducer {
    private ServerSocket serverSocket;
    private int NumberOfWorkers=0;
    private Socket socket=new Socket();
    private int port=1236;
    private HashMap<Integer ,Integer > ClientIndexToRoomsMap=new HashMap<>() ;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    public static void main(String[] args) {
        Reducer reducer=new Reducer();
        reducer.StartReducer();
    }
    private class ReducerThread extends Thread{

        private ArrayList<Room> filterdRooms;
        private ArrayList<Room> rooms;
        private SharedResult sharedResult;

        public ReducerThread(ArrayList<Room> filterdRooms,ArrayList<Room> Rooms,SharedResult sharedResult){
            this.rooms=Rooms;
            this.filterdRooms=filterdRooms;
            this.sharedResult=sharedResult;
        }

        @Override
        public void run() {
            ArrayList<Room> newFilteredRooms = new ArrayList<>(filterdRooms);
            for(Room room:rooms){
                boolean found = false;
                for (Room filteredRoom : filterdRooms) {
                    if (filteredRoom.getRoomName().equals(room.getRoomName()) && filteredRoom.getArea().equals(room.getArea())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    newFilteredRooms.add(room);
                }
            }
            sharedResult.setResult(newFilteredRooms);
        }
    }
    public class SharedResult {
        private ArrayList<Room> result; // Field to store the result

        // Method to set the result
        public synchronized void setResult(ArrayList<Room> result) {
            this.result = result;
        }

        // Method to get the result
        public synchronized ArrayList<Room> getResult() {
            return result;
        }
    }

    private void StartReducer(){
        try {
            serverSocket=new ServerSocket(port);
            System.out.println("Server connected.");
            int count=0;
            ArrayList<Room> filteredRooms=new ArrayList<>();;
            while (true) {
                socket=serverSocket.accept();
                System.out.println("Worker is connected.");
                this.in=new ObjectInputStream(socket.getInputStream());
                NumberOfWorkers=in.readInt();

                System.out.println("Number of workers: "+NumberOfWorkers);

                int cond;
                cond=in.readInt();

                int NumberToSocket;
                NumberToSocket=in.readInt();

                System.out.println("Number of rooms to socket: "+NumberToSocket);
                if(cond==1) {
                    ArrayList<Room> rooms= (ArrayList<Room>) in.readObject();
                    if (!ClientIndexToRoomsMap.containsKey(NumberToSocket)) {
                        ClientIndexToRoomsMap.put(NumberToSocket, 1);
                        SharedResult sharedResult = new SharedResult();
                        System.out.println(filteredRooms.size());
                        ReducerThread thread = new ReducerThread(filteredRooms, rooms, sharedResult);
                        thread.start();
                        thread.join();
                        filteredRooms = sharedResult.getResult();
                        System.out.println(filteredRooms.size());
                    } else {
                        ClientIndexToRoomsMap.compute(NumberToSocket, (k, WorkerCounter) -> WorkerCounter);
                        SharedResult sharedResult = new SharedResult();
                        System.out.println(filteredRooms.size());
                        ReducerThread thread = new ReducerThread(filteredRooms, rooms, sharedResult);
                        thread.start();
                        thread.join();
                        filteredRooms = sharedResult.getResult();
                        System.out.println(filteredRooms.size());
                    }
                    if (ClientIndexToRoomsMap.get(NumberToSocket) >= NumberOfWorkers) {
                        System.out.println("Feugo gia Master");
                        System.out.println(filteredRooms.size());
                        Socket masterSocket = new Socket("localhost", 1234);
                        ObjectOutputStream outToMaster = new ObjectOutputStream(masterSocket.getOutputStream());
                        System.out.println("trying to connect to Master");
                        // Send results. You might need to customize this part based on your application logic.
                        outToMaster.writeInt(7); // Example of sending filtered rooms
                        outToMaster.flush();

                        outToMaster.writeInt(NumberToSocket); // Example of sending filtered rooms
                        outToMaster.flush();

                        outToMaster.writeObject(filteredRooms); // Example of sending filtered rooms
                        outToMaster.flush();

                        ClientIndexToRoomsMap.remove(NumberToSocket);
                        outToMaster.close();
                        masterSocket.close();
                        filteredRooms.clear();
                    }
                }else if(cond==10){
                    int cond2=in.readInt();
                    Socket masterSocket = new Socket("localhost", 1234);
                    ObjectOutputStream outToMaster = new ObjectOutputStream(masterSocket.getOutputStream());
                    System.out.println("trying to connect to Master");
                    // Send results. You might need to customize this part based on your application logic.
                    outToMaster.writeInt(10); // Example of sending filtered rooms
                    outToMaster.flush();

                    outToMaster.writeInt(NumberToSocket); // Example of sending filtered rooms
                    outToMaster.flush();

                    outToMaster.writeInt(cond2); // Example of sending filtered rooms
                    outToMaster.flush();

                }
            }
        }catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
