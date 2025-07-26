package com.github.mydeardoctor.keyworddatasetbot.multithreadingupdates;

import com.github.mydeardoctor.keyworddatasetbot.application.ApplicationManager;
import com.github.mydeardoctor.keyworddatasetbot.application.UpdateUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateHandlingJob implements Runnable
{
    private final Update update;
    private final CommonResourcesManager commonResourcesManager;
    private final ApplicationManager applicationManager;

    private final Logger logger;

    public UpdateHandlingJob(
        final Update update,
        final CommonResourcesManager commonResourcesManager,
        final ApplicationManager applicationManager)
    {
        super();

        this.update = update;
        this.commonResourcesManager = commonResourcesManager;
        this.applicationManager = applicationManager;

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
            userId = UpdateUtilities.getUser(update).getId();
            applicationManager.handleUpdate(update);
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
