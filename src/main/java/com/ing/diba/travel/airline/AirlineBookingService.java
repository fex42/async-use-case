package com.ing.diba.travel.airline;

import com.ing.diba.metrics.influxdb.IntervalInfluxdbReporter;
import com.ing.diba.travel.HolidayPackage;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

/**
 * Created by dhaa on 26.05.16.
 */
public class AirlineBookingService {

    private Airline airline;
    private IntervalInfluxdbReporter intervalInfluxdbReporter;

    public IntervalInfluxdbReporter getIntervalInfluxdbReporter() {
        return intervalInfluxdbReporter;
    }

    public void setIntervalInfluxdbReporter(IntervalInfluxdbReporter intervalInfluxdbReporter)
            throws InterruptedException {
        this.intervalInfluxdbReporter = intervalInfluxdbReporter;
        if (this.intervalInfluxdbReporter != null) {
            this.intervalInfluxdbReporter.start();
        }
    }

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
