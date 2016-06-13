package com.ing.diba.metrics.frequency;




public interface Frequency
{

    public abstract void countEvent();




    public abstract void countEvent(int count);




    public abstract int valueAt(long atTimeMillis,
                                int countTimeFrames);




    public abstract int valueAtCurrent(int countTimeFrames);

}
