package org.example;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Filter implements Serializable {

    private static final long serialVersionUID = 2884540969011670796L;
    private String area="";
    private String time="";
    private int numb=0;
    private double price=0.0;
    private double stars=0.0;

    public Filter(String area,String time,int numb,double price,double stars){
        this.area=area;
        this.time=time;
        this.numb=numb;
        this.price=price;
        this.stars=stars;
    }

    public Filter() {

    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getNumb() {
        return numb;
    }

    public void setNumb(int numb) {
        this.numb = numb;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getStars() {
        return stars;
    }
    public void setStars(double stars) {
        this.stars = stars;
    }
    public boolean isCompatalbe(Room room){
        if(!this.getArea().equals(room.getArea())){
            return false;
        }
        // Check if stars match
        if (this.getStars() != room.getStars()) {
            return false;
        }

        // Check if price is within acceptable range
        if (this.getPrice() < room.getPrice()) {
            return false;
        }

        // Check if room can accommodate the number of persons
        if (this.getNumb() != room.getNoOfPersons()) {
            return false;
        }

        String[] parts=this.getTime().split("-");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate StartDate1 = LocalDate.parse(parts[0], formatter);
            LocalDate EndDate1 = LocalDate.parse(parts[1], formatter);
            if(room.isBooked(StartDate1,EndDate1)){
                return false;
            }
        }catch (java.time.format.DateTimeParseException e){
            e.printStackTrace();
        }
        return true;
    }
}
