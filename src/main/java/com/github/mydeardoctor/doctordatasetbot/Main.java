package com.github.mydeardoctor.doctordatasetbot;

import com.github.mydeardoctor.doctordatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.doctordatasetbot.exceptions.ShutdownHookPrinter;
import com.github.mydeardoctor.doctordatasetbot.exceptions.UncaughtExceptionHandler;
import com.github.mydeardoctor.doctordatasetbot.properties.PropertiesManager;
import com.github.mydeardoctor.doctordatasetbot.telegrambot.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.io.IOException;


public class Main
{
    public static void main(String[] args)
    {
        // Create Logger.
        final Logger logger = LoggerFactory.getLogger(Main.class);
        logger.debug(
            "Thread: group = {}, name = {}, priority = {}.",
            Thread.currentThread().getThreadGroup().getName(),
            Thread.currentThread().getName(),
            Thread.currentThread().getPriority());

        // Set default uncaught exception handler.
        UncaughtExceptionHandler uncaughtExceptionHandler =
            new UncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

        //Load properties.
        PropertiesManager propertiesManager = null;
        try
        {
            propertiesManager = new PropertiesManager(
                "/application.properties");
        }
        catch(IOException e)
        {
            final String errorMessage = "Could not read .properties!";
            logger.error(errorMessage, e);

            final Throwable[] suppressedExceptions = e.getSuppressed();
            for(final Throwable suppressedException : suppressedExceptions)
            {
                logger.error(
                    "Accompanied by suppressed exception:",
                    suppressedException);
            }

            Runtime.getRuntime().addShutdownHook(
                new Thread(new ShutdownHookPrinter(errorMessage)));
            System.exit(1);
        }
        final String doctorDatasetBotToken =
            propertiesManager.getProperty("doctor_dataset_bot_token");
        final String doctorDatasetDatabaseUrl =
            propertiesManager.getProperty("doctor_dataset_database_url");
        final String doctorDatasetDatabaseUser =
            propertiesManager.getProperty("doctor_dataset_database_user");
        final String doctorDatasetDatabasePassword =
            propertiesManager.getProperty("doctor_dataset_database_password");


        // Create Telegram Bot.
        try(final TelegramBotsLongPollingApplication telegramBotApplication =
                new TelegramBotsLongPollingApplication())
        {
            final String telegramBotToken = System.getenv(
                "DOCTOR_DATASET_BOT_TOKEN");
            final TelegramBot telegramBot = new TelegramBot();
            telegramBotApplication.registerBot(telegramBotToken, telegramBot);


            //TODO убрать
//            final DatabaseManager databaseManager = new DatabaseManager();
//            databaseManager.getData();

            // Wait for this thread to terminate.
            Thread.currentThread().join(); //TODO what? its a deadlock!
            //todo does this print?
            //todo it should be telegramthread.join() т.е. ждать пока не закончится телеграм тред
        }
        catch(final Exception e)
        {
            logger.error("Could not start Telegram Bot application!", e);
            //TODO shutdown gracefully
        }
    }
}