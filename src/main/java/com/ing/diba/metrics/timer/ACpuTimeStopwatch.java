package com.ing.diba.metrics.timer;




import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;




/**
 * "System time" is the cpu time spent running OS code on behalf of your application (such as for I/O).
 */
abstract public class ACpuTimeStopwatch extends AStopwatch
{

    protected static final boolean      isCpuTimeSupported;

    protected static final ThreadMXBean threadMXBean;

    static
    {
        threadMXBean = ManagementFactory.getThreadMXBean();

        if (ACpuTimeStopwatch.threadMXBean != null)
        {
            boolean isSupported = ACpuTimeStopwatch.threadMXBean.isCurrentThreadCpuTimeSupported();
            if (isSupported)
            {
                ACpuTimeStopwatch.threadMXBean.setThreadCpuTimeEnabled(true);
                isSupported = ACpuTimeStopwatch.threadMXBean.isThreadCpuTimeEnabled();
            }
            isCpuTimeSupported = isSupported;
        }
        else
        {
            isCpuTimeSupported = false;
        }
    }




    public ACpuTimeStopwatch()
    {
    }




    public boolean isCpuTimeSupported()
    {
        return ACpuTimeStopwatch.isCpuTimeSupported;
    }

}
