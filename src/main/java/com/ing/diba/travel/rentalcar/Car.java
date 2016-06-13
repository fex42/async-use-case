package com.ing.diba.travel.rentalcar;


import com.ing.diba.travel.Bookable;
import com.ing.diba.travel.reservation.ReservationEngine;

import java.util.concurrent.atomic.AtomicInteger;


public class Car
        implements Bookable, Cloneable {

    private static AtomicInteger identityNumber = new AtomicInteger(0);
    private final String location;
    private final ReservationEngine reservationEngine;
    private String name;

    public Car(final String location, final int capacity) {
        this.location = location;
        this.reservationEngine = new ReservationEngine(capacity);
    }


    public boolean book(int fromDay, int toDay, int customer) {
        return reservationEngine.book(fromDay, toDay, customer, true);
    }

    public boolean cancel(int fromDay, int toDay, int customer) {
        return reservationEngine.cancel(fromDay, toDay, customer, true);
    }

    public int getCapacity() {
        return reservationEngine.getCapacityPerDay();
    }

    public String getLocation() {
        return location;
    }

    public String getIdentity() {
        if (name == null) {
            name = "" + identityNumber.incrementAndGet();
        }
        return name;
    }

    public boolean hasCapacity(int fromDay, int toDay, int customer)  {
        return reservationEngine.hasCapacity( fromDay,  toDay,  customer, true);
    }


    public int getCapacityOnDay(int onDay) {
        return reservationEngine.getCapacityOnDay(onDay);
    }

    @Override
    public Car clone()
            throws CloneNotSupportedException {
        return (Car) super.clone();
    }

}
