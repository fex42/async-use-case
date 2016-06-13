package com.ing.diba.metrics.timer;




import java.util.concurrent.TimeUnit;




/**
 * Measures the time taken for execution of some code.
 */
public interface Stopwatch
{

    /**
     * Returns the duration in nanoseconds.
     */
    public long getDuration();




    /**
     * Returns the duration in the specified time unit.
     */
    public long getDuration(TimeUnit timeUnit);




    /**
     * Reset the stopwatch so that it can be used again.
     */
    public void reset();




    /**
     * Mark the start time.
     */
    public void start();




    /**
     * Mark the end time.
     */
    public void stop();
}
