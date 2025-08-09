package com.github.mydeardoctor.keyworddatasetbot.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownHookExecutorServiceCloser extends ShutdownHook
{
    private final ExecutorService executorService;
    private static final long TIMEOUT_MINUTES = 1;
    private final Logger logger;

    public ShutdownHookExecutorServiceCloser(
        final ExecutorService executorService)
    {
        super();
        incrementCountdownLatchInitialCount();

        this.executorService = executorService;
        logger = LoggerFactory.getLogger(
            ShutdownHookExecutorServiceCloser.class);
    }

    @Override
    public void run()
    {
        final String executorServiceAsString = executorService.toString();

        executorService.shutdown();

        boolean isTerminated = false;
        while(isTerminated == false)
        {
            try
            {
                logger.debug(
                    "Shutting down: Trying to shut down executor service {}.",
                    executorServiceAsString);
                isTerminated = executorService.awaitTermination(
                    TIMEOUT_MINUTES,
                    TimeUnit.MINUTES);

                if(isTerminated)
                {
                    logger.debug(
                        "Shutting down: " +
                        "Successfully shut down executor service {}.",
                        executorServiceAsString);
                }
                else
                {
                    logger.error(
                        "Shutting down: " +
                        "Could not shut down executor service {}!",
                        executorServiceAsString);
                }
            }
            catch(final InterruptedException e)
            {
                logger.error("Shutting down: Thread was interrupted!", e);
            }
        }

        final CountDownLatch countdownLatch = getCountdownLatch();
        countdownLatch.countDown();
    }
}