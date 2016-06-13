package com.ing.diba.travel;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import java.io.IOException;

/**
 * Created by dhaa on 26.05.16.
 */
public class CarMain {

    public static void main(final String... args)
            throws InterruptedException, IOException {

        @SuppressWarnings("resource")
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext(
                "classpath:spring-integration-rentalcar.xml");



        System.out.println("waiting");
        System.in.read();
        System.exit(0);
    }
}
