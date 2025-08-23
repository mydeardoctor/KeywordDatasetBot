package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.AudioClassRepository;
import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.database.TelegramUserRepository;
import com.github.mydeardoctor.keyworddatasetbot.database.VoiceRepository;
import com.github.mydeardoctor.keyworddatasetbot.telegram.TelegramCommunicationManager;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class StartStateHandler extends StateHandler
{
    public StartStateHandler(
        final TelegramUserRepository telegramUserRepository,
        final AudioClassRepository audioClassRepository,
        final VoiceRepository voiceRepository,
        final TelegramCommunicationManager telegramCommunicationManager,
        final String appAudioDirectory,
        final String voiceExtension)
    {
        super(
            telegramUserRepository,
            audioClassRepository,
            voiceRepository,
            telegramCommunicationManager,
            appAudioDirectory,
            voiceExtension,
            LoggerFactory.getLogger(StartStateHandler.class));
    }
}
