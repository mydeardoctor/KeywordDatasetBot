package com.github.mydeardoctor.doctordatasetbot.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private final Logger logger;

    public UncaughtExceptionHandler()
    {
        super();
        logger = LoggerFactory.getLogger(UncaughtExceptionHandler.class);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable)
    {
        logger.error(
            "Uncaught exception in Thread: group = {}, name = {}, priority = {}.",
            thread.getThreadGroup().getName(),
            thread.getName(),
            thread.getPriority(),
            throwable);

        final Throwable[] suppressedExceptions = throwable.getSuppressed();
        for(final Throwable suppressedException : suppressedExceptions)
        {
            logger.error(
                "Accompanied by suppressed exception:",
                suppressedException);
        }
    }
}