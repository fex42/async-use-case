package com.ing.diba.travel.hotel;

import com.ing.diba.travel.Bookable;
import com.ing.diba.travel.reservation.ReservationEngine;

public class Hotel
implements Bookable, Cloneable {

    private final String location;

    private final String name;

    private final ReservationEngine reservationEngine;

    public Hotel(final String name, final String location, final int capacity) {
        this.name = name;
        this.location = location;
        this.reservationEngine = new ReservationEngine(capacity);
    }


    public boolean book(int fromDay, int toDay, int customer) {
            return reservationEngine.book(fromDay, toDay, customer, true);
     }

    @Override
    public int getCapacityOnDay(int onDay) {
        return 100;
    }

    public boolean cancel(int fromDay, int toDay, int customer) {
        return reservationEngine.cancel(fromDay, toDay, customer, true);
    }


    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public boolean hasCapacity(int fromDay, int toDay, int customer)  {
        return reservationEngine.hasCapacity( fromDay,  toDay,  customer, true);
    }


    @Override
    public Hotel clone() throws CloneNotSupportedException
    {
        return (Hotel) super.clone();
    }
}
