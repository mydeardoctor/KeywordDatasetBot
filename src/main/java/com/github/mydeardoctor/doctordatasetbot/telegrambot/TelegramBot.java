package com.github.mydeardoctor.doctordatasetbot.telegrambot;

import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class TelegramBot implements LongPollingUpdateConsumer
{
    public TelegramBot()
    {

    }

    @Override
    public void consume(List<Update> list)
    {
        //TODO log thread
        System.out.println("im handling");
    }
}
