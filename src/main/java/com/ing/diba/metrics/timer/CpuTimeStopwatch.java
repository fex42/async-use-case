package com.ing.diba.metrics.timer;




/**
 * "System time" is the cpu time spent running OS code on behalf of your application (such as for I/O).
 */
public class CpuTimeStopwatch extends ACpuTimeStopwatch
{

    public CpuTimeStopwatch()
    {
    }




    /**
     * "System time" is the cpu time spent running OS code on behalf of your application (such as for I/O).
     */
    @Override
    public long nanoTime()
    {
        return ACpuTimeStopwatch.isCpuTimeSupported ? ACpuTimeStopwatch.threadMXBean.getCurrentThreadCpuTime() : 0L;
    }

}
