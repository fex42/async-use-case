package com.ing.diba.metrics;




import java.io.Closeable;
import java.util.SortedMap;

import org.slf4j.Logger;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
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
public abstract class ScheduledReporter implements Closeable, Reporter, MetricsReporter
{

    protected final MetricFilter   filter;

    protected final Logger         logger;

    protected final MetricRegistry registry;




    /**
     * Creates a new {@link ScheduledReporter} instance.
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
    protected ScheduledReporter(final Logger logger,
                                final MetricRegistry registry,
                                final MetricFilter filter)
    {
        this.logger = logger;
        this.registry = registry;
        this.filter = filter;
    }




    /**
     * Stops the reporter and shuts down its thread of execution.
     */
    @Override
    public void close()
    {
        try
        {
            stop();
        }
        catch (final InterruptedException e)
        {

        }
    }




    /**
     * Report the current values of all metrics in the registry.
     */
    @Override
    public void report()
    {
        report(this.registry.getGauges(this.filter), this.registry.getCounters(this.filter), this.registry.getHistograms(this.filter), this.registry.getMeters(this.filter), this.registry.getTimers(this.filter));
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




    /**
     * Starts the reporter pushing.
     */
    abstract public void start()
            throws InterruptedException;




    /**
     * Stops the reporter and shuts down its thread of execution.
     */
    abstract public void stop()
            throws InterruptedException;

}
