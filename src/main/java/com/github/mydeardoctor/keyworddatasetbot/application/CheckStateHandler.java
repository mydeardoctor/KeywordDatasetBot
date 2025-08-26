package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.*;
import com.github.mydeardoctor.keyworddatasetbot.domain.*;
import com.github.mydeardoctor.keyworddatasetbot.telegram.TelegramCommunicationManager;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CheckStateHandler extends StateHandler
{
    public CheckStateHandler(
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
            LoggerFactory.getLogger(CheckStateHandler.class));
    }

    @Override
    protected void onStartReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        deleteMostRecentVoice(chatId, userId);

        super.onStartReceive(chatId, userId);
    }

    @Override
    protected void onStatsReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        deleteMostRecentVoice(chatId, userId);

        super.onStatsReceive(chatId, userId);
    }

    @Override
    protected void onHelpReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        deleteMostRecentVoice(chatId, userId);

        super.onHelpReceive(chatId, userId);
    }

    @Override
    protected void onAboutReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        deleteMostRecentVoice(chatId, userId);

        super.onAboutReceive(chatId, userId);
    }

    @Override
    protected void onCancelReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        deleteMostRecentVoice(chatId, userId);

        super.onCancelReceive(chatId, userId);
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
                TelegramCommunicationManager.MESSAGE_CHECK);
        if(!isExpectedCallbackQuery)
        {
            return;
        }

        final String answerAsString = callbackQuery.getData();
        final Answer answer = AnswerMapper.fromString(answerAsString);

        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        switch(answer)
        {
            case NO ->
            {
                //Delete most recent voice.
                deleteMostRecentVoice(chatId, userId);

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

            case YES ->
            {
                //Download voice.
                String fileUniqueId = null;
                String fileId = null;
                String audioClassAsString = null;
                try
                {
                    final FileIdsAndAudioClass fileIdsAndAudioClass =
                        voiceRepository.getVoiceFileIdsAndAudioClass(userId);
                    fileUniqueId =
                        fileIdsAndAudioClass.getFileUniqueId();
                    fileId =
                        fileIdsAndAudioClass.getFileId();
                    final AudioClass audioClass =
                        fileIdsAndAudioClass.getAudioClass();
                    audioClassAsString =
                        AudioClassMapper.toString(audioClass);
                }
                catch(final SQLException e)
                {
                    throw e;
                }

                try
                {
                    telegramCommunicationManager.downloadFile(
                        fileId,
                        appAudioDirectory,
                        audioClassAsString,
                        fileUniqueId,
                        voiceExtension);
                }
                catch(final TelegramApiException | IOException e)
                {
                    throw e;
                }

                //Reset most recent voice.
                try
                {
                    telegramUserRepository.updateMostRecentVoice(
                        userId,
                        null);
                }
                catch(final SQLException e)
                {
                    throw e;
                }

                //Send message to telegram user.
                telegramCommunicationManager.sendMessage(
                    chatId,
                    TelegramCommunicationManager.MESSAGE_THANK_YOU,
                    null,
                    null);

                //Enter state "choose" again.
                //Imitate /start command.
                try
                {
                    super.onStartReceive(chatId, userId);
                }
                catch(final SQLException e)
                {
                    throw e;
                }
            }

            default ->
            {

            }
        }
    }

    @Override
    protected void handleGarbage(final Long chatId, Long userId)
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

        //Enter state "check" again.
        //Imitate voice with correct duration.
        try
        {
            super.handleVoiceWithCorrectDuration(chatId, userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    private void deleteMostRecentVoice(final Long chatId, final Long userId)
        throws SQLException
    {
        //Query DB.
        try
        {
            voiceRepository.deleteMostRecentVoice(userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
