package com.github.mydeardoctor.doctordatasetbot.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ShutdownHookPrinter implements Runnable
{
    private final String message;
    private final Logger logger;

    public ShutdownHookPrinter(final String message)
    {
        super();
        this.message = message;
        logger = LoggerFactory.getLogger(ShutdownHookPrinter.class);
    }

    @Override
    public void run()
    {
        logger.error("Shutting down! {}", message);
    }
}
