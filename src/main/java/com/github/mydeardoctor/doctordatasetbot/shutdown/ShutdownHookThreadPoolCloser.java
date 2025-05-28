package com.github.mydeardoctor.doctordatasetbot.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownHookThreadPoolCloser implements Runnable
{
    private final ExecutorService threadPool;
    private static final long TIMEOUT_MINUTES = 1;
    private final CountDownLatch countdownLatch;
    private final Logger logger;

    public ShutdownHookThreadPoolCloser(
        final ExecutorService threadPool,
        final CountDownLatch countdownLatch)
    {
        super();
        this.threadPool = threadPool;
        this.countdownLatch = countdownLatch;
        logger = LoggerFactory.getLogger(ShutdownHookThreadPoolCloser.class);
    }

    @Override
    public void run()
    {
        final String threadPoolAsString = threadPool.toString();
        final String errorMessage = new StringBuilder()
            .append("Shutting down! Could not shut down thread pool ")
            .append(threadPoolAsString)
            .append("!")
            .toString();

        threadPool.shutdown();

        boolean isTerminated = false;
        while(isTerminated == false)
        {
            try
            {
                logger.error(
                    "Shutting down! Trying to shut down thread pool {}.",
                    threadPoolAsString);
                isTerminated = threadPool.awaitTermination(
                    TIMEOUT_MINUTES,
                    TimeUnit.MINUTES);

                if(isTerminated)
                {
                    logger.error(
                        "Shutting down! Successfully shut down thread pool {}.",
                        threadPoolAsString);
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

        countdownLatch.countDown();
    }
}