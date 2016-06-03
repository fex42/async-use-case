package com.ing.diba.travel.rentalcar;

/**
 * Created by dhaa on 26.05.16.
 */
public class HiredCar {

    public String location;
    public String name;
    public int fromDay;
    public int toDay;
    public int customer;

    public HiredCar() {

    }

    public HiredCar(Car car, int fromDay, int toDay, int customer) {
        this.location = car.getLocation();
        this.name = car.getIdentity();
        this.fromDay = fromDay;
        this.toDay = toDay;
        this.customer = customer;
    }
}
