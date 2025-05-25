package com.github.mydeardoctor.doctordatasetbot.telegrambot;

import com.github.mydeardoctor.doctordatasetbot.delay.DelayManager;
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
    private final Set<Long> setOfUsersInQueue;

    private static final int MAX_NUMBER_OF_THREADS =
        Runtime.getRuntime().availableProcessors() + 1;
    private final Semaphore spaceInThreadPool;
    private final Set<Long> setOfUsersInThreadPool;
    private static final long DELAY_MS = 10;

    //TODO
    //TODO thread pool default unhandler exception handler
    //TODO Thread pool if thread fail release semaphore (release available space)
    //TODO thread pool shutdown. shutdown hook. wait for threads to finish. dp not ignore interrupt in thread in threadpoool fpr correct shutdown

    // Thread pool to handle incoming updates.
//    private static final int MAX_UPDATES = 100;
//    private static final int QUEUE_SIZE = MAX_UPDATES * 2;
//    private static final int THREAD_POOL_SIZE =
//        ;
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
        setOfUsersInQueue = new HashSet<Long>(MAX_NUMBER_OF_USERS);

        spaceInThreadPool = new Semaphore(MAX_NUMBER_OF_THREADS);
        setOfUsersInThreadPool = new HashSet<Long>(MAX_NUMBER_OF_THREADS);
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

        //Update set of users in queue and queue of users for future scheduling.
        if(!setOfUsersInQueue.contains(userId))
        {
            queueOfUsers.add(userId);
            setOfUsersInQueue.add(userId);
        }

        //Signal that new data is available.
        dataInVirtualQueueOfUpdates.release();

        //End of critical section.
        mutex.unlock();
    }

    public void scheduleUpdate()
    {
        //Wait for space in thread pool and then take it.
        spaceInThreadPool.acquireUninterruptibly();

        //Wait for new data.
        dataInVirtualQueueOfUpdates.acquireUninterruptibly();

        //Protect common resources. Start of critical section.
        mutex.lock();

        //Scheduling algorithm.
        Long candidate = null;
        boolean foundSuitableCandidate = false;
        while(foundSuitableCandidate == false)
        {
            candidate = queueOfUsers.remove();
            //If candidate user cannot be put in thread pool.
            if(setOfUsersInThreadPool.contains(candidate))
            {
                queueOfUsers.add(candidate);
                //End of critical section.
                mutex.unlock();

                //Give time for other threads to use common resources.
                DelayManager.delay(DELAY_MS);

                //Protect common resources. Start of critical section.
                mutex.lock();
            }
            //If candidate user can be put in thread pool.
            else
            {
                foundSuitableCandidate = true;
            }
        }

        Queue<Update> candidatesQueueOfUpdates = queuesOfUpdates.get(candidate);
        final Update update = candidatesQueueOfUpdates.remove();
        final Update nextUpdate = candidatesQueueOfUpdates.peek();
        if(nextUpdate == null)
        {
            candidatesQueueOfUpdates = null;
            queuesOfUpdates.put(candidate, null);
            queuesOfUpdates.remove(candidate);
            setOfUsersInQueue.remove(candidate);
        }
        else
        {
            queueOfUsers.add(candidate);
        }

        //Put update in thread pool.




        //TODO mutex unlock
    }
}