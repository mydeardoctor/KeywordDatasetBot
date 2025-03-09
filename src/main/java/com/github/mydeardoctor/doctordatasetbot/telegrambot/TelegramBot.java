package com.github.mydeardoctor.doctordatasetbot.telegrambot;

import com.sun.jdi.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class TelegramBot implements LongPollingUpdateConsumer
{
    // Thread pool to handle incoming updates.
    private static final int MAX_UPDATES = 100;
    private static final int QUEUE_SIZE = MAX_UPDATES * 2;
    private static final int THREAD_POOL_SIZE =
        Runtime.getRuntime().availableProcessors() + 1;
    private final ExecutorService threadPool;

    // Set of users that are currently being processed in the thread pool.
    private final SetOfUsersBeingProcessed setOfUsersBeingProcessed
        = new SetOfUsersBeingProcessed();

    private static final int SLEEP_TIME_MS = 10;

    private final Logger logger = LoggerFactory.getLogger(TelegramBot.class);


    public TelegramBot()
    {
        // Queue for update handling jobs.
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
            QUEUE_SIZE,
            true);

        // Thread pool to handle incoming updates.
        this.threadPool = new ThreadPoolExecutor(
            THREAD_POOL_SIZE,
            THREAD_POOL_SIZE,
            0,
            TimeUnit.MILLISECONDS,
            queue);
    }

    @Override
    public void consume(List<Update> list)
    {
//        if(logger.isDebugEnabled())
//        {
//            final Thread currentThread = Thread.currentThread();
//            final String debugMessage = String.format(
//                "Thread: group = %s, name = %s, priority = %d.",
//                currentThread.getThreadGroup().getName(),
//                currentThread.getName(),
//                currentThread.getPriority());
//            logger.debug(debugMessage);
//        }

        while(!list.isEmpty())
        {
            final List<Update> preprocessedUpdates = new LinkedList<Update>();
            preprocessedUpdates.clear();

            // Get a snapshot of
            // set of users that are currently being processed
            // in the thread pool.
            final Set<Long> snapshotOfUsersBeingProcessed =
                setOfUsersBeingProcessed.getSnapshot();

            // Iterate over each update in the right order.
            final int numberOfUpdates = list.size();
            for(int i = 0; i < numberOfUpdates; ++i)
            {
                final Update update = list.get(i);
                if(update.hasMessage())
                {
                    final Message message = update.getMessage();
                    final User user = message.getFrom();
                    if(user != null)
                    {
                        final Long userId = user.getId();
                        if(!snapshotOfUsersBeingProcessed.contains(userId))
                        {
                            // Add the user to the set of users
                            // that are currently being processed
                            // in the thread pool.
                            final boolean resultAddToOriginalSet =
                                setOfUsersBeingProcessed.add(userId);
                            final boolean resultAddToSnapshotSet =
                                snapshotOfUsersBeingProcessed.add(userId);
                            if((resultAddToOriginalSet != true) ||
                               (resultAddToSnapshotSet != true))
                            {
                                final String errorMessage = String.format(
                                    "Thread pool userId logic is broken! " +
                                    "Someone already added user %d!",
                                    userId);
                                final InternalException e =
                                    new InternalException(errorMessage);
                                if(logger.isErrorEnabled())
                                {
                                    logger.error(errorMessage, e);
                                }
                                throw e;
                            }

                            // Add update handling job to the thread pool.
                            final UpdateHandlingJob updateHandlingJob =
                                new UpdateHandlingJob(
                                    setOfUsersBeingProcessed,
                                    update);
                            boolean jobAddedToThreadPool = false;
                            while(jobAddedToThreadPool == false)
                            {
                                try
                                {
                                    threadPool.execute(updateHandlingJob);
                                    jobAddedToThreadPool = true;
                                }
                                catch(final RejectedExecutionException e)
                                {
                                    if(logger.isDebugEnabled())
                                    {
                                        logger.debug(
                                            "Thread pool queue is full!",
                                            e);
                                    }

                                    try
                                    {
                                        Thread.sleep(SLEEP_TIME_MS);
                                    }
                                    catch(final InterruptedException ex)
                                    {
                                        if(logger.isDebugEnabled())
                                        {
                                            logger.debug(
                                                "Thread is interrupted" +
                                                "from sleep!",
                                                ex);
                                        }
                                    }
                                }
                            }

                            // Do not process this update any further.
                            preprocessedUpdates.add(update);
                        }
                    }
                    else
                    {
                        // If an update does not contain a user,
                        // do not process this update any further.
                        preprocessedUpdates.add(update);
                    }
                }
                else
                {
                    // If an update does not contain a message,
                    // do not process this update any further.
                    preprocessedUpdates.add(update);
                }
            }

            // Remove preprocessed updates
            // from the original list of incoming updates.
            for(final Update preprocessedUpdate : preprocessedUpdates)
            {
                list.remove(preprocessedUpdate);
            }
        }
    }
}
