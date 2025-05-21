package com.github.mydeardoctor.doctordatasetbot.telegrambot;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class MapOfUpdatesPerUser
{
    //TODO общие типы, конкретные имплементации
    private final HashMap<Long, LinkedBlockingQueue<Update>> queues;
    private final Semaphore semaphoreOfQueues;
    //TODO logger

    public MapOfUpdatesPerUser()
    {
        super();
        queues = new HashMap<>();
        semaphoreOfQueues = new Semaphore(0, true);
    }

    public synchronized void put(final Long userId, final Update update)
    {
        if(!queues.containsKey(userId))
        {
            //TODO do i bound capacity? i should
            queues.put(userId, new LinkedBlockingQueue<Update>());
        }
        //TODO do i block when i put?
        final BlockingQueue<Update> queue = queues.get(userId);

        boolean result = false;
        while(result == false)
        {
            try
            {
                //Blocks if queue is full.
                //TODO skip if timeout is reached?
                queue.put(update);
                result = true;
            }
            catch(final InterruptedException e)
            {
                //TODO debug
            }
        }
    }

    public synchronized Update take()
    {
        //TODO scheduling algorythm
        Update update = null;

        for(final BlockingQueue<Update> queue: queues.values())
        {
            if(!queue.isEmpty())
            {
                boolean result = false;
                while(result == false)
                {
                    try
                    {
                        update = queue.take();
                        result = true;
                    }
                    catch(final InterruptedException e)
                    {
                        //TODO debug log
                    }
                }
                break;
            }
        }

        return update;
    }


    public void signalNewData()
    {
        semaphoreOfQueues.release();
    }

    public void waitForNewData()
    {
        //TODO если я взял семафор, значит, я взял один апдейт
        semaphoreOfQueues.acquireUninterruptibly();
    }
}
