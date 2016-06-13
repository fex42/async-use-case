package com.ing.diba.metrics;




import java.util.SortedMap;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;




public interface MetricsReporter
{

    public void report();




    /**
     * Should report all the given metrics.
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
    public abstract void report(@SuppressWarnings("rawtypes") SortedMap< String, Gauge > gauges,
                                SortedMap< String, Counter > counters,
                                SortedMap< String, Histogram > histograms,
                                SortedMap< String, Meter > meters,
                                SortedMap< String, Timer > timers);

}
