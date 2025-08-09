package com.github.mydeardoctor.keyworddatasetbot;

import com.github.mydeardoctor.keyworddatasetbot.application.ApplicationManager;
import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.multithreadingupdates.Reminder;
import com.github.mydeardoctor.keyworddatasetbot.shutdown.ShutdownHookLogback;
import com.github.mydeardoctor.keyworddatasetbot.shutdown.ShutdownHookPrinter;
import com.github.mydeardoctor.keyworddatasetbot.shutdown.ShutdownHookResourceCloser;
import com.github.mydeardoctor.keyworddatasetbot.shutdown.UncaughtExceptionHandler;
import com.github.mydeardoctor.keyworddatasetbot.properties.PropertiesManager;
import com.github.mydeardoctor.keyworddatasetbot.multithreadingupdates.CommonResourcesManager;
import com.github.mydeardoctor.keyworddatasetbot.multithreadingupdates.UpdateScheduler;
import com.github.mydeardoctor.keyworddatasetbot.multithreadingupdates.UpdateEnqueuer;
import com.github.mydeardoctor.keyworddatasetbot.telegram.TelegramCommunicationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

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
        //TODO ловить nosuchelementexception
        final String botToken =
            propertiesManager.getProperty("bot_token");
        final String voiceExtension =
            propertiesManager.getProperty("voice_extension");
        final String timeZone =
            propertiesManager.getProperty("time_zone");
        final String hourToRemindAsString =
            propertiesManager.getProperty("hour_to_remind");
        final int hourToRemind =
            Integer.parseInt(hourToRemindAsString);
        final String databaseServerUrl =
            propertiesManager.getProperty("database_server_url");
        final String appRole =
            propertiesManager.getProperty("app_role");
        final String appRolePassword =
            propertiesManager.getProperty("app_role_password");
        final String appCertsDirectory =
            propertiesManager.getProperty("app_certs_directory");
        final String appAudioDirectory =
            propertiesManager.getProperty("app_audio_directory");
        final String appDerKey =
            propertiesManager.getProperty("app_der_key");
        final String appKeyPassword =
            propertiesManager.getProperty("app_key_password");
        final String appCrt =
            propertiesManager.getProperty("app_crt");
        final String caCrt =
            propertiesManager.getProperty("ca_crt");

        // Create Telegram Bot.
        try(final TelegramBotsLongPollingApplication telegramBotApplication =
                new TelegramBotsLongPollingApplication())
        {
            Runtime.getRuntime().addShutdownHook(
                new Thread(
                    new ShutdownHookResourceCloser(telegramBotApplication)));

            final DatabaseManager databaseManager = new DatabaseManager(
                databaseServerUrl,
                appRole,
                appRolePassword,
                appCertsDirectory,
                appDerKey,
                appKeyPassword,
                appCrt,
                caCrt);

            final TelegramClient telegramClient
                = new OkHttpTelegramClient(botToken);

            final TelegramCommunicationManager
                telegramCommunicationManager
                = new TelegramCommunicationManager(telegramClient);

            final ApplicationManager applicationManager
                = new ApplicationManager(
                    databaseManager,
                    telegramCommunicationManager,
                    appAudioDirectory,
                    voiceExtension);

            final Reminder reminder = new Reminder(
                timeZone,
                hourToRemind,
                applicationManager);

            final CommonResourcesManager commonResourcesManager =
                new CommonResourcesManager(applicationManager);

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