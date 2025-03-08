package com.github.mydeardoctor.doctordatasetbot;

import ch.qos.logback.classic.LoggerContext;
import com.github.mydeardoctor.doctordatasetbot.telegrambot.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main
{
    public static void main(String[] args)
    {
        //TODO log thread
        final Logger logger = LoggerFactory.getLogger(Main.class);
        if(logger.isWarnEnabled())
        {
            logger.warn("Started main");
        }

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
            LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
            loggerContext.stop();
            //TODO read shutdown hooks

            e.printStackTrace();
        }
    }
}