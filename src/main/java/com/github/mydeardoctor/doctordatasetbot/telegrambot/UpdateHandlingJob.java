package com.github.mydeardoctor.doctordatasetbot.telegrambot;

import com.sun.jdi.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public class UpdateHandlingJob implements Runnable
{
    // Set of users that are currently being processed in the thread pool.
    private final SetOfUsersBeingProcessed setOfUsersBeingProcessed;

    private final Update update;

    private final Logger logger =
        LoggerFactory.getLogger(UpdateHandlingJob.class);

    public UpdateHandlingJob(
        final SetOfUsersBeingProcessed setOfUsersBeingProcessed,
        final Update update)
    {
        this.setOfUsersBeingProcessed = setOfUsersBeingProcessed;
        this.update = update;
    }

    @Override
    public void run()
    {
        if(logger.isDebugEnabled())
        {
            final Thread currentThread = Thread.currentThread();
            final String debugMessage = String.format(
                "Thread: group = %s, name = %s, priority = %d.",
                currentThread.getThreadGroup().getName(),
                currentThread.getName(),
                currentThread.getPriority());
            logger.debug(debugMessage);
        }

        if(update.hasMessage())
        {
            final Message message = update.getMessage();
            final User user = message.getFrom();
            if(user != null)
            {
                final Long userId = user.getId();

                //TODO handle update
//                try
//                {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e)
//                {
//                    throw new RuntimeException(e);
//                }

                // Update is handled. User is processed.
                // Remove the user from
                // the set of users that are currently being processed
                // in the thread pool.
                final boolean result = setOfUsersBeingProcessed.remove(userId);
                if(result != true)
                {
                    final String errorMessage = String.format(
                        "Thread pool userId logic is broken! " +
                        "Someone already handled user %d!",
                        userId);
                    final InternalException e =
                        new InternalException(errorMessage);
                    if(logger.isErrorEnabled())
                    {
                        logger.error(errorMessage, e);
                    }
                    throw e;
                }
            }
        }
    }
}
