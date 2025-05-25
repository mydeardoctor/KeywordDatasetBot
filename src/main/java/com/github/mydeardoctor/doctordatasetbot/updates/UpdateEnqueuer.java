package com.github.mydeardoctor.doctordatasetbot.updates;

import com.github.mydeardoctor.doctordatasetbot.delay.DelayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class UpdateEnqueuer implements LongPollingUpdateConsumer
{
    private final CommonResourcesManager commonResourcesManager;
    private static final long DELAY_MS = 10;

    private final Logger logger;

    public UpdateEnqueuer(final CommonResourcesManager commonResourcesManager)
    {
        super();

        this.commonResourcesManager = commonResourcesManager;

        logger = LoggerFactory.getLogger(UpdateEnqueuer.class);
    }

    @Override
    public void consume(List<Update> list)
    {
        logger.debug(
            "Thread: group = {}, name = {}, priority = {}.",
            Thread.currentThread().getThreadGroup().getName(),
            Thread.currentThread().getName(),
            Thread.currentThread().getPriority());

        for(final Update update : list)
        {
            //Check input.
            if((update == null) || (!update.hasMessage()))
            {
                continue;
            }

            commonResourcesManager.enqueueUpdate(update);
            //Give time for other threads to use common resources.
            DelayManager.delay(DELAY_MS);
        }
    }
}