package com.ing.diba.travel;

import com.ing.diba.travel.airline.BookedFlight;
import com.ing.diba.travel.hotel.BookedRoom;
import com.ing.diba.travel.rentalcar.HiredCar;

/**
 * Created by dhaa on 26.05.16.
 */
public class HolidayPackage {
    public String key;

    public int fromDay;
    public int toDay;
    public int customer;

    public BookedFlight bookedFlight;
    public BookedRoom bookedRoom;
    public HiredCar hiredCar;
}


