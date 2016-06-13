package com.ing.diba.metrics.timer;




import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;




/**
 * This class does not enforce starting or stopping once and only once without a
 * reset.
 */
abstract public class AStopwatch implements Stopwatch
{

    private final AtomicLong durationTime = new AtomicLong(0L);

    private final AtomicLong startTime    = new AtomicLong(0L);




    /**
     * Returns the duration in nanoseconds. No checks are performed to ensure
     * that the stopwatch has been properly started and stopped before executing
     * this method. If called before stop it will return the last duration.
     */
    @Override
    public long getDuration()
    {
        return this.durationTime.get();
    }




    /**
     * {@inheritDoc}
     */
    @Override
    public long getDuration(final TimeUnit timeUnit)
    {
        return timeUnit.convert(getDuration(), TimeUnit.NANOSECONDS);
    }




    abstract protected long nanoTime();




    /**
     * {@inheritDoc}
     */
    @Override
    public void reset()
    {
        this.startTime.set(0L);
        this.durationTime.set(0L);
    }




    /**
     * {@inheritDoc}
     */
    @Override
    public void start()
    {
        this.startTime.set(nanoTime());
    }




    /**
     * {@inheritDoc}
     */
    @Override
    public void stop()
    {
        final long duration = nanoTime() - this.startTime.get();
        if (duration >= 0)
        {
            this.durationTime.set(duration);
        }
    }
}
