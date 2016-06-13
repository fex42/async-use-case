package com.ing.diba.travel.hotel;

import com.ing.diba.metrics.influxdb.IntervalInfluxdbReporter;
import com.ing.diba.travel.HolidayPackage;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

/**
 * Created by dhaa on 26.05.16.
 */
public class HotelBookingService {

    private HotelBookingAgency hotelBookingAgency;
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

    public HotelBookingAgency getHotelBookingAgency() {
        return hotelBookingAgency;
    }

    public void setHotelBookingAgency(HotelBookingAgency hotelBookingAgency) {
        this.hotelBookingAgency = hotelBookingAgency;
    }

    public Message<?> process(Message<HolidayPackage> inputMessage) {
        HolidayPackage holidayPackage = inputMessage.getPayload();

        holidayPackage.bookedRoom = hotelBookingAgency.book(holidayPackage.fromDay,
                                                            holidayPackage.toDay,
                                                            holidayPackage.customer);

       return new GenericMessage<HolidayPackage>(holidayPackage);
    }

}
