package com.ing.diba.metrics.frequency;




public class NullFrequency implements Frequency
{

    @Override
    public void countEvent()
    {
    }




    @Override
    public void countEvent(final int count)
    {
    }




    @Override
    public int valueAt(final long atTimeMillis,
                       final int countTimeFrames)
    {
        return 0;
    }




    @Override
    public int valueAtCurrent(final int countTimeFrames)
    {
        return 0;
    }

}
