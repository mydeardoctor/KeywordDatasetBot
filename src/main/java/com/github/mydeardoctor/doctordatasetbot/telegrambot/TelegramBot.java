package com.github.mydeardoctor.doctordatasetbot.telegrambot;

import com.github.mydeardoctor.doctordatasetbot.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class TelegramBot implements LongPollingUpdateConsumer
{
    private final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    public TelegramBot()
    {

    }

    @Override
    public void consume(List<Update> list)
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
    }
}
