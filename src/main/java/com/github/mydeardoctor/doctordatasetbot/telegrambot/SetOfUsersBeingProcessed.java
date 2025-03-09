package com.github.mydeardoctor.doctordatasetbot.telegrambot;

import java.util.HashSet;
import java.util.Set;

// Set of users that are currently being processed in the thread pool.
public class SetOfUsersBeingProcessed
{
    private final Set<Long> setOfUserIdsBeingProcessed = new HashSet<Long>();

    public SetOfUsersBeingProcessed()
    {

    }

    public synchronized Set<Long> getSnapshot()
    {
        final Set<Long> snapshot =
            new HashSet<Long>(setOfUserIdsBeingProcessed);
        return snapshot;
    }

    public synchronized boolean add(Long userId)
    {
        final boolean result = setOfUserIdsBeingProcessed.add(userId);
        return result;
    }

    public synchronized boolean remove(Long userId)
    {
        final boolean result = setOfUserIdsBeingProcessed.remove(userId);
        return result;
    }
}
