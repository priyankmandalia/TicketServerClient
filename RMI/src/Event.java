

import java.io.Serializable;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author up705759
 */
public class Event implements Serializable{
    

    String title;
    String description;
    ArrayList<String> bookings;

    public Event(String title, String description) {
        this.title = title;
        this.description = description;
        this.bookings = new ArrayList();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getBookings() {
        return bookings;
    }

    public void setBookings(ArrayList<String> bookings) {
        this.bookings = bookings;
    }
    
    public void addBooking(String booking) {
        this.bookings.add(booking);
    }
    
    public int getBookingSize(){
    
        return this.bookings.size();
    
    }
    

    
    
}


