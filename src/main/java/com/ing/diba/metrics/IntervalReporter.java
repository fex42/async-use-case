package com.ing.diba.metrics;




import java.util.SortedMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;




/**
 * The abstract base class for all scheduled reporters (i.e., reporters which
 * process a registry's
 * metrics periodically).
 *
 * @see ConsoleReporter
 * @see CsvReporter
 * @see Slf4jReporter
 */
public abstract class IntervalReporter extends ScheduledReporter
{

    /**
     * A simple named thread factory.
     */
    private static class NamedThreadFactory implements ThreadFactory
    {

        private final ThreadGroup   group;

        private final String        namePrefix;

        private final AtomicInteger threadNumber = new AtomicInteger(1);




        private NamedThreadFactory(final String name)
        {
            final SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
                                     .getThreadGroup();
            this.namePrefix = "metrics-" + name + "-thread-";
        }




        @Override
        public Thread newThread(final Runnable r)
        {
            final Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0);
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
            {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    private static final AtomicInteger     FACTORY_ID = new AtomicInteger();

    private final ScheduledExecutorService scheduledExecutorService;




    /**
     * Creates a new {@link IntervalReporter} instance.
     *
     * @param registry
     *            the {@link com.codahale.metrics.MetricRegistry} containing the
     *            metrics this
     *            reporter will report
     * @param filter
     *            the filter for which metrics to report
     * @param executor
     *            the executor to use while scheduling reporting of metrics.
     */
    protected IntervalReporter(final Logger logger,
                               final MetricRegistry registry,
                               final MetricFilter filter,
                               final ScheduledExecutorService executor)
    {
        super(logger, registry, filter);
        this.scheduledExecutorService = executor;
    }




    /**
     * Creates a new {@link IntervalReporter} instance.
     *
     * @param registry
     *            the {@link com.codahale.metrics.MetricRegistry} containing the
     *            metrics this
     *            reporter will report
     * @param name
     *            the reporter's name
     * @param filter
     *            the filter for which metrics to report
     * @param rateUnit
     *            a unit of time
     * @param durationUnit
     *            a unit of time
     */
    protected IntervalReporter(final Logger logger,
                               final MetricRegistry registry,
                               final MetricFilter filter,
                               final String name)
    {
        this(logger, registry, filter, Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name + '-' + IntervalReporter.FACTORY_ID.incrementAndGet())));
    }




    /**
     * Called periodically by the polling thread. Subclasses should report all
     * the given metrics.
     *
     * @param gauges
     *            all of the gauges in the registry
     * @param counters
     *            all of the counters in the registry
     * @param histograms
     *            all of the histograms in the registry
     * @param meters
     *            all of the meters in the registry
     * @param timers
     *            all of the timers in the registry
     */
    @Override
    public abstract void report(@SuppressWarnings("rawtypes") SortedMap< String, Gauge > gauges,
                                SortedMap< String, Counter > counters,
                                SortedMap< String, Histogram > histograms,
                                SortedMap< String, Meter > meters,
                                SortedMap< String, Timer > timers);




    @Override
    public void start()
            throws InterruptedException
    {
        start(5, TimeUnit.SECONDS);
    }




    /**
     * Starts the reporter polling at the given period.
     *
     * @param period
     *            the amount of time between polls
     * @param unit
     *            the unit for {@code period}
     */
    public void start(final long period,
                      final TimeUnit unit)
    {
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run()
            {
                try
                {
                    report();
                }
                catch (final RuntimeException ex)
                {
                    IntervalReporter.this.logger.error("RuntimeException thrown from {}#report. Exception was suppressed.", IntervalReporter.this.getClass()
                                                       .getSimpleName(), ex);
                }
            }
        }, period, period, unit);
    }




    /**
     * Stops the reporter and shuts down its thread of execution.
     * Uses the shutdown pattern from
     * http://docs.oracle.com/javase/7/docs/api/java
     * /util/concurrent/ExecutorService.html
     */
    @Override
    public void stop()
    {
        this.scheduledExecutorService.shutdown(); // Disable new tasks from
        // being submitted
        try
        {
            // Wait a while for existing tasks to terminate
            if (!this.scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS))
            {
                this.scheduledExecutorService.shutdownNow(); // Cancel currently
                // executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!this.scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS))
                {
                    System.err.println(getClass().getSimpleName() + ": ScheduledExecutorService did not terminate");
                }
            }
        }
        catch (final InterruptedException ie)
        {
            // (Re-)Cancel if current thread also interrupted
            this.scheduledExecutorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread()
            .interrupt();
        }
    }

}
