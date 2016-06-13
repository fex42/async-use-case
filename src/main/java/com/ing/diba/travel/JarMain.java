package com.ing.diba.travel;

import java.lang.reflect.Method;

/**
 * Created by diba on 13.06.16.
 */
public class JarMain {

    public static void main(final String... args)
            throws Exception {
        Class<?> mainClass = Class.forName(args[0]);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        String[] params = null;
        mainMethod.invoke(null, (Object) params);
    }

}
