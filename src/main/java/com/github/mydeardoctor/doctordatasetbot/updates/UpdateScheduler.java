package com.github.mydeardoctor.doctordatasetbot.updates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateScheduler implements Runnable
{
    private final CommonResourcesManager commonResourcesManager;
    private final Logger logger;

    public UpdateScheduler(final CommonResourcesManager commonResourcesManager)
    {
        super();

        this.commonResourcesManager = commonResourcesManager;
        this.logger = LoggerFactory.getLogger(UpdateScheduler.class);
    }

    @Override
    public void run()
    {
        while(true)
        {
            logger.debug(
                "Thread: group = {}, name = {}, priority = {}.",
                Thread.currentThread().getThreadGroup().getName(),
                Thread.currentThread().getName(),
                Thread.currentThread().getPriority());

            commonResourcesManager.scheduleUpdate();
        }
    }
}