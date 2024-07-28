package org.example;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Room implements Serializable {
    @Serial
    private static final long serialVersionUID = 2569659724988268417L;
    private String roomName;
    private String area;
    private int noOfReviews;
    private int noOfPersons;
    private double stars;
    private String roomImage;
    private double price;
    private ArrayList<String> Booked;
    private int Blocked;

    public Room(String Name,int numbPerson,String Area,double stars,int reviews,double price,String img){
        this.roomName=Name;
        this.area=Area;
        this.noOfReviews=reviews;
        this.noOfPersons=numbPerson;
        this.stars=stars;
        this.roomImage=img;
        this.price = price;
        this.Booked=new ArrayList<>();
        this.Blocked=0;
    }

    public Room() {

    }

    public int getBlocked() {
        return Blocked;
    }

    public void setBlocked(int blocked) {
        this.Blocked = blocked;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean isBooked(LocalDate startDate, LocalDate endDate){
        String[] parts;
        for(String key: Booked){
            parts = key.split("-");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            try {
                System.out.println(key);
                LocalDate StartDate1 = LocalDate.parse(parts[0], formatter);
                LocalDate EndDate1 = LocalDate.parse(parts[1], formatter);
                if(!StartDate1.isAfter(endDate) && !EndDate1.isBefore(startDate)){
                    return  true;
                }

            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Invalid date format. Please enter the date in the format DD/MM/YYYY.");
            }
        }
        return false;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public int isBookedInPeriod(LocalDate startDate, LocalDate endDate){
        String[] parts;
        int count=0;
        for(String key: Booked){
            parts = key.split("-");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            try {
                LocalDate StartDate1 = LocalDate.parse(parts[0], formatter);
                LocalDate EndDate1 = LocalDate.parse(parts[1], formatter);
                if(startDate.isBefore(StartDate1) || endDate.isAfter(EndDate1)){
                    count++;
                }

            }catch(java.time.format.DateTimeParseException e) {
                System.out.println("Invalid date format. Please enter the date in the format DD-MM-YYYY.");
            }
        }
        return  count;
    }

    public void AddDate(LocalDate startdate,LocalDate enddate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String startString = startdate.format(formatter);
        String endString = enddate.format(formatter);
        Booked.add(startString+"-"+endString);
    }
    public ArrayList<String> getBooked(){
        return this.Booked;
    }
    public String getRoomName() {
        return roomName;
    }

    public String getArea() {
        return area;
    }

    public int getNoOfReviews() {
        return noOfReviews;
    }

    public int getNoOfPersons() {
        return noOfPersons;
    }

    public double getStars() {
        return stars;
    }

    public double getPrice() {
        return price;
    }

    public String getRoomImage() {
        return roomImage;
    }
}
