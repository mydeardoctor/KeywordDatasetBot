package com.github.mydeardoctor.doctordatasetbot.telegrambot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.concurrent.*;
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

    //TODO
    // Thread pool to handle incoming updates.
//    private static final int MAX_UPDATES = 100;
//    private static final int QUEUE_SIZE = MAX_UPDATES * 2;
//    private static final int THREAD_POOL_SIZE =
//        Runtime.getRuntime().availableProcessors() + 1;
//    private final ExecutorService threadPool;
//
//    // Set of users that are currently being processed in the thread pool.
//    private final SetOfUsersBeingProcessed setOfUsersBeingProcessed
//        = new SetOfUsersBeingProcessed();
//
//    private static final int SLEEP_TIME_MS = 100;
    // Queue for update handling jobs.
//    final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
//        QUEUE_SIZE,
//        true);
//
//    // Thread pool to handle incoming updates.
//        this.threadPool = new ThreadPoolExecutor(
//    THREAD_POOL_SIZE,
//    THREAD_POOL_SIZE,
//            0,
//    TimeUnit.MILLISECONDS,
//    queue);

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