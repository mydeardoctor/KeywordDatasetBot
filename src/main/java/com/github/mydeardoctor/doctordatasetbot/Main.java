package com.github.mydeardoctor.doctordatasetbot;

import com.github.mydeardoctor.doctordatasetbot.telegrambot.TelegramBot;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main
{
    public static void main(String[] args)
    {
        //TODO log thread

        try(final TelegramBotsLongPollingApplication telegramBotApplication =
                new TelegramBotsLongPollingApplication())
        {
            final String telegramBotToken = System.getenv(
                "DOCTOR_DATASET_BOT_TOKEN");
            final TelegramBot telegramBot = new TelegramBot();
            telegramBotApplication.registerBot(telegramBotToken, telegramBot);
            //todo logging that started

            Thread.currentThread().join();
        }
        catch(Exception e)
        {
            //TODO add logger. настройки логгера
            //TODO graceful close
            e.printStackTrace();
        }
    }
}