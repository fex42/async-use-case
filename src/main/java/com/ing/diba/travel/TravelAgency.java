package com.ing.diba.travel;

import com.ing.diba.metrics.influxdb.IntervalInfluxdbReporter;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.messaging.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by dhaa on 26.05.16.
 */
public class TravelAgency {

    private final ConcurrentMap<String, HolidayPackage> holidayPackageMap;

    private AmqpTemplate amqpTemplate;
    private IntervalInfluxdbReporter intervalInfluxdbReporter;

    public TravelAgency() {
        holidayPackageMap = new ConcurrentHashMap<String, HolidayPackage>();
    }

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

    public AmqpTemplate getAmqpTemplate() {
        return amqpTemplate;
    }

    public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }


    public void processHotel(Message<HolidayPackage> inputMessage) {
        final HolidayPackage payload = inputMessage.getPayload();
        HolidayPackage holidayPackage = holidayPackageMap.putIfAbsent(payload.key, payload);
        holidayPackage = (holidayPackage != null ? holidayPackage : payload);

        holidayPackage.bookedRoom = (payload.bookedRoom != null ? payload.bookedRoom : holidayPackage.bookedRoom);

        print(holidayPackage);
    }


    public void processAirline(Message<HolidayPackage> inputMessage) {
        final HolidayPackage payload = inputMessage.getPayload();
        HolidayPackage holidayPackage = holidayPackageMap.putIfAbsent(payload.key, payload);
        holidayPackage = (holidayPackage != null ? holidayPackage : payload);

        holidayPackage.bookedFlight = (payload.bookedFlight != null ? payload.bookedFlight : holidayPackage.bookedFlight);

        print(holidayPackage);
    }


    public void processCar(Message<HolidayPackage> inputMessage) {
        final HolidayPackage payload = inputMessage.getPayload();
        HolidayPackage holidayPackage = holidayPackageMap.putIfAbsent(payload.key, payload);
        holidayPackage = (holidayPackage != null ? holidayPackage : payload);

        holidayPackage.hiredCar = (payload.hiredCar != null ? payload.hiredCar : holidayPackage.hiredCar);

        print(holidayPackage);
    }

    private void print(HolidayPackage holidayPackage) {
        if ((holidayPackage.hiredCar != null) && (holidayPackage.bookedFlight != null) && (holidayPackage.bookedRoom != null)) {
            System.out.println("holidayPackage.key: " + holidayPackage.key + " booked");
        }
    }


}
