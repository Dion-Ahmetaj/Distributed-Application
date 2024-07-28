package org.example;


import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Worker{
    private ArrayList<Room> Rooms;
    private Socket connection;
    private final HashMap<Socket,Object> taskMap=new HashMap<>();
    private ArrayList<Object> taskQueue=new ArrayList<>();
    private final Object lock = new Object();
    private final ArrayList<WorkerThread> workerThreads ;
    private final int threadCount = 5;
    ObjectInputStream in=null;
    ObjectOutputStream out=null;
    private int num;

    public Worker() {
        this.Rooms=new ArrayList<>();
        this.workerThreads=new ArrayList<>();
    }

    public static void main(String[] args) {
        Worker worker=new Worker();
        worker.startWorker();
    }
    public void startWorker() {
        try {
            connection=new Socket("localhost",1234);
            this.out=new ObjectOutputStream(connection.getOutputStream());
            this.out.writeInt(6);
            this.out.flush();
            this.in = new ObjectInputStream(connection.getInputStream());
            while(true) {

                Object task=in.readObject();
                int num=in.readInt();
                int SocketToClient=-1;
                if(!(task instanceof Room)){
                    SocketToClient=in.readInt();
                    System.out.println(SocketToClient);
                }
                addTask(task,num,SocketToClient);

            }
        }catch(IOException e){
        e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void addTask(Object task,int num,int SocketToClient){
        System.out.println("Mesa sto addTask :"+ SocketToClient);
        WorkerThread thread=new WorkerThread(num,SocketToClient);
        workerThreads.add(thread);
        thread.start();
        taskQueue.add(task);
    }
    public void setBooked(String room, String startDate, String endDate){
        for(int i=0; i<this.Rooms.size(); i++){
            if(Rooms.get(i).getRoomName().equals(room)){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate date1 = LocalDate.parse(startDate, formatter);
                LocalDate date2 = LocalDate.parse(endDate, formatter);
                Rooms.get(i).AddDate(date1,date2);
                System.out.println("Room with name: "+ room+" is officialy booked");
                if(Rooms.get(i).isBooked(date1,date2)){
                    System.out.println("Room with name: "+ room+" is officialy booked");
                }
            }
        }

    }
    private class WorkerThread extends Thread {
        private Socket connection;
        ObjectInputStream in;
        ObjectOutputStream out;
        private final int num;
        private int SocketToClient;

        public WorkerThread(int num,int SocketToClient){
                this.num=num;
                this.SocketToClient=SocketToClient;
        }
        public void setSocketToClient(int number){
            this.SocketToClient=number;
        }
        public void run() {

            Object task = null;
            task = taskQueue.remove(0);
            try {
                processTask(task);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        private void processTask(Object task) throws IOException {
            if (task instanceof Room room) {
                Rooms.add(room);
                System.out.println("Add the room");
                System.out.println(Rooms.size());
                // Further processing for Room
            } else if (task instanceof Filter filter) {
                // Implement filter processing and response
                System.out.println("Filter the rooms");
                try {
                    ArrayList<Room> filteredRooms = filteredRooms(filter);
                        // Assuming 'masterAddress' is the IP of the Master and 'masterPort' is the port it listens on for results
                        Socket masterSocket = new Socket("localhost", 1236);
                        ObjectOutputStream outToMaster = new ObjectOutputStream(masterSocket.getOutputStream());
                        System.out.println("Sending to reducer");
                        //Send the Number of Workers
                        outToMaster.writeInt(num);
                        outToMaster.flush();

                        System.out.println(num);

                        outToMaster.writeInt(1);
                        outToMaster.flush();

                        //Send the Number that represent the Socket of Client
                        outToMaster.writeInt(SocketToClient);
                        outToMaster.flush();

                        System.out.println(SocketToClient);
                        //Send the Number of Workers
                        outToMaster.writeObject(filteredRooms); // Example of sending filtered rooms
                        outToMaster.flush();

                        outToMaster.close();
                        masterSocket.close();
                } catch (IOException e) {
                    System.out.println("Error processing filter task: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (task instanceof ArrayList<?>) {
                // Implement filter processing and response
                System.out.println("Filter the rooms");
                try {
                    // Assuming 'masterAddress' is the IP of the Master and 'masterPort' is the port it listens on for results
                    Socket masterSocket = new Socket("localhost", 1236);
                    ObjectOutputStream outToMaster = new ObjectOutputStream(masterSocket.getOutputStream());
                    System.out.println("Sending to reducer");
                    // Send results. You might need to customize this part based on your application logic.
                    outToMaster.writeInt(num);
                    outToMaster.flush();

                    outToMaster.writeInt(1);
                    outToMaster.flush();

                    outToMaster.writeInt(SocketToClient);
                    outToMaster.flush();

                    outToMaster.writeObject(Rooms); // Example of sending filtered rooms
                    outToMaster.flush();

                    outToMaster.close();
                    masterSocket.close();
                } catch (IOException e) {
                    System.out.println("Error processing filter task: " + e.getMessage());
                    e.printStackTrace();
                }
            }else if( task instanceof String ){
                System.out.println("Hello I under the water");
                System.out.println(task);
                String[] parts = task.toString().split(":");
                System.out.println(parts[0]);
                System.out.println(parts[1]);
                System.out.println(parts[2]);
                System.out.println(parts[3]);
                for(int i=0; i<Rooms.size(); i++){
                    if(Rooms.get(i).getRoomName().equals(parts[0])){
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate date1 = LocalDate.parse(parts[1], formatter);
                        LocalDate date2 = LocalDate.parse(parts[2], formatter);
                        try {
                            Socket WorkerToMaster = new Socket("localhost", 1236);
                            ObjectOutputStream outToMaster = new ObjectOutputStream(WorkerToMaster.getOutputStream());

                            outToMaster.writeInt(num);
                            outToMaster.flush();

                            outToMaster.writeInt(10);
                            outToMaster.flush();

                            System.out.println("Socekt:"+SocketToClient);

                            outToMaster.writeInt(SocketToClient);
                            outToMaster.flush();

                            if(parts[3].equals("1")){
                                Rooms.get(i).AddDate(date1,date2);
                                System.out.println("Room with name: "+ parts[0]+" is officially booked");
                                if(Rooms.get(i).isBooked(date1,date2)){
                                    System.out.println("Room with name: "+ parts[0]+" is officially booked");
                                }
                                outToMaster.writeInt(2);
                                outToMaster.flush();
                            }else if(parts[3].equals("0")){
                                System.out.println("eimai edw");
                                if(Rooms.get(i).getBlocked()==1){
                                    outToMaster.writeInt(1);
                                    outToMaster.flush();
                                }else{
                                    Rooms.get(i).setBlocked(1);
                                    outToMaster.writeInt(0);
                                    outToMaster.flush();
                                }
                            }
                            outToMaster.close();
                            WorkerToMaster.close();
                        }catch (IOException e) {
                            System.out.println("Error processing filter task: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            try {
                Thread.sleep(1000); // Simulate processing time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    private ArrayList<Room> filteredRooms(Filter filter) {
        ArrayList<Room> FilteredRooms = new ArrayList<>();
        for(Room room:Rooms){
            if(filter.isCompatalbe(room)){
                FilteredRooms.add(room);
            }
        }
        return FilteredRooms;
    }
}
