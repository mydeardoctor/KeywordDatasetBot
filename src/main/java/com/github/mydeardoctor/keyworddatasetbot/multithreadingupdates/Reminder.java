package com.github.mydeardoctor.keyworddatasetbot.multithreadingupdates;

import com.github.mydeardoctor.keyworddatasetbot.application.ApplicationManager;
import com.github.mydeardoctor.keyworddatasetbot.shutdown.ShutdownHookExecutorServiceCloser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Reminder implements Runnable
{
    private final ScheduledExecutorService scheduler;
    private final ApplicationManager applicationManager;
    private final Logger logger;

    public Reminder(
        final String timeZone,
        final int hourToRemind,
        final ApplicationManager applicationManager)
    {
        super();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        Runtime.getRuntime().addShutdownHook(
            new Thread(new ShutdownHookExecutorServiceCloser(scheduler)));

        this.applicationManager = applicationManager;

        final ZoneId zoneId = ZoneId.of(timeZone);
        final ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
        ZonedDateTime targetTime = currentTime
            .withHour(hourToRemind)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        if(!currentTime.isBefore(targetTime))
        {
            targetTime = targetTime.plusDays(1);
        }
        final Duration initialDelay = Duration.between(currentTime, targetTime);
        final long initialDelayMinutes = initialDelay.toMinutes();
        final long periodMinutes = 24 * 60;

        scheduler.scheduleAtFixedRate(
            this,
            initialDelayMinutes,
            periodMinutes,
            TimeUnit.MINUTES);

        logger = LoggerFactory.getLogger(Reminder.class);
    }

    @Override
    public void run()
    {
        logger.debug(
            "Thread: group = {}, name = {}, priority = {}.",
            Thread.currentThread().getThreadGroup().getName(),
            Thread.currentThread().getName(),
            Thread.currentThread().getPriority());

        applicationManager.remindUsers();
    }
}