package com.github.mydeardoctor.doctordatasetbot.telegrambot;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Scheduler implements Runnable
{
    private final CommonResourcesManager commonResourcesManager;
//    private final ExecutorService threadPool;
//    private final Semaphore semaphoreOfThreadPool;
    //TODO logger

    public Scheduler(final CommonResourcesManager commonResourcesManager)
    {
        super();

        this.commonResourcesManager = commonResourcesManager;

//        final int THREAD_POOL_SIZE =
//            Runtime.getRuntime().availableProcessors() + 1;
//        threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
//
//        semaphoreOfThreadPool = new Semaphore(THREAD_POOL_SIZE, true);
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch(final InterruptedException e)
            {

            }


            //Wait until there is free space in the thread pool.
//            semaphoreOfThreadPool.acquireUninterruptibly();

            //Wait until there is some data in the queue.
//            mapOfUpdatesPerUser.waitForNewData();

            //Get update from some queue.
//            final Update update = mapOfUpdatesPerUser.take();

//            System.out.println(update.getMessage().getText());

            //TODO в тред пуле по окончании задачи надо выдать семафор тред пула
//            semaphoreOfThreadPool.release();
        }
    }
}
