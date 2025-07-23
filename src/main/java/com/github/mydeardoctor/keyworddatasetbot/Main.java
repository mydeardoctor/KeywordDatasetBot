package com.github.mydeardoctor.keyworddatasetbot;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.shutdown.ShutdownHookLogback;
import com.github.mydeardoctor.keyworddatasetbot.shutdown.ShutdownHookPrinter;
import com.github.mydeardoctor.keyworddatasetbot.shutdown.ShutdownHookResourceCloser;
import com.github.mydeardoctor.keyworddatasetbot.shutdown.UncaughtExceptionHandler;
import com.github.mydeardoctor.keyworddatasetbot.properties.PropertiesManager;
import com.github.mydeardoctor.keyworddatasetbot.updates.CommonResourcesManager;
import com.github.mydeardoctor.keyworddatasetbot.updates.UpdateScheduler;
import com.github.mydeardoctor.keyworddatasetbot.updates.UpdateEnqueuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.io.IOException;


/* TODO
   автоматизировать установку java, maven, в т.ч. для Docker

   База данных:
    firewall
   conncetion pool hikari
   транзакции*/
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
        Runtime.getRuntime().addShutdownHook(
            new Thread(new ShutdownHookLogback()));

        // Set default uncaught exception handler.
        UncaughtExceptionHandler uncaughtExceptionHandler =
            new UncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

        // Load properties.
        PropertiesManager propertiesManager = null;
        try
        {
            propertiesManager = new PropertiesManager(
                "/application.properties");
        }
        catch(final IOException e)
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
                new Thread(
                    new ShutdownHookPrinter(errorMessage)));
            System.exit(1);
        }
        final String keywordDatasetBotToken =
            propertiesManager.getProperty("keyword_dataset_bot_token");
        final String keywordDatabaseUrl =
            propertiesManager.getProperty("keyword_database_url");
        final String keywordDatabaseUser =
            propertiesManager.getProperty("keyword_database_user");
        final String keywordDatabasePassword =
            propertiesManager.getProperty("keyword_database_password");

        // Create Database manager.
        final DatabaseManager databaseManager = new DatabaseManager(
            keywordDatabaseUrl,
            keywordDatabaseUser,
            keywordDatabasePassword);
        databaseManager.getData();

        // Create Telegram Bot.
        try(final TelegramBotsLongPollingApplication telegramBotApplication =
                new TelegramBotsLongPollingApplication())
        {
            Runtime.getRuntime().addShutdownHook(
                new Thread(
                    new ShutdownHookResourceCloser(telegramBotApplication)));

            final CommonResourcesManager commonResourcesManager =
                new CommonResourcesManager();

            final UpdateEnqueuer updateEnqueuer
                = new UpdateEnqueuer(commonResourcesManager);

            final Thread updateSchedulerThread = new Thread(
                new UpdateScheduler(commonResourcesManager));
            updateSchedulerThread.start();

            telegramBotApplication.registerBot(
                keywordDatasetBotToken,
                updateEnqueuer);

            /* Deadlock. The main thread is blocked forever.
            TelegramBots Java library implementation
            requires the main thread to be alive. */
            Thread.currentThread().join();
        }
        catch(final Exception e)
        {
            final String errorMessage =
                "Could not start Telegram Bot application!";
            logger.error(errorMessage, e);

            final Throwable[] suppressedExceptions = e.getSuppressed();
            for(final Throwable suppressedException : suppressedExceptions)
            {
                logger.error(
                    "Accompanied by suppressed exception:",
                    suppressedException);
            }

            Runtime.getRuntime().addShutdownHook(
                new Thread(
                    new ShutdownHookPrinter(errorMessage)));
            System.exit(1);
        }
    }
}