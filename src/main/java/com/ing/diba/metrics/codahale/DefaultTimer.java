package com.ing.diba.metrics.codahale;




import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.ing.diba.metrics.Start;
import org.slf4j.Logger;

import com.ing.diba.metrics.average.ExponentialWeightedMovingAverage;
import com.ing.diba.metrics.average.SimpleMovingAverage;
import com.ing.diba.metrics.average.SimpleMovingMedian;
import com.ing.diba.metrics.average.WeightedMovingAverage;
import com.ing.diba.metrics.frequency.ArrayBucketBasedFrequency;
import com.ing.diba.metrics.frequency.Frequency;
import com.ing.diba.metrics.frequency.NullFrequency;
import com.ing.diba.metrics.timer.BasicStopwatch;
import com.ing.diba.metrics.timer.CpuTimeStopwatch;
import com.ing.diba.metrics.timer.NullStopwatch;
import com.ing.diba.metrics.timer.ShortLivedStopwatch;
import com.ing.diba.metrics.timer.Stopwatch;
import com.ing.diba.metrics.timer.UserTimeStopwatch;
import com.ing.diba.metrics.Measurement;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.LoggerFactory;


/**
 * Created by dhaa on 10.12.15.
 */
public class DefaultTimer implements Measurement
{

    private static final Logger                       logger         = LoggerFactory.getLogger(DefaultTimer.class);
    private static final NullStopwatch                NULL_STOPWATCH = new NullStopwatch();
    private final AtomicReference< Context >          currentContext;
    private String                                    baseName       = "<not yet initialized>";

    private AtomicLong                                callCounter;
    private volatile ExponentialWeightedMovingAverage ewma03Cpu;
    private volatile ExponentialWeightedMovingAverage ewma03Real;
    private volatile ExponentialWeightedMovingAverage ewma06Real;
    private volatile ExponentialWeightedMovingAverage ewma08Real;
    private volatile boolean                          isCpuTime      = false;
    private volatile boolean                          isRealTime     = false;
    private volatile boolean                          isUserTime     = false;
    private volatile Frequency                        loadAverage;
    private volatile SimpleMovingAverage              smaReal;
    private volatile SimpleMovingMedian               smmReal;
    private volatile WeightedMovingAverage            wmaReal;
    private MetricRegistry                            metricRegistry;

    public DefaultTimer()
    {
        final Context context = new Context(0L);
        context.reset();
        this.currentContext = new AtomicReference< Context >(context);
        this.loadAverage = new NullFrequency();
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    private boolean hasNames(final String[] names)
    {
        final boolean isNotNull = (names != null) && (names.length > 0);

        if (isNotNull)
        {
            for (final String name : names)
            {
                if ((name == null) || name.trim()
                                          .isEmpty())
                {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean init(final MetricType[] metricTypes,
                        final String name,
                        final String[] names)
    {
        this.baseName = MetricRegistry.name(name, names);

        if (metricRegistry != null)
        {
            if ((name != null) && (!name.isEmpty()) && hasNames(names))
            {
                try
                {
                    registerCallCount(metricTypes, metricRegistry);
                    registerRealTime(metricTypes, metricRegistry);
                    registerCpuTime(metricTypes, metricRegistry);
                    registerLoadAvg(metricTypes, metricRegistry);
                }
                catch (final Exception e)
                {
                    DefaultTimer.logger.warn("Unable to register default-timer for names [{}] and [{}]; Caught Exception: " + e, name, Arrays.toString(names));
                    DefaultTimer.logger.warn("Caught Exception: " + e, e);
                }
            }
            else
            {
                DefaultTimer.logger.warn("Unable to register default-timer for empty or null names");
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean init(final String name,
                        final String[] names)
    {
        final MetricType[] metricTypes = {
                MetricType.CallCount,
                MetricType.RealTime,
                MetricType.RealTimeEwma03,
                MetricType.CPUTime,
                MetricType.CPUTimeEwma03,
                MetricType.CPUTimePercent,
                MetricType.LoadAvgOne,
                MetricType.LoadAvgFive,
                MetricType.LoadAvgFifteen };

        return init(metricTypes, name, names);
    }

    private void registerCallCount(final MetricType[] metricTypes,
                                   final MetricRegistry metricRegistry)
    {
        for (final MetricType type : metricTypes)
        {
            switch (type)
            {
                case CallCount:
                    registerGaugeCallCount(type, metricRegistry);
                    break;
                default:
                    break;
            }
        }
    }

    private void registerCpuTime(final MetricType[] metricTypes,
                                 final MetricRegistry metricRegistry)
    {
        for (final MetricType type : metricTypes)
        {
            switch (type)
            {
                case CPUTime:
                    this.isCpuTime = true;
                    registerGaugeCPU(type, metricRegistry);
                    break;
                case CPUTimeEwma03:
                    this.isCpuTime = true;
                    this.ewma03Cpu = new ExponentialWeightedMovingAverage(0.3);
                    registerGaugeCPUEwma03(type, metricRegistry);
                    break;
                case CPUTimePercent:
                    this.isCpuTime = true;
                    registerGaugeCPUPercent(type, metricRegistry);
                    break;
                case UserTime:
                    this.isUserTime = true;
                    registerGaugeUser(type, metricRegistry);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public <T> void registerGauge(final String suffix,
                                  final Callable< T > gaugeValue)
    {
        final String theSuffix = (suffix.startsWith(".") ? suffix : "." + suffix);
        final String metricName = this.baseName + theSuffix;
        try
        {
            final T theValue = gaugeValue.call();

            if (metricRegistry != null)
            {
                DefaultTimer.logger.info("Install measurements for micro-application '{}' on {} namespace; First value {}", Start.applicationName, metricName, theValue);
                metricRegistry.register(metricName, new Gauge< T >() {

                    @Override
                    public T getValue()
                    {
                        try
                        {
                            return gaugeValue.call();
                        }
                        catch (final Exception ex)
                        {
                            DefaultTimer.logger.error("Unable to get value for namespace {} because of Exception: {}", metricName, ex.toString());
                            return null;
                        }
                    }
                });
            }
        }
        catch (final Exception ex)
        {
            DefaultTimer.logger.error("Unable to register Gauge for namespace {} because of Exception: {}", metricName, ex.toString());
        }
    }

    private void registerGaugeCallCount(final MetricType type,
                                        final MetricRegistry metricRegistry)
    {
        this.callCounter = new AtomicLong(0L);
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Long >() {

            @Override
            public Long getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return context.callCount;
            }
        });
    }

    private void registerGaugeCPU(final MetricType type,
                                  final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Long >() {

            @Override
            public Long getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return context.wrappedCpuTimeStopwatch.getDuration();
            }
        });
    }

    private void registerGaugeCPUEwma03(final MetricType type,
                                        final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Double >() {

            @Override
            public Double getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return DefaultTimer.this.ewma03Cpu.calculate(context.wrappedCpuTimeStopwatch.getDuration());
            }
        });
    }

    private void registerGaugeCPUPercent(final MetricType type,
                                         final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Double >() {

            @Override
            public Double getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                final double cpu = context.wrappedCpuTimeStopwatch.getDuration() + Double.MIN_VALUE;
                final double real = context.wrappedBasicStopwatch.getDuration() + Double.MIN_VALUE;
                final double percent = cpu / real;
                return (real <= 0.1d ? 0d : (Math.round(percent * 1000d) / 1000d));
            }
        });
    }

    private void registerGaugeLoadAvgFifteen(final MetricType type,
                                             final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Integer >() {

            @Override
            public Integer getValue()
            {
                return DefaultTimer.this.loadAverage.valueAtCurrent(15) / 15;
            }
        });
    }

    private void registerGaugeLoadAvgFive(final MetricType type,
                                          final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Integer >() {

            @Override
            public Integer getValue()
            {
                return DefaultTimer.this.loadAverage.valueAtCurrent(5) / 5;
            }
        });
    }

    private void registerGaugeLoadAvgOne(final MetricType type,
                                         final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Integer >() {

            @Override
            public Integer getValue()
            {
                return DefaultTimer.this.loadAverage.valueAtCurrent(1);
            }
        });
    }

    private void registerGaugeReal(final MetricType type,
                                   final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Long >() {

            @Override
            public Long getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return context.wrappedBasicStopwatch.getDuration();
            }
        });
    }

    private void registerGaugeRealEwma03(final MetricType type,
                                         final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Double >() {

            @Override
            public Double getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return DefaultTimer.this.ewma03Real.calculate(context.wrappedBasicStopwatch.getDuration());
            }
        });
    }

    private void registerGaugeRealEwma06(final MetricType type,
                                         final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Double >() {

            @Override
            public Double getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return DefaultTimer.this.ewma06Real.calculate(context.wrappedBasicStopwatch.getDuration());
            }
        });
    }

    private void registerGaugeRealEwma08(final MetricType type,
                                         final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Double >() {

            @Override
            public Double getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return DefaultTimer.this.ewma08Real.calculate(context.wrappedBasicStopwatch.getDuration());
            }
        });
    }

    private void registerGaugeRealSma(final MetricType type,
                                      final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Double >() {

            @Override
            public Double getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return DefaultTimer.this.smaReal.calculate(context.wrappedBasicStopwatch.getDuration());
            }
        });
    }

    private void registerGaugeRealSmm(final MetricType type,
                                      final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Double >() {

            @Override
            public Double getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return DefaultTimer.this.smmReal.calculate(context.wrappedBasicStopwatch.getDuration());
            }
        });
    }

    private void registerGaugeRealWma(final MetricType type,
                                      final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Double >() {

            @Override
            public Double getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return DefaultTimer.this.wmaReal.calculate(context.wrappedBasicStopwatch.getDuration());
            }
        });
    }

    private void registerGaugeUser(final MetricType type,
                                   final MetricRegistry metricRegistry)
    {
        DefaultTimer.logger.info("Install measurements for micro-application '{}' on {}{} namespace", Start.applicationName, this.baseName, type.suffix);

        metricRegistry.register(this.baseName + type.suffix, new Gauge< Long >() {

            @Override
            public Long getValue()
            {
                final Context context = DefaultTimer.this.currentContext.get();
                return context.wrappedUserTimeStopwatch.getDuration();
            }
        });
    }

    private void registerLoadAvg(final MetricType[] metricTypes,
                                 final MetricRegistry metricRegistry)
    {
        for (final MetricType type : metricTypes)
        {
            switch (type)
            {
                case LoadAvgFifteen:
                    this.loadAverage = new ArrayBucketBasedFrequency();
                    registerGaugeLoadAvgFifteen(type, metricRegistry);
                    break;
                case LoadAvgFive:
                    this.loadAverage = new ArrayBucketBasedFrequency();
                    registerGaugeLoadAvgFive(type, metricRegistry);
                    break;
                case LoadAvgOne:
                    this.loadAverage = new ArrayBucketBasedFrequency();
                    registerGaugeLoadAvgOne(type, metricRegistry);
                    break;
                default:
                    break;
            }
        }
    }

    private void registerRealTime(final MetricType[] metricTypes,
                                  final MetricRegistry metricRegistry)
    {
        for (final MetricType type : metricTypes)
        {
            switch (type)
            {
                case RealTime:
                    this.isRealTime = true;
                    registerGaugeReal(type, metricRegistry);
                    break;
                case RealTimeEwma03:
                    this.isRealTime = true;
                    this.ewma03Real = new ExponentialWeightedMovingAverage(0.3);
                    registerGaugeRealEwma03(type, metricRegistry);
                    break;
                case RealTimeEwma06:
                    this.isRealTime = true;
                    this.ewma06Real = new ExponentialWeightedMovingAverage(0.6);
                    registerGaugeRealEwma06(type, metricRegistry);
                    break;
                case RealTimeEwma08:
                    this.isRealTime = true;
                    this.ewma08Real = new ExponentialWeightedMovingAverage(0.8);
                    registerGaugeRealEwma08(type, metricRegistry);
                    break;
                case RealTimeSma:
                    this.isRealTime = true;
                    this.smaReal = new SimpleMovingAverage(type.valueRange);
                    registerGaugeRealSma(type, metricRegistry);
                    break;
                case RealTimeSmm:
                    this.isRealTime = true;
                    this.smmReal = new SimpleMovingMedian(type.valueRange);
                    registerGaugeRealSmm(type, metricRegistry);
                    break;
                case RealTimeWma:
                    this.isRealTime = true;
                    this.wmaReal = new WeightedMovingAverage(type.valueWeights);
                    registerGaugeRealWma(type, metricRegistry);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Object start()
    {
        final long callCount = (this.callCounter != null ? this.callCounter.incrementAndGet() : 0L);
        this.loadAverage.countEvent();
        return new Context(callCount);
    }

    @Override
    public void stop(final Object contextObj)
    {
        if (contextObj instanceof Context)
        {
            final Context context = (Context) contextObj;
            context.stop();
            this.currentContext.set(context);
        }
    }

    @Override
    public String toString()
    {
        return this.getClass()
                   .getName() + "(" + this.baseName + ")";
    }

    private class Context
    {

        final Stopwatch basicStopwatch;

        final long      callCount;

        final Stopwatch cpuTimeStopwatch;

        final Stopwatch userTimeStopwatch;

        final Stopwatch wrappedBasicStopwatch;

        final Stopwatch wrappedCpuTimeStopwatch;

        final Stopwatch wrappedUserTimeStopwatch;




        Context(final long callCount)
        {
            this.callCount = callCount;

            this.basicStopwatch = (DefaultTimer.this.isRealTime ? new BasicStopwatch() : DefaultTimer.NULL_STOPWATCH);
            this.userTimeStopwatch = (DefaultTimer.this.isUserTime ? new UserTimeStopwatch() : DefaultTimer.NULL_STOPWATCH);
            this.cpuTimeStopwatch = (DefaultTimer.this.isCpuTime ? new CpuTimeStopwatch() : DefaultTimer.NULL_STOPWATCH);

            this.wrappedBasicStopwatch = (DefaultTimer.this.isRealTime ? new ShortLivedStopwatch(this.basicStopwatch) : DefaultTimer.NULL_STOPWATCH);
            this.wrappedUserTimeStopwatch = (DefaultTimer.this.isUserTime ? new ShortLivedStopwatch(this.userTimeStopwatch) : DefaultTimer.NULL_STOPWATCH);
            this.wrappedCpuTimeStopwatch = (DefaultTimer.this.isCpuTime ? new ShortLivedStopwatch(this.cpuTimeStopwatch) : DefaultTimer.NULL_STOPWATCH);

            start();
        }




        public void reset()
        {
            this.wrappedBasicStopwatch.reset();
            this.wrappedUserTimeStopwatch.reset();
            this.wrappedCpuTimeStopwatch.reset();
        }




        private void start()
        {
            this.wrappedBasicStopwatch.start();
            this.wrappedUserTimeStopwatch.start();
            this.wrappedCpuTimeStopwatch.start();
        }




        public void stop()
        {
            this.wrappedUserTimeStopwatch.stop();
            this.wrappedCpuTimeStopwatch.stop();
            this.wrappedBasicStopwatch.stop();
        }




        @Override
        public String toString()
        {
            return "elapsed-time: " + this.basicStopwatch.getDuration(TimeUnit.MILLISECONDS) + " ms; cpu-time: " + (this.userTimeStopwatch.getDuration(TimeUnit.MILLISECONDS) + this.cpuTimeStopwatch.getDuration(TimeUnit.MILLISECONDS));
        }
    }

}
