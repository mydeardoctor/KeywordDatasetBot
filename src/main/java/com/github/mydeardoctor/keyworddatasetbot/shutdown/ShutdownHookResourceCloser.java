package com.github.mydeardoctor.keyworddatasetbot.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class ShutdownHookResourceCloser extends ShutdownHook
{
    private final AutoCloseable resource;
    private final Logger logger;

    public ShutdownHookResourceCloser(final AutoCloseable resource)
    {
        super();
        incrementCountdownLatchInitialCount();

        this.resource = resource;
        logger = LoggerFactory.getLogger(ShutdownHookResourceCloser.class);
    }

    @Override
    public void run()
    {
        final String resourceAsString = resource.toString();

        try
        {
            resource.close();
            logger.debug(
                "Shutting down: Successfully closed resource {}.",
                resourceAsString);
        }
        catch(final Exception e)
        {
            logger.error(
                "Shutting down: Could not close resource {}!",
                resourceAsString,
                e);
        }
        finally
        {
            final CountDownLatch countdownLatch = getCountdownLatch();
            countdownLatch.countDown();
        }
    }
}