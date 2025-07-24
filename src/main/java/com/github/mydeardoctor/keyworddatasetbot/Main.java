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
        final String botToken =
            propertiesManager.getProperty("bot_token");
        final String databaseServerUrl =
            propertiesManager.getProperty("database_server_url");
        final String clientAppRole =
            propertiesManager.getProperty("client_app_role");
        final String clientAppPassword =
            propertiesManager.getProperty("client_app_password");
        final String clientAppCertsDirectory =
            propertiesManager.getProperty("client_app_certs_directory");
        final String clientAppDerKey =
            propertiesManager.getProperty("client_app_der_key");
        final String clientAppKeyPassword =
            propertiesManager.getProperty("client_app_key_password");
        final String clientAppCrt =
            propertiesManager.getProperty("client_app_crt");
        final String caCrt =
            propertiesManager.getProperty("ca_crt");

        // Create Database manager.
        final DatabaseManager databaseManager = new DatabaseManager(
            databaseServerUrl,
            clientAppRole,
            clientAppPassword,
            clientAppCertsDirectory,
            clientAppDerKey,
            clientAppKeyPassword,
            clientAppCrt,
            caCrt);

        // Create Telegram Bot.
        try(final TelegramBotsLongPollingApplication telegramBotApplication =
                new TelegramBotsLongPollingApplication())
        {
            Runtime.getRuntime().addShutdownHook(
                new Thread(
                    new ShutdownHookResourceCloser(telegramBotApplication)));

            final CommonResourcesManager commonResourcesManager =
                new CommonResourcesManager(databaseManager);

            final UpdateEnqueuer updateEnqueuer
                = new UpdateEnqueuer(commonResourcesManager);

            final Thread updateSchedulerThread = new Thread(
                new UpdateScheduler(commonResourcesManager));
            updateSchedulerThread.start();

            telegramBotApplication.registerBot(
                botToken,
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