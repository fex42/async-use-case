package com.ing.diba.metrics;


import java.net.InetAddress;
import java.net.UnknownHostException;


public final class Start {

    public static final String applicationName;
    public static final String processId;
    public static final String userHome;
    public static String canonicalHostName;

    static {
        applicationName = "TravelAgency";
        userHome = "diba";
        processId = "7689";

        try {
            Start.canonicalHostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            Start.canonicalHostName = "Unknown-Host";
            System.err.println("Exception at Start<init>: " + e);
        }
    }


}
