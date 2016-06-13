package com.ing.diba.metrics.average;




abstract class AMovingAverage
{

    /**
     * The internal buffer array.
     */
    protected final double[] buffer;

    private final boolean    isCircular;

    /**
     * Index to write next value.
     */
    protected volatile int   writeIndex = -1;




    protected AMovingAverage(final int size,
                             final boolean isCircular)
    {

        this.buffer = new double[size];
        this.isCircular = isCircular;
    }




    protected void set(final double value)
    {
        if (this.isCircular)
        {
            this.writeIndex = (this.writeIndex + 1) % this.buffer.length;
            // update circular write index
        }
        else
        {
            if (this.buffer.length > (this.writeIndex + 1))
            {
                ++this.writeIndex;
            }
            else
            {
                for (int i = 1; this.buffer.length > i; ++i)
                {
                    this.buffer[i - 1] = this.buffer[i];
                }
            }
        }
        this.buffer[this.writeIndex] = value; // set new buffer value
    }

}
