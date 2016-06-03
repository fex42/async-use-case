package com.ing.diba.travel.reservation;

public class ReservationEngine {

    private final int capacityPerDay;

    private final int[] reservationPerDay = new int[365];

    private int[] reservationPerDayWork = new int[365];

    public ReservationEngine(final int capacity) {
        this.capacityPerDay = capacity;
        init(reservationPerDay);
    }

    public boolean cancel(int fromDay, int toDay, int customer, boolean continuous) {
        synchronized (reservationPerDayWork) {
            System.arraycopy(reservationPerDay, 0, reservationPerDayWork, 0, reservationPerDay.length);

            if (continuous) {
                for (int i = fromDay; toDay >= i; ++i) {
                    if (!cancelOnDay(i, customer)) {
                        return false;
                    }
                }
            } else {
                if (!cancelOnDay(fromDay, customer) || !cancelOnDay(toDay, customer)) {
                    return false;
                }
            }

            System.arraycopy(reservationPerDayWork, 0, reservationPerDay, 0, reservationPerDay.length);
            return true;
        }
    }

    private boolean cancelOnDay(int onDay, int customer) {
        if ((reservationPerDayWork[onDay] + customer) <= capacityPerDay) {
            reservationPerDayWork[onDay] = reservationPerDayWork[onDay] + customer;
            return true;
        }
        return false;
    }

    private boolean bookOnDay(int onDay, int customer) {
        if (reservationPerDayWork[onDay] >= customer) {
            reservationPerDayWork[onDay] = reservationPerDayWork[onDay] - customer;
            return true;
        }
        return false;
    }


    public int getCapacityPerDay() {
        return capacityPerDay;
    }

    public int[] getReservationPerDay() {
        return reservationPerDay;
    }

    private void init(int[] reservationArray) {
        for (int i = 0; reservationArray.length > i; ++i) {
            reservationArray[i] = capacityPerDay;
        }
    }


    public int getCapacityOnDay(int onDay) {
        return reservationPerDay[onDay];
    }

    public boolean hasCapacity(int fromDay, int toDay, int customer, boolean continuous) {

        if (continuous) {
            for (int i = fromDay; toDay >= i; ++i) {
                if (reservationPerDay[i] >= customer) {
                    return true;
                }
            }
        } else {
            return ((reservationPerDay[fromDay] >= customer) && (reservationPerDay[toDay] >= customer));
        }

        return false;
    }

    public boolean book(int fromDay, int toDay, int customer, boolean continuous) {
        synchronized (reservationPerDayWork) {
            System.arraycopy(reservationPerDay, 0, reservationPerDayWork, 0, reservationPerDay.length);

            if (continuous) {
                for (int i = fromDay; toDay >= i; ++i) {
                    if (!bookOnDay(i, customer)) {
                        return false;
                    }
                }
            } else {
                if (!bookOnDay(fromDay, customer) || !bookOnDay(toDay, customer)) {
                    return false;
                }
            }

            System.arraycopy(reservationPerDayWork, 0, reservationPerDay, 0, reservationPerDay.length);
            return true;
        }
    }

}
