package com.github.mydeardoctor.doctordatasetbot.shutdown;

import ch.qos.logback.core.hook.DefaultShutdownHook;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ShutdownHookLogback extends DefaultShutdownHook
{
    private static final long MAX_DELAY_MINUTES = 5;
    private final CountDownLatch countdownLatch;
    private final Logger logger;

    public ShutdownHookLogback()
    {
        super();

        final ShutdownHookCountdownLatch shutdownHookCountdownLatch =
            ShutdownHookCountdownLatch.getInstance();
//        shutdownHookCountdownLatch.incrementInitialCount();
        this.countdownLatch = shutdownHookCountdownLatch.getCountdownLatch();
//        this.countdownLatch = ShutdownHookCountdownLatch.countdownLatch;
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
                final boolean result = countdownLatch.await(MAX_DELAY_MINUTES, TimeUnit.MINUTES);
                tried = true;

                if(result)
                {
                    logger.error(
                        "Shutting down! " +
                        "Successfully waited fot shutdown hooks to complete.");
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

        //TODO does it flush?
        super.run();
    }
}
