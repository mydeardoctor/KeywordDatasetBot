package com.github.mydeardoctor.keyworddatasetbot.shutdown;

import java.util.concurrent.CountDownLatch;

public abstract class ShutdownHook implements Runnable
{
    private static int initialCount = 0;
    private static CountDownLatch countdownLatch = new CountDownLatch(0);

    //Default access modifier - package-private.
    ShutdownHook()
    {
        super();
    }

    //Default access modifier - package-private.
    synchronized void incrementCountdownLatchInitialCount()
    {
        ++initialCount;
        countdownLatch = new CountDownLatch(initialCount);
    }

    //Default access modifier - package-private.
    synchronized CountDownLatch getCountdownLatch()
    {
        return countdownLatch;
    }
}
