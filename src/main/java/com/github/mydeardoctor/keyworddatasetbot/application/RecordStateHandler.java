package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.AudioClassRepository;
import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.database.TelegramUserRepository;
import com.github.mydeardoctor.keyworddatasetbot.database.VoiceRepository;
import com.github.mydeardoctor.keyworddatasetbot.domain.*;
import com.github.mydeardoctor.keyworddatasetbot.telegram.TelegramCommunicationManager;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Voice;

import java.sql.SQLException;

public class RecordStateHandler extends StateHandler
{
    public RecordStateHandler(
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
            LoggerFactory.getLogger(RecordStateHandler.class));
    }

    @Override
    protected void handleVoice(
        final Voice voice,
        final Long chatId,
        final Long userId)
        throws SQLException
    {
        try
        {
            super.handleVoice(
                voice,
                chatId,
                userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }

        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        //Query DB.
        final int durationRoundedDownSeconds = voice.getDuration();
        final int durationRoundedUpSeconds = durationRoundedDownSeconds + 1;

        int maxDurationSeconds = 0;
        try
        {
            maxDurationSeconds = audioClassRepository.getMaxDuration(userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }

        //Check duration.
        //Duration is too long.
        if(durationRoundedUpSeconds > maxDurationSeconds)
        {
            //Send message to telegram user.
            final String messageVoiceIsTooLong =
                String.format(
                    TelegramCommunicationManager.MESSAGE_VOICE_IS_TOO_LONG_FORMAT,
                    maxDurationSeconds);
            telegramCommunicationManager.sendMessage(
                chatId,
                messageVoiceIsTooLong,
                null,
                null);

            //Enter state "record" again.
            //Imitate garbage.
            try
            {
                handleGarbage(chatId, userId);
            }
            catch(final SQLException e)
            {
                throw e;
            }

            return;
        }

        //Duration is OK.
        //Save voice to DB.
        final String fileUniqueId = voice.getFileUniqueId();
        final String fileId = voice.getFileId();

        AudioClass audioClass = null;
        try
        {
            audioClass = telegramUserRepository.getAudioClass(userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
        if(audioClass == null)
        {
            final String errorMessage =
                "Telegram user does not contain chosen audio class!";
            throw new SQLException(errorMessage);
        }

        try
        {
            voiceRepository.saveVoice(
                fileUniqueId,
                fileId,
                durationRoundedUpSeconds,
                audioClass,
                userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }

        //Update most recent voice.
        try
        {
            telegramUserRepository.updateMostRecentVoice(
                userId,
                fileUniqueId);
        }
        catch(final SQLException e)
        {
            throw e;
        }

        try
        {
            handleVoiceWithCorrectDuration(
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

        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        //Query DB.
        AudioClass audioClass = null;
        try
        {
            audioClass = telegramUserRepository.getAudioClass(userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
        if(audioClass == null)
        {
            final String errorMessage =
                "Telegram user does not contain chosen audio class!";
            throw new SQLException(errorMessage);
        }

        //Enter state "record" again.
        //Imitate callback query with chosen audio class.
        try
        {
            super.handleCallbackQueryWithChosenAudioClass(
                AudioClassMapper.toString(audioClass),
                chatId,
                userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
