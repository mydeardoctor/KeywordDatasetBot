package com.github.mydeardoctor.doctordatasetbot.shutdown;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.hook.DefaultShutdownHook;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//TODO зарегать
public class ShutdownHookLogback extends ShutdownHook
{
    private static final long MAX_DELAY_MINUTES = 5;
    private final Logger logger;

    public ShutdownHookLogback()
    {
        super();

        logger = LoggerFactory.getLogger(ShutdownHookLogback.class);
    }

    @Override
    public void run()
    {
        final String errorMessage =
            "Shutting down! Could not wait for shutdown hooks to complete!";

        boolean tried = false;
        while(tried == false)
        {
            try
            {
                final CountDownLatch countdownLatch = getCountdownLatch();
                final boolean result =
                    countdownLatch.await(MAX_DELAY_MINUTES, TimeUnit.MINUTES);
                tried = true;

                if(result)
                {
                    logger.error(
                        "Shutting down! " +
                        "Successfully waited for shutdown hooks to complete.");
                }
                else
                {
                    logger.error(errorMessage);
                }
            }
            catch(final InterruptedException e)
            {
                logger.error(errorMessage, e);
            }
        }

        logger.error("Shutting down! Shutting down Logback.");
        final LoggerContext loggerContext =
            (LoggerContext)LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }
}
