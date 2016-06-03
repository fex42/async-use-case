package com.ing.diba.travel.airline;

import com.ing.diba.travel.HolidayPackage;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

/**
 * Created by dhaa on 26.05.16.
 */
public class AirlineBookingService {

    private Airline airline;

    public Airline getAirline() {
        return airline;
    }

    public void setAirline(Airline airline) {
        this.airline = airline;
    }

    public Message<?> process(Message<HolidayPackage> inputMessage) {
        HolidayPackage holidayPackage = inputMessage.getPayload();

        holidayPackage.bookedFlight = airline.book(holidayPackage.fromDay,
                                                   holidayPackage.toDay,
                                                   holidayPackage.customer);

        System.out.println("holidayPackage.key: " + holidayPackage.key);
        System.out.println("holidayPackage.bookedFlight: " + holidayPackage.bookedFlight);
        return new GenericMessage<HolidayPackage>(holidayPackage);
    }

}
