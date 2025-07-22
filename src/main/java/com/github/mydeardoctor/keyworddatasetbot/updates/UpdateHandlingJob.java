package com.github.mydeardoctor.keyworddatasetbot.updates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateHandlingJob implements Runnable
{
    private final Update update;
    private final CommonResourcesManager commonResourcesManager;

    private final Logger logger;

    public UpdateHandlingJob(
        final Update update,
        final CommonResourcesManager commonResourcesManager)
    {
        super();

        this.update = update;
        this.commonResourcesManager = commonResourcesManager;

        logger = LoggerFactory.getLogger(UpdateHandlingJob.class);
    }

    @Override
    public void run()
    {
        Long userId = null;

        try
        {
            logger.debug(
                "Thread: group = {}, name = {}, priority = {}.",
                Thread.currentThread().getThreadGroup().getName(),
                Thread.currentThread().getName(),
                Thread.currentThread().getPriority());

            //Handle update.
            if((update != null) && (update.hasMessage()))
            {
                userId = update.getMessage().getFrom().getId();
                System.out.println(update.getMessage().getText());
            }
        }
        catch(final Exception e)
        {
            logger.error("Exception in thread pool!", e);
        }
        finally
        {
            commonResourcesManager.finishHandlingUpdate(userId);
        }
    }
}
