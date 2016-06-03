package com.ing.diba.travel;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by dhaa on 26.05.16.
 */
public class TestMain {
    private AmqpTemplate amqpTemplate;

    public static void main(final String... args)
            throws InterruptedException, IOException {

        @SuppressWarnings("resource")
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext(
                "classpath:spring-integration-test.xml");

        TestMain testMain = context.getBean("testMain", TestMain.class);

        HolidayPackage holidayPackage = new HolidayPackage();
        holidayPackage.key = UUID.randomUUID().toString();
        holidayPackage.fromDay = 5;
        holidayPackage.toDay = 15;
        holidayPackage.customer = 3;

        String exchange1 = "travel.booking.request.room.ex";
        String routingKey1 = "travel.booking.room";
        testMain.amqpTemplate.convertAndSend(exchange1, routingKey1, holidayPackage);

        String exchange2 = "travel.booking.request.flight.ex";
        String routingKey2 = "travel.booking.flight";
        testMain.amqpTemplate.convertAndSend(exchange2, routingKey2, holidayPackage);

        String exchange3 = "travel.booking.request.car.ex";
        String routingKey3 = "travel.booking.car";
        testMain.amqpTemplate.convertAndSend(exchange3, routingKey3, holidayPackage);

        System.out.println("waiting");
        System.in.read();
        System.exit(0);
    }

    public AmqpTemplate getAmqpTemplate() {
        return amqpTemplate;
    }

    public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }
}
