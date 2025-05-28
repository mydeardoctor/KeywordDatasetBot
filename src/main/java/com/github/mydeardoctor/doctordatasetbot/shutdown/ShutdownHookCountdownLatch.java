package com.github.mydeardoctor.doctordatasetbot.shutdown;

import java.util.concurrent.CountDownLatch;

public class ShutdownHookCountdownLatch
{
    private static final int NUMBER_OF_SHUTDOWN_HOOK_PRINTERS = 2;
    private static final int NUMBER_OF_SHUTDOWN_HOOK_RESOURCE_CLOSERS = 1;
    private static final int NUMBER_OF_SHUTDOWN_HOOK_THREAD_POOL_CLOSERS = 1;
    private static final int NUMBER_OF_SHUTDOWN_HOOKS =
        NUMBER_OF_SHUTDOWN_HOOK_PRINTERS +
        NUMBER_OF_SHUTDOWN_HOOK_RESOURCE_CLOSERS +
        NUMBER_OF_SHUTDOWN_HOOK_THREAD_POOL_CLOSERS;
    /* This countdown latch is basically a global variable.
       This is done deliberately
       to avoid explicitly passing it
       to every class that eventually creates a new shutdown hook. */
    public static final CountDownLatch countdownLatch =
        new CountDownLatch(NUMBER_OF_SHUTDOWN_HOOKS);

    private ShutdownHookCountdownLatch()
    {

    }
}