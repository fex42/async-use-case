package com.ing.diba.travel.airline;

/**
 * Created by dhaa on 26.05.16.
 */
public class BookedFlight {

    public String route;
    public String name;
    public int fromDay;
    public int toDay;
    public int customer;


    public BookedFlight() {
    }

    public BookedFlight(Flight flight, int fromDay, int toDay, int customer) {
        this.route = flight.getRoute();
        this.name = flight.getIdentity();
        this.fromDay = fromDay;
        this.toDay = toDay;
        this.customer = customer;
    }
}
