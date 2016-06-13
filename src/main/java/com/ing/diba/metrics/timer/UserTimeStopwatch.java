package com.ing.diba.metrics.timer;




/**
 * "User time" is the cpu time spent running your application's own code.
 */
public class UserTimeStopwatch extends ACpuTimeStopwatch
{

    public UserTimeStopwatch()
    {
    }




    /**
     * "User time" is the cpu time spent running your application's own code.
     */
    @Override
    public long nanoTime()
    {
        return ACpuTimeStopwatch.isCpuTimeSupported ? ACpuTimeStopwatch.threadMXBean.getCurrentThreadUserTime() : 0L;
    }

}
