package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.AudioClassRepository;
import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.database.TelegramUserRepository;
import com.github.mydeardoctor.keyworddatasetbot.database.VoiceRepository;
import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import com.github.mydeardoctor.keyworddatasetbot.telegram.TelegramCommunicationManager;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public class ChooseStateHandler extends StateHandler
{
    public ChooseStateHandler(
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
            LoggerFactory.getLogger(ChooseStateHandler.class));
    }

    @Override
    protected void handleCallbackQuery(
        final CallbackQuery callbackQuery,
        final Long chatId,
        final Long userId)
        throws SQLException, TelegramApiException, IOException
    {
        try
        {
            super.handleCallbackQuery(
                callbackQuery,
                chatId,
                userId);
        }
        catch(final SQLException | TelegramApiException | IOException e)
        {
            throw e;
        }

        final boolean isExpectedCallbackQuery =
            getIsExpectedCallbackQuery(
                callbackQuery,
                TelegramCommunicationManager.MESSAGE_CHOOSE);
        if(!isExpectedCallbackQuery)
        {
            return;
        }

        final String audioClassAsString = callbackQuery.getData();
        try
        {
            super.handleCallbackQueryWithChosenAudioClass(
                audioClassAsString,
                chatId,
                userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    @Override
    protected void handleGarbage(final Long chatId, final Long userId)
        throws SQLException
    {
        try
        {
            super.handleGarbage(chatId, userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }

        //Enter state "choose" again.
        //Imitate "/start" command.
        try
        {
            super.onStartReceive(chatId, userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
