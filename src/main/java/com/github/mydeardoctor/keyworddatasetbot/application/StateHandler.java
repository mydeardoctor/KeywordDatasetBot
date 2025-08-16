package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.domain.*;
import com.github.mydeardoctor.keyworddatasetbot.telegram.TelegramCommunicationManager;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class StateHandler
{
    protected final DatabaseManager databaseManager;
    protected final TelegramCommunicationManager
        telegramCommunicationManager;
    protected final String appAudioDirectory;
    protected final String voiceExtension;
    private final Logger logger;

    public StateHandler(
        final DatabaseManager databaseManager,
        final TelegramCommunicationManager
            telegramCommunicationManager,
        final String appAudioDirectory,
        final String voiceExtension,
        final Logger logger)
    {
        super();

        this.databaseManager = databaseManager;
        this.telegramCommunicationManager = telegramCommunicationManager;
        this.appAudioDirectory = appAudioDirectory;
        this.voiceExtension = voiceExtension;
        this.logger = logger;
    }

    public final void handleUpdate(
        final Update update,
        final Long chatId,
        final Long userId)
        throws SQLException, TelegramApiException, IOException
    {
        final boolean isValid = UpdateUtilities.getIsValid(update);
        if(!isValid)
        {
            return;
        }

        final boolean isCommand = getIsCommand(update);
        final boolean isCallbackQuery = getIsCallbackQuery(update);
        final boolean isVoice = getIsVoice(update);

        if(isCommand)
        {
            final Message message = update.getMessage();
            final String commandAsString = message.getText();
            try
            {
                handleCommand(commandAsString, chatId, userId);
            }
            catch(final SQLException e)
            {
                throw e;
            }
        }
        else if(isCallbackQuery)
        {
            final CallbackQuery callbackQuery = update.getCallbackQuery();
            try
            {
                handleCallbackQuery(
                    callbackQuery,
                    chatId,
                    userId);
            }
            catch(final SQLException | TelegramApiException | IOException e)
            {
                throw e;
            }
        }
        else if(isVoice)
        {
            final Message message = update.getMessage();
            final Voice voice = message.getVoice();
            try
            {
                handleVoice(
                    voice,
                    chatId,
                    userId);
            }
            catch(final SQLException e)
            {
                throw e;
            }
        }
        else
        {
            try
            {
                handleGarbage(chatId, userId);
            }
            catch(final SQLException e)
            {
                throw e;
            }
        }
    }

    private boolean getIsCommand(final Update update)
    {
        if((update == null) ||
           (!update.hasMessage()) ||
           (!update.getMessage().isCommand()))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean getIsCallbackQuery(final Update update)
    {
        if((update == null) ||
           (!update.hasCallbackQuery()))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    protected boolean getIsExpectedCallbackQuery(
        final CallbackQuery callbackQuery,
        final String initialMessage)
    {
        final MaybeInaccessibleMessage maybeInaccessibleMessage =
            callbackQuery.getMessage();
        if(!(maybeInaccessibleMessage instanceof Message))
        {
            return false;
        }

        final Message message = (Message)maybeInaccessibleMessage;
        if(!message.hasText())
        {
            return false;
        }

        final String text = message.getText();
        if(!text.equals(initialMessage))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean getIsVoice(final Update update)
    {
        if((update == null) ||
           (!update.hasMessage()) ||
           (!update.getMessage().hasVoice()))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private void handleCommand(
        final String commandAsString,
        final Long chatId,
        final Long userId)
        throws SQLException
    {
        Command command = null;
        try
        {
            command = CommandParser.parse(commandAsString);
        }
        catch(final IllegalArgumentException e)
        {
            return;
        }

        switch(command)
        {
            case Command.START ->
            {
                try
                {
                    onStartReceive(chatId, userId);
                }
                catch(final SQLException e)
                {
                    throw e;
                }
            }

            case Command.STATS ->
            {
                try
                {
                    onStatsReceive(chatId, userId);
                }
                catch(final SQLException e)
                {
                    throw e;
                }
            }

            case Command.HELP ->
            {
                try
                {
                    onHelpReceive(chatId, userId);
                }
                catch(final SQLException e)
                {
                    throw e;
                }
            }

            case Command.CANCEL ->
            {
                try
                {
                    onCancelReceive(chatId, userId);
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

    protected void onStartReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        //Query DB and prepare message.
        List<AudioClass> audioClasses = null;
        try
        {
            audioClasses = databaseManager.getAudioClasses();
        }
        catch(final SQLException e)
        {
            throw e;
        }

        final List<String> audioClassesHumanReadable = new ArrayList<>();
        final List<String> audioClassesAsString = new ArrayList<>();
        for(final AudioClass audioClass : audioClasses)
        {
            final String audioClassHumanReadable = audioClass.toString();
            audioClassesHumanReadable.add(audioClassHumanReadable);

            final String audioClassAsString = AudioClassMapper.map(audioClass);
            audioClassesAsString.add(audioClassAsString);
        }

        //Send message to telegram user.
        telegramCommunicationManager.sendMessage(
            chatId,
            TelegramCommunicationManager.MESSAGE_CHOOSE,
            audioClassesHumanReadable,
            audioClassesAsString,
            false);

        //Change state.
        try
        {
            databaseManager.updateDialogueStateAndAudioClass(
                userId,
                DialogueState.CHOOSE,
                null);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    protected void onStatsReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        //Query DB and prepare message.
        Map<AudioClass, Long> voiceCount = null;
        try
        {
            voiceCount = databaseManager.getVoiceCount(userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }

        long totalVoiceCountForCurrentUser = 0;

        long totalVoiceCountForAllUsers = 0;
        try
        {
            totalVoiceCountForAllUsers = databaseManager.getTotalVoiceCount();
        }
        catch(final SQLException e)
        {
            throw e;
        }

        final StringBuilder stringBuilderEng = new StringBuilder()
            .append("Recorded voice messages count.\n");
        final StringBuilder stringBuilderRus = new StringBuilder()
            .append("Количество записанных голосовых сообщений.\n");

        for(Map.Entry<AudioClass, Long> mapEntry: voiceCount.entrySet())
        {
            final AudioClass audioClass = mapEntry.getKey();
            final Long count = mapEntry.getValue();
            totalVoiceCountForCurrentUser += count;

            final String audioClassAsString = AudioClassMapper.map(audioClass);
            if(audioClassAsString != null)
            {
                stringBuilderEng
                    .append(audioClassAsString)
                    .append(": ")
                    .append(count)
                    .append("\n");
                stringBuilderRus
                    .append(audioClassAsString)
                    .append(": ")
                    .append(count)
                    .append("\n");
            }
        }

        stringBuilderEng
            .append("Total for you: ")
            .append(totalVoiceCountForCurrentUser)
            .append("\n");
        stringBuilderRus
            .append("Общее количество для вас: ")
            .append(totalVoiceCountForCurrentUser)
            .append("\n");

        stringBuilderEng
            .append("Total for all users: ")
            .append(totalVoiceCountForAllUsers)
            .append("\n\n");
        stringBuilderRus
            .append("Общее количество для всех пользователей: ")
            .append(totalVoiceCountForAllUsers);

        final String stringRus = stringBuilderRus.toString();
        final String voiceCountMessage = stringBuilderEng
            .append(stringRus)
            .toString();

        //Send message to telegram user.
        telegramCommunicationManager.sendMessage(
            chatId,
            voiceCountMessage,
            null,
            null,
            false);

        //Change state.
        try
        {
            databaseManager.updateDialogueStateAndAudioClass(
                userId,
                DialogueState.START,
                null);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    protected void onHelpReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send message to telegram user.
        telegramCommunicationManager.sendMessage(
            chatId,
            TelegramCommunicationManager.MESSAGE_HELP,
            null,
            null,
            false);

        //Change state.
        try
        {
            databaseManager.updateDialogueStateAndAudioClass(
                userId,
                DialogueState.START,
                null);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    protected void onCancelReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send message to telegram user.
        telegramCommunicationManager.sendMessage(
            chatId,
            TelegramCommunicationManager.MESSAGE_CANCEL,
            null,
            null,
            false);

        //Change state.
        try
        {
            databaseManager.updateDialogueStateAndAudioClass(
                userId,
                DialogueState.START,
                null);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    protected void handleCallbackQuery(
        final CallbackQuery callbackQuery,
        final Long chatId,
        final Long userId)
        throws SQLException, TelegramApiException, IOException
    {
        //Answer to callback query of telegram user.
        final String callbackQueryId = callbackQuery.getId();
        telegramCommunicationManager.answerCallbackQuery(callbackQueryId);
    }

    protected void handleCallbackQueryWithChosenAudioClass(
        final String audioClassAsString,
        final Long chatId,
        final Long userId)
        throws SQLException
    {
        final AudioClass audioClass = AudioClassMapper.map(audioClassAsString);
        if(audioClass == null)
        {
            return;
        }

        //Send "typing..." to telegram user.
        telegramCommunicationManager.sendChatAction(
            chatId,
            TelegramCommunicationManager.CHAT_ACTION_TYPING);

        //Query DB and prepare message.
        int maxDurationSeconds = 0;
        try
        {
            maxDurationSeconds = databaseManager.getMaxDuration(audioClass);
        }
        catch(final SQLException e)
        {
            throw e;
        }

        //Send message to telegram user.
        final String messageRecord = String.format(
            TelegramCommunicationManager.MESSAGE_RECORD_FORMAT,
            maxDurationSeconds);
        telegramCommunicationManager.sendMessage(
            chatId,
            messageRecord,
            null,
            null,
            true);

        //Change state.
        try
        {
            databaseManager.updateDialogueStateAndAudioClass(
                userId,
                DialogueState.RECORD,
                audioClass);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    private void handleCallbackQueryWithCheckResult(
        final CallbackQuery callbackQuery)
    {

    }

    protected void handleVoice(
        final Voice voice,
        final Long chatId,
        final Long userId)
        throws SQLException
    {

    }

    protected void handleVoiceWithCorrectDuration(
        final Long chatId,
        final Long userId)
        throws SQLException
    {
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
        telegramCommunicationManager.sendMessage(
            chatId,
            TelegramCommunicationManager.MESSAGE_CHECK,
            answersHumanReadable,
            answersAsString,
            false);

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

    protected void handleGarbage(final Long chatId, final Long userId)
        throws SQLException
    {

    }
}