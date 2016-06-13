package com.ing.diba.metrics.average;




/**
 * The Exponentially Weighted Moving Average (EWMA) is a statistic for
 * monitoring the value that averages the data in a way that gives less and
 * less weight to data as they are further removed in time.
 */
public class ExponentialWeightedMovingAverage implements Average
{

    private final double    alpha;

    private volatile double oldValue = Double.NaN;




    /**
     * By the choice of weighting factor, alpha, the EWMA control procedure can
     * be made sensitive to a small or gradual drift in the process, whereas the
     * Shewhart control procedure can only react when the last data point is
     * outside a control limit.
     * <p>
     * The statistic that is calculated is: <code>EWMAt = alpha Yt + ( 1- alpha) EWMAt-1    for t = 1, 2, ..., n.</code> where
     * <ul>
     * <li>EWMA<sub>0</sub> is the mean of historical data (target)</li>
     * <li>Y<sub>t</sub> is the observation at time <i>t</i></li>
     * <li><i>n</i> is the number of observations to be monitored including EWMA<sub>0</sub></li>
     * <li>0 &lt; alpha <= 1 is a constant that determines the depth of memory of the EWMA.</li>
     * </ul>
     * </p>
     * <p>
     * The parameter alpha determines the rate at which 'older' data enter into the calculation of the EWMA statistic. A value of alpha = 1 implies that only the most recent measurement influences the EWMA (degrades to Shewhart chart). Thus, a large value of alpha = 1 gives more weight to recent
     * data and less weight to older data; a small value of alpha gives more weight to older data. The value of alpha is usually set between 0.2 and 0.3 (Hunter) although this choice is somewhat arbitrary. Lucas and Saccucci (1990) give tables that help the user select alpha.
     * </p>
     * 
     * @param alpha
     */
    public ExponentialWeightedMovingAverage(final double alpha)
    {
        this.alpha = alpha;
    }




    /* (non-Javadoc)
     * @see com.apollo.optik.metrics.average.Average#average(double)
     */
    @Override
    public double calculate(final double value)
    {
        if (Double.isNaN(this.oldValue))
        {
            this.oldValue = value;
            return value;
        }
        final double newValue = this.oldValue + (this.alpha * (value - this.oldValue));
        this.oldValue = newValue;
        return newValue;
    }
}
