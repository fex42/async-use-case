package com.ing.diba.travel.hotel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dhaa on 26.05.16.
 */
public class BookedRoom {
    private static AtomicInteger identityNumber = new AtomicInteger(0);
    public String location;
    public String hotelName;
    public int roomNumber = identityNumber.incrementAndGet() % 1000;
    public int fromDay;
    public int toDay;
    public int customer;

    public BookedRoom() {

    }

    public BookedRoom(Hotel hotel, int fromDay, int toDay, int customer) {
        this.location = hotel.getLocation();
        this.hotelName = hotel.getName();
        this.fromDay = fromDay;
        this.toDay = toDay;
        this.customer = customer;
    }

    public static AtomicInteger getIdentityNumber() {
        return identityNumber;
    }

    public static void setIdentityNumber(AtomicInteger identityNumber) {
        BookedRoom.identityNumber = identityNumber;
    }

    public String getLocation() {
        return location;
    }

    public String getHotelName() {
        return hotelName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getFromDay() {
        return fromDay;
    }

    public int getToDay() {
        return toDay;
    }

    public int getCustomer() {
        return customer;
    }
}
