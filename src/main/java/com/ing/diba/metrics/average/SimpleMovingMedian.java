package com.ing.diba.metrics.average;




import java.util.Arrays;




/**
 * From a statistical point of view, the moving average, when used to estimate the underlying
 * trend in a time series, is susceptible to rare events such as rapid shocks or other anomalies.
 * A more robust estimate of the trend is the simple moving median over n time points:
 * 
 * <pre>
 * SMM = Median(pm, pm-1, ... , pm-n+1)
 * </pre>
 * 
 * where the median is found by, for example, sorting the values inside the brackets and
 * finding the value in the middle.
 *
 * @author Haase.Dirk
 */
public class SimpleMovingMedian extends AMovingAverage implements Average
{

    private final int      centerIndex;

    private volatile int   currentValueCount = 0;

    private final double[] sortedBuffer;




    public SimpleMovingMedian(final int countValues)
    {
        super(countValues, true);
        this.centerIndex = (countValues / 2);
        this.sortedBuffer = new double[countValues];
    }




    @Override
    public double calculate(final double newValue)
    {
        set(newValue);

        if (this.currentValueCount < this.sortedBuffer.length)
        {
            ++this.currentValueCount;
            return newValue;
        }

        for (int i = 0; this.sortedBuffer.length > i; ++i)
        {
            this.sortedBuffer[i] = this.buffer[i];
        }

        Arrays.sort(this.sortedBuffer);

        return this.sortedBuffer[this.centerIndex];
    }

}
