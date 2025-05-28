package com.github.mydeardoctor.doctordatasetbot.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class ShutdownHookPrinter implements Runnable
{
    private final String message;
    private final CountDownLatch countdownLatch;
    private final Logger logger;

    public ShutdownHookPrinter(
        final String message,
        final CountDownLatch countdownLatch)
    {
        super();
        this.message = message;
        this.countdownLatch = countdownLatch;
        logger = LoggerFactory.getLogger(ShutdownHookPrinter.class);
    }

    @Override
    public void run()
    {
        logger.error("Shutting down! {}", message);
        countdownLatch.countDown();
    }
}
