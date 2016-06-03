package com.ing.diba.travel.airline;

import java.util.HashMap;
import java.util.Map;

public class Airline {
    private final String[] daySchedule = {"morning-1", "lunchtime-1", "evening-1",
                                          "morning-2", "lunchtime-2", "evening-2",
                                          "morning-3", "lunchtime-3", "evening-3"};

    private final String route;

    @SuppressWarnings("unchecked")
    private Map<String, Flight>[] reservationPool = new Map[365];

    public Airline(final String route, Flight[] flights) throws CloneNotSupportedException {
        this.route = route;
        initReservationPool(flights);
    }

    public BookedFlight book(int fromDay, int toDay, int customer) {
        synchronized (reservationPool) {
            BookedFlight bookedFlight = null;

            for (String name : reservationPool[fromDay].keySet()) {
                Flight flight = reservationPool[fromDay].get(name);
                if (flight.hasCapacity(fromDay, toDay, customer)) {
                    if (flight.book(fromDay, toDay, customer)) {
                        bookedFlight = new BookedFlight(flight, fromDay, toDay, customer);
                        break;
                    }
                }
            }

            return bookedFlight;
        }
    }

    public int getCapacityOnDay(int onDay) {
        int capacityOnDay = 0;
        for (String dayTime : reservationPool[onDay].keySet()) {
            Flight flight = reservationPool[onDay].get(dayTime);
            capacityOnDay += flight.getCapacityOnDay(onDay);
        }
        return capacityOnDay;
    }

    public String getRoute() {
        return route;
    }

    public void initReservationPool(Flight[] flights)
            throws CloneNotSupportedException {
        for (int i = 0; reservationPool.length > i; ++i) {
            reservationPool[i] = new HashMap<String, Flight>();
            for (Flight flight : flights) {
                for(String dayTime : daySchedule)
                {
                    reservationPool[i].put(dayTime, flight.clone());
                }
            }
        }
    }

}
