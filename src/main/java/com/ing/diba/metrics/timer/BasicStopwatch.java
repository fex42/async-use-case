package com.ing.diba.metrics.timer;




/**
 */
public class BasicStopwatch extends AStopwatch
{

    @Override
    protected long nanoTime()
    {
        return System.nanoTime();
    }

}
