package com.ing.diba.travel.hotel;

import java.util.HashMap;
import java.util.Map;

public class HotelBookingAgency {
    private final String location;

    @SuppressWarnings("unchecked")
    private Map<String, Hotel>[] reservationPool = new Map[365];

    public HotelBookingAgency(final String location, Hotel[] contractHotels)
            throws CloneNotSupportedException {
        this.location = location;
        initReservationPool(contractHotels);
    }

    public BookedRoom book(int fromDay, int toDay, int customer) {
        synchronized (reservationPool) {
            BookedRoom bookedRoom = null;

            for (String name : reservationPool[fromDay].keySet()) {
                Hotel hotel = reservationPool[fromDay].get(name);
                System.out.println(hotel.hasCapacity(fromDay, toDay, customer));
                if (hotel.hasCapacity(fromDay, toDay, customer)) {
                    if (hotel.book(fromDay, toDay, customer)) {
                        bookedRoom = new BookedRoom(hotel, fromDay, toDay, customer);
                        break;
                    }
                }
            }

            return bookedRoom;
        }
    }

    public int getCapacityOnDay(int onDay) {
        int capacityOnDay = 0;
        for (String name : reservationPool[onDay].keySet()) {
            Hotel hotel = reservationPool[onDay].get(name);
            capacityOnDay += hotel.getCapacityOnDay(onDay);
        }
        return capacityOnDay;
    }

    public String getLocation() {
        return location;
    }

    public void initReservationPool(Hotel[] contractHotels)
            throws CloneNotSupportedException {
        for (int i = 0; reservationPool.length > i; ++i) {
            reservationPool[i] = new HashMap<String, Hotel>();
            for (Hotel hotel : contractHotels) {
                reservationPool[i].put(hotel.getName(), hotel.clone());
            }
        }
    }

}
