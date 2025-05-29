package com.github.mydeardoctor.doctordatasetbot.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class ShutdownHookPrinter extends ShutdownHook
{
    private final String message;
    private final Logger logger;

    public ShutdownHookPrinter(final String message)
    {
        super();
        incrementCountdownLatchInitialCount();

        this.message = message;
        logger = LoggerFactory.getLogger(ShutdownHookPrinter.class);
    }

    @Override
    public void run()
    {
        logger.error("Shutting down! {}", message);

        final CountDownLatch countdownLatch = getCountdownLatch();
        countdownLatch.countDown();
    }
}
