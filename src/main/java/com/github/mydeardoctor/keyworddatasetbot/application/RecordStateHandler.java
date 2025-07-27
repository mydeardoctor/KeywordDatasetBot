package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.domain.*;
import com.github.mydeardoctor.keyworddatasetbot.telegramuser.TelegramUserCommunicationManager;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecordStateHandler extends StateHandler
{
    public RecordStateHandler(
        final DatabaseManager databaseManager,
        final TelegramUserCommunicationManager telegramUserCommunicationManager)
    {
        super(
            databaseManager,
            telegramUserCommunicationManager,
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
        telegramUserCommunicationManager.sendChatAction(
            chatId,
            TelegramUserCommunicationManager.CHAT_ACTION_TYPING);

        //Query DB.
        final int durationRoundedDownSeconds = voice.getDuration();
        final int durationRoundedUpSeconds = durationRoundedDownSeconds + 1;

        int maxDurationSeconds = 0;
        try
        {
            maxDurationSeconds = databaseManager.getMaxDuration(userId);
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
                    TelegramUserCommunicationManager.MESSAGE_VOICE_IS_TOO_LONG_FORMAT,
                    maxDurationSeconds);
            telegramUserCommunicationManager.sendMessage(
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
            audioClass = databaseManager.getAudioClass(userId);
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
            databaseManager.saveVoice(
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
            databaseManager.updateMostRecentVoice(
                userId,
                fileUniqueId);
        }
        catch(final SQLException e)
        {
            throw e;
        }

        //Prepare message.
        final List<Answer> answers = new ArrayList<>();
        answers.add(Answer.YES);
        answers.add(Answer.NO);
        final List<String> answersHumanReadable = new ArrayList<>();
        final List<String> answersAsString = new ArrayList<>();
        for(final Answer answer : answers)
        {
            answersHumanReadable.add(answer.toString());
            answersAsString.add(AnswerMapper.map(answer));
        }

        //Send message to telegram user.
        telegramUserCommunicationManager.sendMessage(
            chatId,
            TelegramUserCommunicationManager.MESSAGE_CHECK,
            answersHumanReadable,
            answersAsString);

        //Change state.
        try
        {
            databaseManager.updateDialogueState(
                userId,
                DialogueState.CHECK);
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
        telegramUserCommunicationManager.sendChatAction(
            chatId,
            TelegramUserCommunicationManager.CHAT_ACTION_TYPING);

        //Query DB.
        AudioClass audioClass = null;
        try
        {
            audioClass = databaseManager.getAudioClass(userId);
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
                AudioClassMapper.map(audioClass),
                chatId,
                userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
