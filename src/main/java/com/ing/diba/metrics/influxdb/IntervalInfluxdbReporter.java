package com.ing.diba.metrics.influxdb;


import com.codahale.metrics.*;
import com.codahale.metrics.jvm.*;
import com.ing.diba.metrics.IntervalReporter;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.SortedMap;


/**
 * A reporter which publishes metric values to a InfluxDB server.
 *
 * @see <a href="http://influxdb.org/">InfluxDB - An open-source distributed time series database with no external dependencies.</a>
 */
public class IntervalInfluxdbReporter
        extends IntervalReporter {


    private final InfluxdbReporter influxdbReporter;
    private final MetricRegistry metricRegistry;

    public IntervalInfluxdbReporter(final MetricRegistry registry, final InfluxdbReporter influxdbReporter) {
        super(LoggerFactory.getLogger(IntervalInfluxdbReporter.class), registry, MetricFilter.ALL, "influxdb-reporter");
        this.metricRegistry = registry;
        this.influxdbReporter = influxdbReporter;
        registerDefaultMetrics();
    }


    protected void registerDefaultMetrics() {
        this.metricRegistry.register("jvm.attribute", new JvmAttributeGaugeSet());
        this.metricRegistry.register("jvm.buffers",new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        this.metricRegistry.register("jvm.classloader", new ClassLoadingGaugeSet());
        this.metricRegistry.register("jvm.filedescriptor", new FileDescriptorRatioGauge());
        this.metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
        this.metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        this.metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet());
    }


    @Override
    @SuppressWarnings("rawtypes")
    public void report(final SortedMap<String, Gauge> gauges, final SortedMap<String, Counter> counters,
                       final SortedMap<String, Histogram> histograms, final SortedMap<String, Meter> meters,
                       final SortedMap<String, Timer> timers) {
        this.influxdbReporter.report(gauges, counters, histograms, meters, timers);
    }

}
