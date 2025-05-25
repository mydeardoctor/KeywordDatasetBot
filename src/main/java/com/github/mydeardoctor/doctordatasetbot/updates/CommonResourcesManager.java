package com.github.mydeardoctor.doctordatasetbot.updates;

import com.github.mydeardoctor.doctordatasetbot.delay.DelayManager;
import com.github.mydeardoctor.doctordatasetbot.exceptions.ShutdownHookThreadPoolCloser;
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
    private final ExecutorService threadPool;

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
        threadPool = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        Runtime.getRuntime().addShutdownHook(
            new Thread(new ShutdownHookThreadPoolCloser(threadPool)));
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
        threadPool.execute(
            new UpdateHandlingJob(update, this));
        setOfUsersInThreadPool.add(candidate);
        //Update is submitted. Free space in virtual queue.
        spaceInVirtualQueueOfUpdates.release();

        //End of critical section.
        mutex.unlock();
    }

    public void finishHandlingUpdate(final Long userId)
    {
        //Protect common resources. Start of critical section.
        mutex.lock();

        if(userId != null)
        {
            setOfUsersInThreadPool.remove(userId);
        }
        //Update is handled. Free space in thread pool.
        spaceInThreadPool.release();

        //End of critical section.
        mutex.unlock();
    }
}