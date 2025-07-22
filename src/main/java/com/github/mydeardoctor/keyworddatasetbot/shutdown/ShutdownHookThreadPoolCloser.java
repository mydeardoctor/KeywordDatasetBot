package com.github.mydeardoctor.keyworddatasetbot.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownHookThreadPoolCloser extends ShutdownHook
{
    private final ExecutorService threadPool;
    private static final long TIMEOUT_MINUTES = 1;
    private final Logger logger;

    public ShutdownHookThreadPoolCloser(final ExecutorService threadPool)
    {
        super();
        incrementCountdownLatchInitialCount();

        this.threadPool = threadPool;
        logger = LoggerFactory.getLogger(ShutdownHookThreadPoolCloser.class);
    }

    @Override
    public void run()
    {
        final String threadPoolAsString = threadPool.toString();

        threadPool.shutdown();

        boolean isTerminated = false;
        while(isTerminated == false)
        {
            try
            {
                logger.debug(
                    "Shutting down: Trying to shut down thread pool {}.",
                    threadPoolAsString);
                isTerminated = threadPool.awaitTermination(
                    TIMEOUT_MINUTES,
                    TimeUnit.MINUTES);

                if(isTerminated)
                {
                    logger.debug(
                        "Shutting down: Successfully shut down thread pool {}.",
                        threadPoolAsString);
                }
                else
                {
                    logger.error(
                        "Shutting down: Could not shut down thread pool {}!",
                        threadPoolAsString);
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