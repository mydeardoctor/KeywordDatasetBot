package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.telegram.TelegramCommunicationManager;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class StartStateHandler extends StateHandler
{
    public StartStateHandler(
        final DatabaseManager databaseManager,
        final TelegramCommunicationManager telegramCommunicationManager,
        final String clientAppAudioDirectory,
        final String voiceExtension)
    {
        super(
            databaseManager,
            telegramCommunicationManager,
            clientAppAudioDirectory,
            voiceExtension,
            LoggerFactory.getLogger(StartStateHandler.class));
    }
}
