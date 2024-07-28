package org.example;

import java.io.*;
import java.net.*;
import java.util.*;

public class Master {

    private final HashMap<Integer,Socket> SocketToClient;
    private int port=1234;

    ObjectInputStream in;
    private ServerSocket serverSocket;

    private Socket socket=new Socket();

    private final int NumberOfWorkers;

    private ArrayList<Socket> SocketToWorker=new ArrayList<>();
    private ArrayList<ObjectInputStream> SocketToObjectIn=new ArrayList<>();
    private ArrayList<ObjectOutputStream> SocketToObjectOut=new ArrayList<>();

    public Master(int NumberOfWorkers,HashMap<Integer,Socket> SocketToClient){
        this.NumberOfWorkers = NumberOfWorkers;
        this.SocketToClient = SocketToClient;
    }
    public HashMap<Integer,Socket> getHashMap(){
        return this.SocketToClient;
    }
    public void setRandNumber(int number, Socket Socket){
        this.SocketToClient.put(number,Socket);
        System.out.println(number);
    }
    public static void main(String[] args){
        HashMap<Integer,Socket> SocketToClient=new HashMap<>();
        Master masterServer=new Master(3,SocketToClient);

        masterServer.StartServer();
    }
    public int h(String name){
        int hashCode = name.hashCode();
        return Math.abs(hashCode) % SocketToWorker.size();
    }
    public void StartServer(){
        try{
            serverSocket=new ServerSocket(port);
            System.out.println("Server connected.");
            int workerCount=0;
            while (true) {
                socket=serverSocket.accept();
                System.out.println("Client connected.");
                this.in = new ObjectInputStream(socket.getInputStream());
                int num = in.readInt();
                if(num==6) {
                    System.out.println("Worker connected.");
                    if(SocketToWorker.size()<NumberOfWorkers) {
                        SocketToWorker.add(socket);
                        SocketToObjectIn.add(in);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        SocketToObjectOut.add(out);
                        System.out.println(SocketToWorker.size());
                    }
                } else if (num==7) {
                    System.out.println("Reducer connected.");
                    int hashMapkey=in.readInt();

                    System.out.println(hashMapkey);

                    ArrayList<Room> Results = (ArrayList<Room>) in.readObject();

                    HashMap<Integer,Socket> IntegerToSocket=getHashMap();
                    if (IntegerToSocket.containsKey(hashMapkey)) {
                        System.out.println("Socket Found");
                        Socket socket2=IntegerToSocket.get(hashMapkey);
                        System.out.println(socket2);
                        ObjectOutputStream out = new ObjectOutputStream(socket2.getOutputStream());
                        out.writeObject(Results);
                        out.flush();
                    }
                }else if(num==10){
                    System.out.println("Reducer connected.On 10 ");
                    int hashMapkey=in.readInt();
                    System.out.println(hashMapkey);
                    int cond=in.readInt();
                    System.out.println(cond);


                    HashMap<Integer,Socket> IntegerToSocket=getHashMap();
                    if (IntegerToSocket.containsKey(hashMapkey)) {

                        System.out.println("Socket Found");

                        Socket socket3=IntegerToSocket.get(hashMapkey);
                        System.out.println(socket3);
                        ObjectOutputStream out = new ObjectOutputStream(socket3.getOutputStream());

                        out.writeInt(cond);
                        out.flush();
                        System.out.println("Cond was sent");
                    }
                }else{
                    if (num==3 || num==4) {
                        System.out.println("Tenant connected.");
                        System.out.println(socket);
                    }
                    Object receivedObject = in.readObject();
                    MasterThread Thread = new MasterThread(num, receivedObject);
                    System.out.println("Thread started");
                    Thread.start();

                }
            }
        }catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    private class MasterThread extends Thread{
        private int num;
        private Object receivedObject;

        public MasterThread(int num, Object receivedObject){
            this.num = num;
            this.receivedObject = receivedObject;
        }
        public void run() {
            changeWorker1(this.receivedObject, this.num);
        }
        public void changeWorker1(Object receivedObject, int num){
            try {
                if (receivedObject instanceof Room room && num == 1) {

                    int workerIndex = h(room.getRoomName());
                    System.out.println("Room with room name:" + room.getRoomName() + " Added to worker:" + workerIndex);
                    ObjectOutputStream outWorker = SocketToObjectOut.get(workerIndex);
                    synchronized (outWorker) {
                        outWorker.writeObject(receivedObject);
                        outWorker.flush();

                        outWorker.writeInt(SocketToWorker.size());
                        outWorker.flush();

                        outWorker.reset();
                    }

                } else if (receivedObject instanceof Filter filter) {
                    System.out.println("Processing the filter in area:" + ((Filter) receivedObject).getArea());

                    HashMap<Integer, Socket> IntegerToSocket = getHashMap();
                    Random random = new Random();
                    int x = -1;
                    int RandKey;
                    boolean cond = false;
                    do {
                        RandKey = random.nextInt(100);
                        if (!IntegerToSocket.containsKey(RandKey)) {
                            System.out.println("RandKey:" + RandKey);
                            // Key does not exist, so put the random number as a key with a corresponding value
                            setRandNumber(RandKey, socket);
                            cond = true;
                            x = RandKey;
                        }
                    } while (cond == false);
                    System.out.println(x);
                    for (int j = 0; j < SocketToWorker.size(); j++) {
                        Map(j, filter, x);
                    }
                }
                if (num == 2) {
                    ArrayList<Room> listOfArrays = new ArrayList<>();

                    System.out.println("Eimai edw");
                    HashMap<Integer, Socket> IntegerToSocket = getHashMap();
                    Random random = new Random();
                    int RandKey = -1;
                    while (true) {
                        RandKey = random.nextInt(100);
                        if (!IntegerToSocket.containsKey(RandKey)) {
                            // Key does not exist, so put the random number as a key with a corresponding value
                            setRandNumber(RandKey, socket);
                            break;
                        }
                    }
                    for (int i = 0; i < SocketToWorker.size(); i++) {
                        Map(i, listOfArrays, RandKey);
                        System.out.println("Esteila");
                    }
                } else if (num == 4) {
                    System.out.println("Eimai edw");
                    HashMap<Integer, Socket> IntegerToSocket = getHashMap();
                    Random random = new Random();
                    int RandKey = -1;
                    while (true) {
                        RandKey = random.nextInt(100);
                        if (!IntegerToSocket.containsKey(RandKey)) {
                            // Key does not exist, so put the random number as a key with a corresponding value
                            setRandNumber(RandKey, socket);
                            System.out.println(RandKey);
                            break;
                        }
                    }
                    for (int i = 0; i < SocketToWorker.size(); i++) {

                        ObjectOutputStream outWorker = SocketToObjectOut.get(i);
                        synchronized (outWorker) {
                        System.out.println("Sending Results to map");

                        outWorker.writeObject(receivedObject);
                        outWorker.flush();

                        outWorker.writeInt(SocketToWorker.size());
                        outWorker.flush();

                        outWorker.writeInt(RandKey);
                        outWorker.flush();
                        }
                    }

                }
            } catch (IOException e) {
                System.err.println("Failed to change workers: " + e.getMessage());
            }
        }
    }

    private void Map(int MapId,Object Object,int RandKey) throws IOException {
        ObjectOutputStream outWorker=SocketToObjectOut.get(MapId);
        System.out.println("Sending Results to map");
        synchronized (outWorker) {
            outWorker.writeObject(Object);
            outWorker.flush();

            System.out.println("Object Sent");

            outWorker.writeInt(SocketToWorker.size());
            outWorker.flush();

            System.out.println("Number of Workers Sent");

            outWorker.writeInt(RandKey);
            outWorker.flush();

            System.out.println("RandKey Sent");
        }
    }

}
