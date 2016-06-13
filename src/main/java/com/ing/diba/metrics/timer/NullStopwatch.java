package com.ing.diba.metrics.timer;




import java.util.concurrent.TimeUnit;




public class NullStopwatch implements Stopwatch
{

    @Override
    public long getDuration()
    {

        return 0;
    }




    @Override
    public long getDuration(final TimeUnit timeUnit)
    {

        return 0;
    }




    @Override
    public void reset()
    {

    }




    @Override
    public void start()
    {

    }




    @Override
    public void stop()
    {

    }

}
