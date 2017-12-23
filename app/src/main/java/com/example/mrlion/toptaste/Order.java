package com.example.mrlion.toptaste;

/**
 * Created by Mr Lion on 12/9/2017.
 */

public class Order {
    private String Name;
    private String PhoneNumber;
    private String Location;
    private String ItemsOrdered;

    public Order(){

    }

    public void setItemsOrdered(String itemsOrdered) {
        this.ItemsOrdered = itemsOrdered;
    }

    public String getItemsOrdered() {

        return ItemsOrdered;
    }

    public Order(String name, String phoneNumber, String location, String itemsOrdered) {
        Name = name;
        PhoneNumber = phoneNumber;
        Location = location;
        ItemsOrdered = itemsOrdered;
    }

    public String getName() {
        return Name;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public String getLocation() {
        return Location;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.PhoneNumber = phoneNumber;
    }

    public void setLocation(String location) {
        this.Location = location;
    }
}
