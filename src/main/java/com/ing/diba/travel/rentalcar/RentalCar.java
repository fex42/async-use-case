package com.ing.diba.travel.rentalcar;


import java.util.HashMap;
import java.util.Map;


public class RentalCar {
    private final String location;

    @SuppressWarnings("unchecked")
    private Map<Integer, Car>[] reservationPool = new Map[365];

    public RentalCar(final String location, int countCars, Car car) throws CloneNotSupportedException {
        this.location = location;
        initReservationPool(countCars, car);
    }

    public HiredCar book(int fromDay, int toDay, int customer) {
        synchronized (reservationPool) {
            HiredCar hiredCar = null;

            for (Integer number : reservationPool[fromDay].keySet()) {
                Car car = reservationPool[fromDay].get(number);
                if (car.hasCapacity(fromDay, toDay, car.getCapacity())) {
                    if (car.book(fromDay, toDay, car.getCapacity())) {
                        hiredCar = new HiredCar(car, fromDay, toDay, customer);
                        break;
                    }
                }
            }

            return hiredCar;
        }
    }

    public int getCapacityOnDay(int onDay) {
        int capacityOnDay = 0;
        for (Integer number : reservationPool[onDay].keySet()) {
            Car car = reservationPool[onDay].get(number);
            capacityOnDay += car.getCapacityOnDay(onDay);
        }
        return capacityOnDay;
    }

    public String getLocation() {
        return location;
    }

    public void initReservationPool(int countCars, Car car)
            throws CloneNotSupportedException {
        for (int i = 0; reservationPool.length > i; ++i) {
            reservationPool[i] = new HashMap<Integer, Car>();
            for (int j = 0; countCars > j; ++j) {
                reservationPool[i].put(j, car.clone());
            }
        }
    }

}
