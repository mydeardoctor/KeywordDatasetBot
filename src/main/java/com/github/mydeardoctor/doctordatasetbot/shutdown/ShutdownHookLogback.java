package com.github.mydeardoctor.doctordatasetbot.shutdown;

import ch.qos.logback.classic.LoggerContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ShutdownHookLogback extends ShutdownHook
{
    private static final long TIMEOUT_MINUTES = 2;
    private final Logger logger;

    public ShutdownHookLogback()
    {
        super();

        logger = LoggerFactory.getLogger(ShutdownHookLogback.class);
    }

    @Override
    public void run()
    {
        boolean tried = false;
        while(tried == false)
        {
            try
            {
                logger.debug(
                    "Shutting down: Waiting for shutdown hooks to complete.");

                final CountDownLatch countdownLatch = getCountdownLatch();
                final boolean result =
                    countdownLatch.await(TIMEOUT_MINUTES, TimeUnit.MINUTES);
                tried = true;

                if(result)
                {
                    logger.debug("Shutting down: Shutdown hooks completed.");
                }
                else
                {
                    logger.error(
                        "Shutting down: Shutdown hooks could not complete!");
                }
            }
            catch(final InterruptedException e)
            {
                logger.error("Shutting down: Thread was interrupted!", e);
            }
        }

        logger.debug("Shutting down: Shutting down Logback.");
        final LoggerContext loggerContext =
            (LoggerContext)LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }
}
