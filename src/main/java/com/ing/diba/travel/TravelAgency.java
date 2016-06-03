package com.ing.diba.travel;

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

    public TravelAgency() {
        holidayPackageMap = new ConcurrentHashMap<String, HolidayPackage>();
    }

    public AmqpTemplate getAmqpTemplate() {
        return amqpTemplate;
    }

    public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public Message<?> process(Message<?> inputMessage) {


        return null;
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
        if ((holidayPackage.hiredCar != null) && (holidayPackage.bookedFlight != null)  && (holidayPackage.bookedRoom != null)) {
            System.out.println("holidayPackage.key: " + holidayPackage.key);
            System.out.println("holidayPackage.hiredCar: " + holidayPackage.hiredCar);
            System.out.println("holidayPackage.bookedFlight: " + holidayPackage.bookedFlight);
            System.out.println("holidayPackage.bookedRoom: " + holidayPackage.bookedRoom);
        }
    }


}
