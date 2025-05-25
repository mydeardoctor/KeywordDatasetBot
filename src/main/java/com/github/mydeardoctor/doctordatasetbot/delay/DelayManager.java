package com.github.mydeardoctor.doctordatasetbot.delay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayManager
{
    private static final Logger logger =
        LoggerFactory.getLogger(DelayManager.class);

    public static void delay(final long milliseconds)
    {
        boolean result = false;
        while(result == false)
        {
            try
            {
                Thread.sleep(milliseconds);
                result = true;
            }
            catch(final InterruptedException e)
            {
                logger.error("Thread was interrupted!", e);
            }
        }
    }
}