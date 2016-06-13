package com.ing.diba.travel.reservation;

import java.util.Random;

public class ReservationEngine {
    private final Random random = new Random();
    private long avgValue = 100;
    private long sdValue = 50;
    private long limitValue = 200;

    public ReservationEngine(final int capacity) {
    }

    public int getCapacityPerDay() {
        return 100;
    }

    public int getCapacityOnDay(int onDay) {
        return 100;
    }

    public boolean cancel(int fromDay, int toDay, int customer, boolean continuous) {
        return true;
    }


    public boolean hasCapacity(int fromDay, int toDay, int customer, boolean continuous) {
        return true;
    }

    public boolean book(int fromDay, int toDay, int customer, boolean continuous) {
        final double v = (this.random.nextGaussian() * this.sdValue) + this.avgValue;
        //System.out.println(v);
        return v < limitValue;
    }

}
