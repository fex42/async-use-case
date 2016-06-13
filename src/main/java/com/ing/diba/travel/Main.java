package com.ing.diba.travel;

import com.ing.diba.travel.reservation.ReservationEngine;

import java.io.IOException;

/**
 * Created by dhaa on 09.06.16.
 */
public class Main {

    public static void main(final String... args)
            throws InterruptedException, IOException {
        ReservationEngine reservationEngine = new ReservationEngine(0);

        int count = 0;
        for(int i=0; 1000 > i; ++i) {
            if (reservationEngine.book(0,0,0, true)) {
                ++count;
            }
        }
        System.out.println(count + "/" + 1000);
    }
}