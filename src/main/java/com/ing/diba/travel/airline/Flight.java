package com.ing.diba.travel.airline;

import com.ing.diba.travel.Bookable;
import com.ing.diba.travel.reservation.ReservationEngine;

import java.util.concurrent.atomic.AtomicInteger;

public class Flight
        implements Bookable, Cloneable {

    private static AtomicInteger identityNumber = new AtomicInteger(0);
    private final String route;
    private final ReservationEngine reservationEngine;
    private String name;

    public Flight(final String route, final int capacity) {
        this.route = route;
        this.reservationEngine = new ReservationEngine(capacity);
    }

    public boolean book(int fromDay, int toDay, int customer) {
        return reservationEngine.book(fromDay, toDay, customer, false);
    }

    public String getIdentity() {
        if (name == null) {
            name = "" + identityNumber.incrementAndGet();
        }
        return name;
    }

    public boolean cancel(int fromDay, int toDay, int customer) {
        return reservationEngine.cancel(fromDay, toDay, customer, false);
    }

    public boolean hasCapacity(int fromDay, int toDay, int customer) {
        return reservationEngine.hasCapacity(fromDay, toDay, customer, false);
    }

    public int getCapacity() {
        return reservationEngine.getCapacityPerDay();
    }

    public String getRoute() {
        return route;
    }

    public int getCapacityOnDay(int onDay) {
        return reservationEngine.getCapacityOnDay(onDay);
    }

    @Override
    public Flight clone()
            throws CloneNotSupportedException {
        return (Flight) super.clone();
    }

}


