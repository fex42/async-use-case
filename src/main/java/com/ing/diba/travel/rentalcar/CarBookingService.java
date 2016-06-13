package com.ing.diba.travel.rentalcar;

import com.ing.diba.metrics.influxdb.IntervalInfluxdbReporter;
import com.ing.diba.travel.HolidayPackage;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

/**
 * Created by dhaa on 26.05.16.
 */
public class CarBookingService {

    private RentalCar rentalCar;
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

    public RentalCar getRentalCar() {
        return rentalCar;
    }

    public void setRentalCar(RentalCar rentalCar) {
        this.rentalCar = rentalCar;
    }

    public Message<?> process(Message<HolidayPackage> inputMessage) {
        HolidayPackage holidayPackage = inputMessage.getPayload();

        holidayPackage.hiredCar = rentalCar.book(holidayPackage.fromDay, holidayPackage.toDay, holidayPackage.customer);

        System.out.println("holidayPackage.key: " + holidayPackage.key);
        System.out.println("holidayPackage.hiredCar: " + holidayPackage.hiredCar);
        return new GenericMessage<HolidayPackage>(holidayPackage);
    }

}
