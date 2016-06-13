package com.ing.diba.metrics.average;




/**
 * @author Haase.Dirk
 */
public class SimpleMovingAverage extends AMovingAverage implements Average
{

    private final int    countValues;

    private volatile int currentValueCount = 0;




    public SimpleMovingAverage(final int countValues)
    {
        super(countValues, true);
        this.countValues = countValues;
    }




    @Override
    public double calculate(final double newValue)
    {
        set(newValue);

        if (this.currentValueCount < this.countValues)
        {
            ++this.currentValueCount;
            return newValue;
        }

        double sum = 0;
        for (final double value : this.buffer)
        {
            sum += value;
        }

        return (sum / this.countValues);
    }

}
