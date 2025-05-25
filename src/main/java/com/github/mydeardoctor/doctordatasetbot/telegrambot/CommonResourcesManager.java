package com.github.mydeardoctor.doctordatasetbot.telegrambot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommonResourcesManager
{
    private final Lock mutex;

    private static final int MAX_NUMBER_OF_UPDATES = 1024;
    private final Semaphore spaceInVirtualQueueOfUpdates;
    private final Semaphore dataInVirtualQueueOfUpdates;
    private static final int MAX_NUMBER_OF_USERS = MAX_NUMBER_OF_UPDATES;
    private final Map<Long, Queue<Update>> queuesOfUpdates;
    private final Queue<Long> queueOfUsers;
    private final Set<Long> setOfQueuedUsers;

    private final Logger logger;

    //TODO shutdown hooks for resources. for thread pool.
    public CommonResourcesManager()
    {
        super();

        mutex = new ReentrantLock(true);

        spaceInVirtualQueueOfUpdates =
            new Semaphore(MAX_NUMBER_OF_UPDATES);
        dataInVirtualQueueOfUpdates =
            new Semaphore(0);
        queuesOfUpdates = new HashMap<Long, Queue<Update>>(MAX_NUMBER_OF_USERS);
        queueOfUsers = new LinkedBlockingQueue<Long>(MAX_NUMBER_OF_USERS);
        setOfQueuedUsers = new HashSet<Long>(MAX_NUMBER_OF_USERS);

        logger = LoggerFactory.getLogger(CommonResourcesManager.class);
    }

    public void enqueueUpdate(final Update update)
    {
        //Check input argument.
        if((update == null) || (!update.hasMessage()))
        {
            return;
        }

        //Wait for space in virtual queue and then put update in virtual queue.
        spaceInVirtualQueueOfUpdates.acquireUninterruptibly();

        //Protect common resources. Start of critical section.
        mutex.lock();

        //Put update in a queue of specific user.
        final Long userId = update.getMessage().getFrom().getId();
        if(!queuesOfUpdates.containsKey(userId))
        {
            queuesOfUpdates.put(
                userId,
                new LinkedBlockingQueue<Update>(MAX_NUMBER_OF_UPDATES));
        }
        queuesOfUpdates.get(userId).add(update);

        //Update set of queued users and queue of users for future scheduling.
        if(!setOfQueuedUsers.contains(userId))
        {
            queueOfUsers.add(userId);
            setOfQueuedUsers.add(userId);
        }

        //Signal that new data is available.
        dataInVirtualQueueOfUpdates.release();

        //End of critical section.
        mutex.unlock();
    }
}
