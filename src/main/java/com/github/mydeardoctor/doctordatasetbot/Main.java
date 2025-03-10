package com.github.mydeardoctor.doctordatasetbot;

import com.github.mydeardoctor.doctordatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.doctordatasetbot.properties.PropertiesManager;
import com.github.mydeardoctor.doctordatasetbot.telegrambot.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;


public class Main
{
    public static void main(String[] args)
    {
        // Create Logger.
        final Logger logger = LoggerFactory.getLogger(Main.class);
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

        //Load properties.
        final PropertiesManager propertiesManager = new PropertiesManager();
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
            Thread.currentThread().join();
        }
        catch(final Exception e)
        {
            if(logger.isErrorEnabled())
            {
                logger.error("Could not start Telegram Bot application!", e);
            }
            //TODO shutdown gracefully
        }
    }
}