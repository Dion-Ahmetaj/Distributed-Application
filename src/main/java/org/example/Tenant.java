package org.example;

import java.io.*;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.time.*;
import java.util.*;
import java.time.format.DateTimeFormatter;

public class Tenant extends Thread{

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello Customer");
        int answer=0;
        String area="";
        String time="01/01/2024-05/01/2024";
        int numb=0;
        double price=0.0;
        double stars=0.0;
        Scanner sc=new Scanner(System.in);
        int answer2=0;
        do{
            do {
                System.out.println("Please Choose Filters:\n" +
                        "1)Area\n" +
                        "2)Time Period\n" +
                        "3)Number of tenants\n" +
                        "4)Price\n" +
                        "5)Stars");
                answer=sc.nextInt();
                sc.nextLine();
                switch (answer){
                    case 1:
                        System.out.println("Enter Area");
                        area=sc.nextLine();
                        break;
                    case 2:
                        System.out.println("Enter Time Period(DD/MM/YYYY-DD/MM/YYY)");
                        time=sc.nextLine();
                        sc.nextLine();
                        break;
                    case 3:
                        do{
                            System.out.println("Enter Number of tenants");
                            numb=sc.nextInt();
                            sc.nextLine();
                        }while(numb<=0);
                        break;
                    case 4:
                        do{
                            System.out.println("Enter Price");
                            price=sc.nextInt();
                            sc.nextLine();
                        }while (price<0);
                        break;
                    case 5:
                        do{
                            System.out.println("Enter Stars");
                            stars=sc.nextDouble();
                            sc.nextLine();
                        }while (stars<0.0 || stars>5.0);
                        break;
                }

            }while (answer<1 || answer>5);
            System.out.println("Do you want to enter another filter:\n1)Yes\n2)No");
            answer2= sc.nextInt();
        }while (answer2==1);
        sc.nextLine();
        Filter filter=new Filter(area,time,numb,price,stars);
        Tenant tenant=new Tenant(filter, 3 , "","","");
        tenant.start();
        tenant.join();
        ArrayList<Room> rooms1=tenant.getRooms();
        if(rooms1.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        } else {
            for (int i = 0; i < rooms1.size(); i++) {
                System.out.println((i + 1) + ") " + rooms1.get(i).getRoomName());
            }
        }
        System.out.println("Enter the number of the room you wish to choose:");
        int choice=sc.nextInt();

        if(choice < 1 || choice > rooms1.size()) {
            System.out.println("Invalid choice. Exiting.");
            return;
        }

        Room selectedRoom = rooms1.get(choice - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Split the input string into start and end date strings
        String[] parts =  filter.getTime().split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid date range format. Expected format: dd/MM/yyyy-dd/MM/yyyy");
        }
        LocalDate startDate = LocalDate.parse(parts[0], formatter);
        LocalDate endDate = LocalDate.parse(parts[1], formatter);
        System.out.println("You selected: " + selectedRoom.getRoomName());
        Tenant tenant1 = new Tenant(filter, 4, selectedRoom.getRoomName(), parts[0], parts[1]);
        tenant1.start();

        tenant1.join();
        int num1=tenant1.getNum();
        System.out.println("Enter the number of the tenant you wish to choose:"+num1);

    }
    Socket requestSocket;
    ObjectInputStream in=null;
    ObjectOutputStream out=null;
    private Filter filter;
    private  int num;
    private String name;
    private String startDate;
    private String endDate;
    private int num1;

    private ArrayList<Room> rooms;
    public Tenant(Filter filter,int num,String name, String startDate, String endDate) {
        this.filter=filter;
        this.num=num;
        this.name=name;
        this.startDate=startDate;
        this.endDate=endDate;
        this.rooms=new ArrayList<>();
        this.num1=-1;
    }

    private static boolean book(Room room, LocalDate startDate, LocalDate endDate) {
        synchronized (room) {
            if (!room.isBooked(startDate, endDate)) {
                room.AddDate(startDate, endDate);
                System.out.println("Room " + room.getRoomName() + " successfully booked from " + startDate + " to " + endDate);
                return true;
            } else {
                System.out.println("Room " + room.getRoomName() + " is not available for the selected dates.");
                return false;
            }
        }
    }

    public void run() {

        try {
            //10.26.40.36
            requestSocket=new Socket("localhost",1234);

            this.out=new ObjectOutputStream(requestSocket.getOutputStream());

            if(num==3) {
                this.out.writeInt(3);
                this.out.flush();
                this.out.writeObject(filter);
                this.out.flush();

                this.in = new ObjectInputStream(requestSocket.getInputStream());

                rooms = (ArrayList<Room>) this.in.readObject();

            }else {
                out.writeInt(4);
                out.flush();

                out.writeObject(name + ":" + startDate + ":" + endDate+ ":" +"0");
                out.flush();

                this.in = new ObjectInputStream(requestSocket.getInputStream());

                System.out.println("Waiting to read an integer from the server...");
                int number = in.readInt();
                num1 = number;
                System.out.println("Read integer: " + number);

                if (number == 0) {
                    System.out.println(number);
                } else if (number == 1) {
                    System.out.println(number);
                } else if (number == 2) {
                    System.out.println(number);
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
    public ArrayList<Room> getRooms(){
        return this.rooms;
    }
    public int getNum() {
        return this.num1;
    }
}
