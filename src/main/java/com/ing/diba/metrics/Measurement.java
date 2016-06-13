package com.ing.diba.metrics;

import java.util.concurrent.Callable;

public interface Measurement {
    
    enum MetricType {
        CallCount("call.count"), 
        CPUTime("time.cpu"), 
        CPUTimePercent("time.cpu.percent"), 
        CPUTimeEwma03("time.cpu.ewma03"), 
        RealTime("time.real"), 
        RealTimeEwma03("time.real.ewma03"), 
        RealTimeEwma06("time.real.ewma06"), 
        RealTimeEwma08("time.real.ewma08"), 
        RealTimeSma("time.real.sma", 5), 
        RealTimeSmm("time.real.smm", 5), 
        RealTimeWma("time.real.wma",new double[]{11, 12, 13, 14, 15}), 
        LoadAvgFifteen("loadavg.fifteen"), 
        LoadAvgFive("loadavg.five"), 
        LoadAvgOne("loadavg.one"), 
        UserTime("time.user");
        
        public final String suffix;
        
        public final String columnName;

        public final int valueRange;
        
        public final double[] valueWeights;

        
        MetricType(final String suffix) {
            this(suffix, 1);
        }
        
        MetricType(final String suffix, final int valueRange) {
            this.columnName = suffix.replace('.', '_');
            this.suffix = "." + suffix;
            this.valueRange = valueRange;
            this.valueWeights = new double[]{1};
        }
        
        MetricType(final String suffix, final double[] valueWeights) {
            this.columnName = suffix.replace('.', '_');
            this.suffix = suffix;
            this.valueRange = 1;
            this.valueWeights = valueWeights;
        }
    }
    
    public boolean init(String name, String[] names);
    
    public boolean init(final MetricType[] metricTypes, final String name, final String[] names);
    
    public <T> void registerGauge(final String suffix, final Callable<T> gaugeValue);
    
    public Object start();
    
    public void stop(final Object timerContext);
    
}
