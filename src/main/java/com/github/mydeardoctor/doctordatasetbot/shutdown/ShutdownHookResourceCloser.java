package com.github.mydeardoctor.doctordatasetbot.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class ShutdownHookResourceCloser implements Runnable
{
    private final AutoCloseable resource;
    private final CountDownLatch countdownLatch;
    private final Logger logger;

    public ShutdownHookResourceCloser(final AutoCloseable resource)
    {
        super();
        this.resource = resource;
//        this.countdownLatch = countdownLatch;
        final ShutdownHookCountdownLatch shutdownHookCountdownLatch =
            ShutdownHookCountdownLatch.getInstance();
        shutdownHookCountdownLatch.incrementInitialCount();
        this.countdownLatch = shutdownHookCountdownLatch.getCountdownLatch();
        logger = LoggerFactory.getLogger(ShutdownHookResourceCloser.class);
    }

    @Override
    public void run()
    {
        final String resourceAsString = resource.toString();

        try
        {
            resource.close();
            logger.error(
                "Shutting down! Successfully closed resource {}.",
                resourceAsString);
        }
        catch(final Exception e)
        {
            logger.error(
                "Shutting down! Could not close resource {}!",
                resourceAsString,
                e);
        }
        finally
        {
            countdownLatch.countDown();
        }
    }
}