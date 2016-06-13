package com.ing.diba.metrics.average;




public class WeightedMovingAverage extends AMovingAverage implements Average
{

    private final int      countValues;

    private volatile int   currentValueCount = 0;

    private final int      sumWeights;

    private final double[] valueWeights;




    public WeightedMovingAverage(final double[] aValueWeights)
    {
        super(aValueWeights.length, false);
        this.countValues = aValueWeights.length;
        this.valueWeights = new double[this.countValues];

        int weights = 0;
        for (int i = 0; this.countValues > i; ++i)
        {
            weights += aValueWeights[i];
            this.valueWeights[i] = aValueWeights[i];
        }

        this.sumWeights = weights;
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
        for (int i = 0; this.valueWeights.length > i; ++i)
        {
            sum += (this.valueWeights[i] * this.buffer[i]);
        }

        return (sum / this.sumWeights);
    }

}
