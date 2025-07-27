package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.domain.*;
import com.github.mydeardoctor.keyworddatasetbot.telegramuser.TelegramUserCommunicationManager;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO singleton
//TODO отвечать на inline query
public abstract class StateHandler
{
    protected final DatabaseManager databaseManager;
    protected final TelegramUserCommunicationManager
        telegramUserCommunicationManager;
    private final Logger logger;

    public StateHandler(
        final DatabaseManager databaseManager,
        final TelegramUserCommunicationManager
            telegramUserCommunicationManager,
        final Logger logger)
    {
        super();

        this.databaseManager = databaseManager;
        this.telegramUserCommunicationManager
            = telegramUserCommunicationManager;
        this.logger = logger;
    }

    //TODO access modifiers
    public final void handleUpdate(
        final Update update,
        final Long chatId,
        final Long userId)
        throws SQLException
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
            catch(final SQLException e)
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
        final CallbackQuery callbackQuery)
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

    //TODO при работе с БД отправляем typing

    //TODO в процессе предупредить пользоателя, то надо записать голосовуху со словом и только со словом
    //TODO переопределить в checkStateHandler
    //TODO throws SQL exception
    protected void onStartReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramUserCommunicationManager.sendChatAction(
            chatId,
            TelegramUserCommunicationManager.CHAT_ACTION_TYPING);

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
        telegramUserCommunicationManager.sendMessage(
            chatId,
            TelegramUserCommunicationManager.MESSAGE_CHOOSE,
            audioClassesHumanReadable,
            audioClassesAsString);

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

    //TODO переопределить в checkStateHandler
    protected void onStatsReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramUserCommunicationManager.sendChatAction(
            chatId,
            TelegramUserCommunicationManager.CHAT_ACTION_TYPING);

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
        telegramUserCommunicationManager.sendMessage(
            chatId,
            voiceCountMessage,
            null,
            null);

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

    //TODO переопределить в checkStateHandler
    protected void onHelpReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send message to telegram user.
        telegramUserCommunicationManager.sendMessage(
            chatId,
            TelegramUserCommunicationManager.MESSAGE_HELP,
            null,
            null);

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

    //TODO переопределить в checkStateHandler
    protected void onCancelReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send message to telegram user.
        telegramUserCommunicationManager.sendMessage(
            chatId,
            TelegramUserCommunicationManager.MESSAGE_CANCEL,
            null,
            null);

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
        throws SQLException
    {
        //Answer to callback query of telegram user.
        final String callbackQueryId = callbackQuery.getId();
        telegramUserCommunicationManager.answerCallbackQuery(callbackQueryId);
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
        telegramUserCommunicationManager.sendChatAction(
            chatId,
            TelegramUserCommunicationManager.CHAT_ACTION_TYPING);

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
            TelegramUserCommunicationManager.MESSAGE_RECORD_FORMAT,
            maxDurationSeconds);
        telegramUserCommunicationManager.sendMessage(
            chatId,
            messageRecord,
            null,
            null);

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

    protected void handleGarbage(final Long chatId, final Long userId)
        throws SQLException
    {

    }
}