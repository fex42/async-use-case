package com.ing.diba.metrics.influxdb;




import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import com.ing.diba.metrics.Start;
import org.slf4j.Logger;

import com.ing.diba.metrics.influxdb.client.InfluxdbClient;
import com.ing.diba.metrics.influxdb.client.SingleMetric;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.slf4j.LoggerFactory;


/**
 * A reporter which publishes metric values to a InfluxDB server.
 *
 * @see <a href="http://influxdb.org/">InfluxDB - An open-source distributed
 *      time series database with no external dependencies.</a>
 */
public class InfluxdbReporter
{

    enum MeasurementSchema
    {
        First_Level_Metric_On_Separate_TimeSeries,
        Metric_And_App_On_Separate_TimeSeries,
        Metric_On_Separate_TimeSeries;
    }

    private static String[]                COLUMNS_COUNT                = { "count" };

    private static String[]                COLUMNS_GAUGE                = {
                                                                                "value_i", // integer
            "value_f", // float
            "value_s", // string
            "value_b" // boolean
                                                                        };

    private static String[]                COLUMNS_HISTOGRAM            = {
                                                                                "count",
            "min",
            "max",
            "mean",
            "std_dev",
            "percentile_50",
            "percentile_70",
            "percentile_95",
            "percentile_99",
            "percentile_999",
            "run_count"                                                };

    private static String[]                COLUMNS_METER                = {
                                                                                "count",
            "one_minute",
            "five_minute",
            "fifteen_minute",
            "mean_rate"                                                };

    private static String[]                COLUMNS_TIMER                = {
                                                                                "count",
            "min",
            "max",
            "mean",
            "std_dev",
            "median",
            "percentile_70",
            "percentile_95",
            "percentile_99",
            "percentile_999",
            "one_minute",
            "five_minute",
            "fifteen_minute",
            "mean_rate",
            "run_count",
            "last_value"                                               };

    private final static MeasurementSchema currentMeasurementSchema     = MeasurementSchema.Metric_On_Separate_TimeSeries;

    private final String                   application;

    private final List< SingleMetric >     collectedMetricList;

    private int                            countLoggedMetrics           = 0;

    private int                            cycleCount                   = 0;

    private int                            cycleCountOverAll            = 0;

    private final InfluxdbClient           influxdbClient;

    private final Logger                   logger;

    private int                            maxCycleCountPerReport       = 50;

    private int                            maxMetricCountPerReport      = 1500;

    private final List< SingleMetric >     singlePassMetricList;




    public InfluxdbReporter(final InfluxdbClient influxdbClient)
    {
        this.collectedMetricList = new ArrayList< SingleMetric >(this.maxMetricCountPerReport + 500);
        this.singlePassMetricList = new ArrayList< SingleMetric >(100);
        this.logger = LoggerFactory.getLogger(InfluxdbReporter.class);
        this.application = "TravelAgency";
        this.influxdbClient = influxdbClient;
    }




    private void addDefaultTags(final SingleMetric singleMetric,
                                final String metric)
    {
        singleMetric.getTagMap()
                    .put("host", Start.canonicalHostName);
        singleMetric.getTagMap()
                    .put("pid", Start.processId);
        singleMetric.getTagMap()
                    .put("metric", metric);
        singleMetric.getTagMap()
                    .put("application", this.application);
    }




    private int findFieldValueType(final Object value)
    {
        // integer
        int fieldType = 0;

        // { "integer", "float", "string", "boolean" };
        if (value instanceof Number)
        {
            if ((value instanceof Float) || (value instanceof Double) || (value instanceof BigDecimal))
            {
                fieldType = 1;
            }
        }
        else if (value instanceof Boolean)
        {
            fieldType = 3;
        }
        else
        {
            // string
            fieldType = 2;
        }
        return fieldType;
    }




    private String firstURLString()
    {
        final URL[] urls = this.influxdbClient.getUrl();
        String urlStr = "<null array>";
        if (urls != null)
        {
            urlStr = (urls.length > 0 ? urls[0].toExternalForm() : "<empty array>");
        }
        return urlStr;
    }




    public int getMaxCycleCountPerReport()
    {
        return this.maxCycleCountPerReport;
    }




    public int getMaxMetricCountPerReport()
    {
        return this.maxMetricCountPerReport;
    }




    public String getPrefix()
    {
        return this.application;
    }




    private void initMeasurement(final SingleMetric singleMetric,
                                 final String name)
    {
        final String metric = prepareMeasurementName(name);

        switch (InfluxdbReporter.currentMeasurementSchema)
        {
            case Metric_On_Separate_TimeSeries:
                singleMetric.setMeasurement(metric);
                break;
            case Metric_And_App_On_Separate_TimeSeries:
                singleMetric.setMeasurement(this.application + "::" + metric);
                break;
            case First_Level_Metric_On_Separate_TimeSeries:
                final int index = metric.indexOf(".");
                if (index > 0)
                {
                    singleMetric.setMeasurement(this.application + "::" + metric.substring(0, index));
                }
                else
                {
                    singleMetric.setMeasurement(this.application + "::" + metric);
                }
                break;
            default:
                singleMetric.setMeasurement(this.application);
                break;
        }
    }




    private void logMetrics(final List< SingleMetric > metricList)
    {
        if (this.logger.isTraceEnabled())
        {
            final int countMetrics = metricList.size();
            if (this.countLoggedMetrics < countMetrics)
            {
                final String urlStr = firstURLString();
                this.countLoggedMetrics = countMetrics;
                this.logger.trace("===");
                this.logger.trace("=========");
                this.logger.trace("==================================== {} Metrics for one cycle, all data reported on URL [{}]", this.countLoggedMetrics, urlStr);

                for (int i = 0; metricList.size() > i; ++i)
                {
                    final SingleMetric metric = metricList.get(i);
                    this.logger.trace("{} reporting serie: [{}]", i, metric.build());
                }
                this.logger.trace("==================================== {} Metrics for one cycle, all data reported on URL [{}]", this.countLoggedMetrics, urlStr);
                this.logger.trace("=========");
                this.logger.trace("===");
            }
        }
    }




    private String prepareMeasurementName(final String name)
    {
        String metric = name.toLowerCase();
        metric = metric.replace('-', '_');
        metric = metric.replace(' ', '.');
        final int index = metric.indexOf(".");
        if (index == 0)
        {
            return metric.substring(index);
        }
        return metric;
    }




    @SuppressWarnings("rawtypes")
    public void report(final SortedMap< String, Gauge > gauges,
                       final SortedMap< String, Counter > counters,
                       final SortedMap< String, Histogram > histograms,
                       final SortedMap< String, Meter > meters,
                       final SortedMap< String, Timer > timers)
    {
        try
        {
            this.singlePassMetricList.clear();

            final long startNanos1 = System.nanoTime();

            final long timestamp = System.currentTimeMillis();
            ++this.cycleCountOverAll;
            ++this.cycleCount;

            if (this.logger.isTraceEnabled())
            {
                this.logger.trace("{} cycle count: gathering {} Gauges, {} Counters, {} Histograms, {} Meters, {} Timers", this.cycleCountOverAll, gauges.size(), counters.size(), histograms.size(), meters.size(), timers.size());
            }

            reportGauge(gauges, this.singlePassMetricList, timestamp);
            reportCounter(counters, this.singlePassMetricList, timestamp);
            reportHistogram(histograms, this.singlePassMetricList, timestamp);
            reportMeter(meters, this.singlePassMetricList, timestamp);
            reportTimer(timers, this.singlePassMetricList, timestamp);

            if (!this.singlePassMetricList.isEmpty())
            {
                this.collectedMetricList.addAll(this.singlePassMetricList);

                logMetrics(this.singlePassMetricList);

                if ((this.cycleCountOverAll <= 10) || (this.cycleCount >= this.maxCycleCountPerReport) || (this.collectedMetricList.size() >= this.maxMetricCountPerReport))
                {
                    reportMetrics(startNanos1);
                }
                else
                {
                    final long durationMillis1 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos1);
                    this.logger.debug("{} cycle count: {} series data waiting for report (duration over all {} ms)", this.cycleCountOverAll, this.collectedMetricList.size(), durationMillis1);
                }
            }
            else
            {
                final long durationMillis1 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos1);
                this.logger.info("{} cycle count: NO series data to report (duration over all {} ms) (2)", this.cycleCountOverAll, durationMillis1);
            }
        }
        catch (final Exception e)
        {
            final String urlStr = firstURLString();
            this.logger.trace("Exception while reporting on [" + urlStr + "] to InfluxDB: ", e);
        }
        finally
        {
            this.singlePassMetricList.clear();
        }
    }




    protected void reportCounter(final SortedMap< String, Counter > counters,
                                 final List< SingleMetric > singleMetricList,
                                 final long timestamp)
    {
        for (final Map.Entry< String, Counter > entry : counters.entrySet())
        {
            reportCounter(entry.getKey(), entry.getValue(), singleMetricList, timestamp);
        }
    }




    protected void reportCounter(final String name,
                                 final Counter counter,
                                 final List< SingleMetric > singleMetricList,
                                 final long timestamp)
    {
        final SingleMetric singleMetric = new SingleMetric();
        singleMetric.setTimestamp(timestamp);
        initMeasurement(singleMetric, name);
        addDefaultTags(singleMetric, name);
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_COUNT[0], counter.getCount());
        singleMetricList.add(singleMetric);
    }




    @SuppressWarnings("rawtypes")
    protected void reportGauge(final SortedMap< String, Gauge > gauges,
                               final List< SingleMetric > singleMetricList,
                               final long timestamp)
    {
        for (final Map.Entry< String, Gauge > entry : gauges.entrySet())
        {
            reportGauge(entry.getKey(), entry.getValue(), singleMetricList, timestamp);
        }
    }




    protected void reportGauge(final String name,
                               final Gauge< ? > gauge,
                               final List< SingleMetric > singleMetricList,
                               final long timestamp)
    {
        final SingleMetric singleMetric = new SingleMetric();
        singleMetric.setTimestamp(timestamp);
        initMeasurement(singleMetric, name);
        addDefaultTags(singleMetric, name);

        final Object value = gauge.getValue();
        // { "integer", "float", "string", "boolean" };
        final int fieldType = findFieldValueType(value);
        final int fieldTypeString = 2;
        if (fieldType != fieldTypeString)
        {
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_GAUGE[fieldType], value);
            singleMetricList.add(singleMetric);
        }
    }




    protected void reportHistogram(final SortedMap< String, Histogram > histograms,
                                   final List< SingleMetric > singleMetricList,
                                   final long timestamp)
    {
        for (final Map.Entry< String, Histogram > entry : histograms.entrySet())
        {
            reportHistogram(entry.getKey(), entry.getValue(), singleMetricList, timestamp);
        }
    }




    protected void reportHistogram(final String name,
                                   final Histogram histogram,
                                   final List< SingleMetric > singleMetricList,
                                   final long timestamp)
    {
        final SingleMetric singleMetric = new SingleMetric();
        singleMetric.setTimestamp(timestamp);
        initMeasurement(singleMetric, name);
        addDefaultTags(singleMetric, name);

        final Snapshot snapshot = histogram.getSnapshot();

        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[0], snapshot.size());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[1], snapshot.getMin());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[2], snapshot.getMax());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[3], snapshot.getMean());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[4], snapshot.getStdDev());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[5], snapshot.getMedian());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[6], snapshot.get75thPercentile());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[7], snapshot.get95thPercentile());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[8], snapshot.get99thPercentile());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[9], snapshot.get999thPercentile());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_HISTOGRAM[10], histogram.getCount());

        singleMetricList.add(singleMetric);
    }




    protected void reportMeter(final SortedMap< String, Meter > meters,
                               final List< SingleMetric > singleMetricList,
                               final long timestamp)
    {
        for (final Map.Entry< String, Meter > entry : meters.entrySet())
        {
            reportMeter(entry.getKey(), entry.getValue(), singleMetricList, timestamp);
        }
    }




    protected void reportMeter(final String name,
                               final Metered meter,
                               final List< SingleMetric > singleMetricList,
                               final long timestamp)
    {
        final SingleMetric singleMetric = new SingleMetric();
        singleMetric.setTimestamp(timestamp);
        initMeasurement(singleMetric, name);
        addDefaultTags(singleMetric, name);

        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_METER[0], meter.getCount());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_METER[1], meter.getOneMinuteRate());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_METER[2], meter.getFiveMinuteRate());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_METER[3], meter.getFifteenMinuteRate());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_METER[4], meter.getMeanRate());

        singleMetricList.add(singleMetric);
    }




    private void reportMetrics(final long startNanos1)
            throws Exception
    {
        try
        {
            this.cycleCount = 0;

            final long startNanos2 = System.nanoTime();
            final String reply = this.influxdbClient.writeData(this.collectedMetricList);
            final long durationMillis1 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos1);
            final long durationMillis2 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos2);

            if (this.logger.isInfoEnabled())
            {
                if ((reply != null) && (!reply.isEmpty()))
                {
                    final String urlStr = firstURLString();
                    this.logger.info("{} cycle count: added {} series to {} series over all, all data reported on URL {} with reply {}; (duration over all {} ms; duration request {} ms)", this.cycleCountOverAll, this.singlePassMetricList.size(), this.collectedMetricList.size(), urlStr, reply, durationMillis1, durationMillis2);
                }
                else
                {
                    this.logger.info("{} cycle count: NO series data to report; (duration over all {} ms; duration request {} ms) (1)", this.cycleCountOverAll, durationMillis1, durationMillis2);
                }
            }
        }
        finally
        {
            this.collectedMetricList.clear();
        }
    }




    protected void reportTimer(final SortedMap< String, Timer > timers,
                               final List< SingleMetric > singleMetricList,
                               final long timestamp)
    {
        for (final Map.Entry< String, Timer > entry : timers.entrySet())
        {
            reportTimer(entry.getKey(), entry.getValue(), singleMetricList, timestamp);
        }
    }




    protected void reportTimer(final String name,
                               final Timer timer,
                               final List< SingleMetric > singleMetricList,
                               final long timestamp)
    {
        final Snapshot snapshot = timer.getSnapshot();
        reportTimer(name, timer, snapshot, singleMetricList, timestamp);
    }




    protected void reportTimer(final String name,
                               final Timer timer,
                               final Snapshot snapshot,
                               final List< SingleMetric > singleMetricList,
                               final long timestamp)
    {
        final SingleMetric singleMetric = new SingleMetric();
        singleMetric.setTimestamp(timestamp);
        initMeasurement(singleMetric, name);
        addDefaultTags(singleMetric, name);

        if (snapshot != null)
        {
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_TIMER[0], snapshot.size());
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_TIMER[1], snapshot.getMin());
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_TIMER[2], snapshot.getMax());
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_TIMER[3], snapshot.getMean());
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_TIMER[4], snapshot.getStdDev());
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_TIMER[5], snapshot.getMedian());
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_TIMER[6], snapshot.get75thPercentile());
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_TIMER[7], snapshot.get95thPercentile());
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_TIMER[8], snapshot.get99thPercentile());
            singleMetric.getFieldMap()
                        .put(InfluxdbReporter.COLUMNS_TIMER[9], snapshot.get999thPercentile());
        }

        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_TIMER[10], timer.getOneMinuteRate());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_TIMER[11], timer.getFiveMinuteRate());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_TIMER[12], timer.getFifteenMinuteRate());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_TIMER[13], timer.getMeanRate());
        singleMetric.getFieldMap()
                    .put(InfluxdbReporter.COLUMNS_TIMER[14], timer.getCount());

        singleMetricList.add(singleMetric);
    }




    public void setMaxCycleCountPerReport(final int maxCycleCountPerReport)
    {
        this.maxCycleCountPerReport = maxCycleCountPerReport;
    }




    public void setMaxMetricCountPerReport(final int maxMetricCountPerReport)
    {
        this.maxMetricCountPerReport = maxMetricCountPerReport;
    }
}
