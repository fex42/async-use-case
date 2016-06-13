package com.ing.diba.metrics.timer;




import java.util.concurrent.TimeUnit;




public class ShortLivedStopwatch implements Stopwatch
{

    private final Stopwatch delegate;

    private final int       maxReadCount = 10;

    private volatile int    readCounter  = 0;




    public ShortLivedStopwatch(final Stopwatch delegate)
    {
        this.delegate = delegate;
    }




    @Override
    public long getDuration()
    {
        return (++this.readCounter < this.maxReadCount ? this.delegate.getDuration() : 0);
    }




    @Override
    public long getDuration(final TimeUnit timeUnit)
    {
        return (++this.readCounter < this.maxReadCount ? this.delegate.getDuration(timeUnit) : 0);
    }




    @Override
    public void reset()
    {
        this.readCounter = 0;
        this.delegate.reset();
    }




    @Override
    public void start()
    {
        this.readCounter = 0;
        this.delegate.start();
    }




    @Override
    public void stop()
    {
        this.readCounter = 0;
        this.delegate.stop();
    }

}
