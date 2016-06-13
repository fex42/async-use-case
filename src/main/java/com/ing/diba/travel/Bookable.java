package com.ing.diba.travel;

public interface Bookable {

    public boolean cancel(int fromDay, int toDay, int customer);

    public boolean book(int fromDay, int toDay, int customer);

    public int getCapacityOnDay(int onDay);

}
