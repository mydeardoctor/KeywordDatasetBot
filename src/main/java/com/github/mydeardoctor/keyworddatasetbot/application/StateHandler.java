package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.domain.*;
import com.github.mydeardoctor.keyworddatasetbot.telegramuser.TelegramUserCommunicationManager;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

    public void handleUpdate(final Update update)
        throws SQLException, IllegalArgumentException
    {
        final boolean isValidUpdate  = getIsValidUpdate(update);
        if(isValidUpdate == false)
        {
            return;
        }

        final Message message = update.getMessage();
        final Long chatId = message.getChatId();
        final User user = message.getFrom();
        final Long userId = user.getId();

        //TODO при неправильном вводе выводить подсказку?
        final boolean isValidCommand = getIsValidCommand(message);
        if(isValidCommand)
        {
            final String commandAsString = message.getText();
            final Command command = CommandParser.parse(commandAsString);
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
                    final String errorMessage = "Invalid command!";
                    throw new IllegalArgumentException(errorMessage);
                }
            }
            return;
        }

        //TODO не команды
    }

    //TODO переделать, т.к. может прийти не только message, но и коллбек query
    private static boolean getIsValidUpdate(final Update update)
    {
        if((update == null) || (!update.hasMessage()))
        {
            return false;
        }

        final Message message = update.getMessage();
        final User user = message.getFrom();
        if(user == null)
        {
            return false;
        }

        return true;
    }

    private static boolean getIsValidCommand(final Message message)
    {
        if((message == null) || (!message.hasText()) || (!message.isCommand()))
        {
            return false;
        }

        Command command = null;
        try
        {
            command = CommandParser.parse(message.getText());
        }
        catch(final IllegalArgumentException e)
        {
            return false;
        }

        return true;
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
}