package com.ing.diba.metrics.frequency;




import java.util.concurrent.locks.ReentrantLock;




/**
 * Created by dhaa on 20.12.15.
 */
public class ArrayBucketBasedFrequency implements Frequency
{

    class Bucket
    {

        volatile long bucketStartTimeMillis;

        volatile int  countEvents;
    }

    class ResourceLock extends ReentrantLock implements AutoCloseable
    {

        private static final long serialVersionUID = 1L;

        volatile boolean          isInterrupted    = false;




        ResourceLock()
        {
            super(true);
        }




        @Override
        public void close()
        {
            unlock();
        }




        @Override
        public void lockInterruptibly()
        {
            try
            {
                super.lockInterruptibly();
                this.isInterrupted = false;
            }
            catch (final InterruptedException e)
            {
                Thread.currentThread()
                      .interrupt();
                this.isInterrupted = true;
            }
        }

    }

    private static final long  MIN_DURATION_MILLIS = 2000L;

    private static final int   MIN_TIME_FRAMES     = 1;

    private static final long  ONE_MINUTE_MILLIS   = 1000L * 60L;

    private final int          bucketCount;

    private final int          bucketsPerTimeFrame;

    private final Bucket[]     currentBucketBuffer;

    private volatile int       currentIndex        = 0;

    private final int          maxTimeFramesToObserve;

    private final ResourceLock resourceLock;

    private final long         singleBucketDuration;

    private final long         singleTimeFrameDuration;




    public ArrayBucketBasedFrequency()
    {
        this(ArrayBucketBasedFrequency.ONE_MINUTE_MILLIS, 15, 4, 10);
    }




    public ArrayBucketBasedFrequency(final long timeFrameDuration,
                                     final int timeFramesToObserve,
                                     final int timesBuckets,
                                     final int bucketCountPerTimeFrame)
    {
        if (timeFrameDuration < ArrayBucketBasedFrequency.MIN_DURATION_MILLIS)
        {
            throw new IllegalArgumentException("Parameter timeFrameDuration with " + timeFrameDuration + " can not be less than " + ArrayBucketBasedFrequency.MIN_DURATION_MILLIS);
        }
        if (timeFramesToObserve < ArrayBucketBasedFrequency.MIN_TIME_FRAMES)
        {
            throw new IllegalArgumentException("Parameter timeFramesToObserve with " + timeFramesToObserve + " can not be less than " + ArrayBucketBasedFrequency.MIN_TIME_FRAMES);
        }
        if (timesBuckets < 1)
        {
            throw new IllegalArgumentException("Parameter timesBuckets with " + timesBuckets + " can not be less than 1");
        }

        this.resourceLock = new ResourceLock();

        this.singleTimeFrameDuration = timeFrameDuration + (bucketCountPerTimeFrame - (timeFrameDuration % bucketCountPerTimeFrame));
        this.maxTimeFramesToObserve = timeFramesToObserve;

        final long minTimeFrameDuration = this.singleTimeFrameDuration / bucketCountPerTimeFrame;
        this.bucketCount = (this.maxTimeFramesToObserve * timesBuckets * bucketCountPerTimeFrame) + bucketCountPerTimeFrame;

        this.bucketsPerTimeFrame = (int) (this.singleTimeFrameDuration / minTimeFrameDuration);
        this.singleBucketDuration = (this.singleTimeFrameDuration / this.bucketsPerTimeFrame);

        this.currentBucketBuffer = buildNewArray(this.bucketCount);
    }




    private Bucket[] buildNewArray(final int arraySize)
    {
        final Bucket[] buckets = new Bucket[arraySize];

        buckets[0] = new Bucket();
        buckets[0].bucketStartTimeMillis = System.currentTimeMillis();
        for (int i = 1; buckets.length > i; ++i)
        {
            buckets[i] = new Bucket();
            final Bucket previousBucket = buckets[i - 1];
            buckets[i].bucketStartTimeMillis = previousBucket.bucketStartTimeMillis + this.singleBucketDuration;
        }

        return buckets;
    }




    @Override
    public void countEvent()
    {
        countEvent(1);
    }




    @Override
    public void countEvent(final int count)
    {
        try (
             final ResourceLock lock = lock())
        {
            if (!lock.isInterrupted)
            {
                final long currentTimeMillis = System.currentTimeMillis();

                final int currentIndex1 = this.currentIndex;

                final int currentIndex2 = (currentIndex1 > 0 ? currentIndex1 - 1 : 0);
                final int currentIndex3 = findIndex(currentTimeMillis, currentIndex2);
                final int currentIndex4 = (currentIndex3 >= 0 ? currentIndex3 : reorgArray(currentTimeMillis));

                this.currentIndex = currentIndex4;

                this.currentBucketBuffer[currentIndex4].countEvents += count;
            }
        }
    }




    private int findIndex(final long atTimeMillis,
                          final int startIndex)
    {
        int foundIndex = -1;

        for (int i = startIndex; this.bucketCount > i; ++i)
        {
            final long currentBucketStartTimeMillis = this.currentBucketBuffer[i].bucketStartTimeMillis;
            final long nextBucketStartTimeMillis = currentBucketStartTimeMillis + this.singleBucketDuration;

            final boolean isCurrentOrLater = currentBucketStartTimeMillis <= atTimeMillis;
            final boolean isCurrentOrEarlier = nextBucketStartTimeMillis > atTimeMillis;
            final boolean isInCurrentBucket = isCurrentOrLater && isCurrentOrEarlier;

            if (isInCurrentBucket)
            {
                foundIndex = i;
                break;
            }
        }

        return foundIndex;
    }




    public int getBucketCount()
    {
        return this.bucketCount;
    }




    public int getBucketsPerTimeFrame()
    {
        return this.bucketsPerTimeFrame;
    }




    public long getCurrentLastStartMillis()
    {
        return this.currentBucketBuffer[this.bucketCount - 1].bucketStartTimeMillis;
    }




    public long getSingleBucketDuration()
    {
        return this.singleBucketDuration;
    }




    public long getSingleTimeFrameDuration()
    {
        return this.singleTimeFrameDuration;
    }




    private ResourceLock lock()
    {
        this.resourceLock.lockInterruptibly();
        return this.resourceLock;
    }




    private int reorgArray(final long atTimeMillis)
    {
        final int foundIndex = findIndex(atTimeMillis, 0);
        if (foundIndex < 0)
        {
            final int indexAfterReorgArray = (this.maxTimeFramesToObserve * this.bucketsPerTimeFrame) - 1;
            final int firstIndexForReorgArray = this.bucketCount - (this.maxTimeFramesToObserve * this.bucketsPerTimeFrame);

            for (int i = 0; this.bucketCount > i; ++i)
            {
                final int copyFromIndex = i + firstIndexForReorgArray;
                if (copyFromIndex < this.bucketCount)
                {
                    final Bucket copyFromBucket = this.currentBucketBuffer[copyFromIndex];
                    this.currentBucketBuffer[i].bucketStartTimeMillis = copyFromBucket.bucketStartTimeMillis;
                    this.currentBucketBuffer[i].countEvents = copyFromBucket.countEvents;
                }
                else
                {
                    final Bucket previousBucket = this.currentBucketBuffer[i - 1];
                    this.currentBucketBuffer[i].bucketStartTimeMillis = previousBucket.bucketStartTimeMillis + this.singleBucketDuration;
                    this.currentBucketBuffer[i].countEvents = 0;
                }
            }

            return indexAfterReorgArray;
        }
        return foundIndex;
    }




    @Override
    public int valueAt(final long atTimeMillis,
                       final int countTimeFrames)
    {
        try (
             final ResourceLock lock = lock())
        {
            if (!lock.isInterrupted)
            {
                if (countTimeFrames > 0)
                {
                    final long firstBucketStartTimeMillis = this.currentBucketBuffer[0].bucketStartTimeMillis;
                    final boolean isEndOnBeforeFirstBucket = firstBucketStartTimeMillis > atTimeMillis;

                    if (!isEndOnBeforeFirstBucket)
                    {
                        final long firstTimeMillis = (atTimeMillis - (this.bucketsPerTimeFrame * this.singleBucketDuration * countTimeFrames));
                        final boolean isOnFirstBucketOrLater = firstBucketStartTimeMillis <= firstTimeMillis;

                        final int firstIndex = (isOnFirstBucketOrLater ? findIndex(firstTimeMillis, 0) : 0);

                        if (firstIndex >= 0)
                        {
                            int lastIndex = firstIndex + (countTimeFrames * this.bucketsPerTimeFrame);
                            lastIndex = (lastIndex > this.bucketCount ? this.bucketCount : lastIndex);

                            int countSum = 0;

                            for (int i = firstIndex; lastIndex > i; ++i)
                            {
                                countSum += this.currentBucketBuffer[i].countEvents;
                            }

                            return countSum;
                        }
                    }
                }
            }
            return 0;
        }
    }




    @Override
    public int valueAtCurrent(final int countTimeFrames)
    {
        return valueAt(System.currentTimeMillis(), countTimeFrames);
    }

}
