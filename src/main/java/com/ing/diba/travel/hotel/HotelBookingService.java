package com.ing.diba.travel.hotel;

import com.ing.diba.travel.HolidayPackage;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

/**
 * Created by dhaa on 26.05.16.
 */
public class HotelBookingService {

    private HotelBookingAgency hotelBookingAgency;

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

        System.out.println("holidayPackage.key: " + holidayPackage.key);
        System.out.println("holidayPackage.bookedRoom: " + holidayPackage.bookedRoom);
        return new GenericMessage<HolidayPackage>(holidayPackage);
    }

}
