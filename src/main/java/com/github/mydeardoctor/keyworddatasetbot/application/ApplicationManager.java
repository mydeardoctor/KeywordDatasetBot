package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.delay.DelayManager;
import com.github.mydeardoctor.keyworddatasetbot.domain.*;
import com.github.mydeardoctor.keyworddatasetbot.updates.UpdateHandlingJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.KeyValuePair;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Map;

//TODO РЕФАКТОРИНГ. Делаю минимал репродюсибл экзампл.
public class ApplicationManager
{
    private final DatabaseManager databaseManager;
    private final TelegramClient telegramClient;

    private static final String MESSAGE_INVALID_COMMAND =
    """
    Invalid command! Try again.
    Недопустимая команда! Попробуйте ещё раз.
    """;

    private final Logger logger;

    public ApplicationManager(
        final DatabaseManager databaseManager,
        final TelegramClient telegramClient)
    {
        super();

        this.databaseManager = databaseManager;
        this.telegramClient = telegramClient;
        logger = LoggerFactory.getLogger(ApplicationManager.class);
    }

    public void handleUpdate(final Update update)
    {
        if((update == null) || (!update.hasMessage()))
        {
            return;
        }

        final Message message = update.getMessage();
        final User user = message.getFrom();
        if(user == null)
        {
            return;
        }

        final Long userId = user.getId();
        final Long chatId = message.getChatId();

        //Get dialogue state for current user.
        DialogueState dialogueState = null;
        try
        {
            dialogueState = databaseManager.getDialogueState(userId);
        }
        catch(final SQLException e)
        {
            handleApplicationLevelException(chatId, e);
            return;
        }

        //If dialogue state for current user is not in the database
        //then current user is not in the database.
        if(dialogueState == null)
        {
            //Save current user in the database.
            try
            {
                databaseManager.saveUser(
                    userId,
                    user.getUserName(),
                    user.getFirstName(),
                    user.getLastName(),
                    DialogueState.START,
                    null);
            }
            catch(final SQLException e)
            {
                handleApplicationLevelException(chatId, e);
                return;
            }

            //Get dialogue state for current user again.
            try
            {
                dialogueState = databaseManager.getDialogueState(userId);
            }
            catch(final SQLException e)
            {
                handleApplicationLevelException(chatId, e);
                return;
            }
            if(dialogueState == null)
            {
                final String errorMessage =
                    "Could not get dialogue state for existing user!";
                handleApplicationLevelException(
                    chatId, new SQLException(errorMessage));
                return;
            }
        }

        //State machine.
        if(message.isCommand())
        {
            if(!message.hasText())
            {
                sendMessage(chatId, MESSAGE_INVALID_COMMAND);
                return;
            }

            Command command = null;
            try
            {
                command = CommandParser.parse(message.getText());
            }
            catch(final IllegalArgumentException e)
            {
                sendMessage(chatId, MESSAGE_INVALID_COMMAND);
                return;
            }

            /* Message is a command.
               Any command overrides dialogue state. */
            switch(command)
            {
                case Command.START ->
                {
                    //TODO
                    sendMessage(chatId, "started");
                }

                case Command.STATS ->
                {
                    sendTyping(chatId);

                    Map<AudioClass, Long> voiceCount = null;
                    try
                    {
                        voiceCount = databaseManager.getVoiceCount(userId);
                    }
                    catch(final SQLException e)
                    {
                        handleApplicationLevelException(chatId, e);
                        return;
                    }

                    long totalVoiceCountForCurrentUser = 0;

                    long totalVoiceCountForAllUsers = 0;
                    try
                    {
                        totalVoiceCountForAllUsers =
                            databaseManager.getTotalVoiceCount();
                    }
                    catch(final SQLException e)
                    {
                        handleApplicationLevelException(chatId, e);
                        return;
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

                        final String audioClassAsString =
                            AudioClassMapper.map(audioClass);
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

                    sendMessage(chatId, voiceCountMessage);
                }

                case Command.HELP ->
                {
                    //TODO
                    sendMessage(chatId, "helped");
                }

                case Command.CANCEL ->
                {
                    //TODO
                    sendMessage(chatId, "canceled");
                }

                default ->
                {
                    sendMessage(chatId, MESSAGE_INVALID_COMMAND);
                }
            }
        }
        else
        {
            /* Message is not a command.
               Action depends on dialogue state. */
            //TODO
            sendMessage(chatId, "not a command");
        }
    }

    private void sendMessage(final Long chatId, final String message)
    {
        final SendMessage sendMessageMethod = SendMessage
            .builder()
            .chatId(chatId)
            .text(message)
            .build();
        try
        {
            telegramClient.execute(sendMessageMethod);
        }
        catch(final TelegramApiException e)
        {
            final String errorMessage =
                "Telegram client could not send message!";
            logger.error(errorMessage, e);
        }
    }

    //TODO абстрагировать telegrmClient execute вместе с exception
    private void sendTyping(final Long chatId)
    {
        final SendChatAction sendChatAction = SendChatAction
            .builder()
            .chatId(chatId)
            .action("typing")
            .build();
        try
        {
            telegramClient.execute(sendChatAction);
        }
        catch(final TelegramApiException e)
        {
            final String errorMessage =
                "Telegram client could not send chat action!";
            logger.error(errorMessage, e);
        }
    }

    private void handleApplicationLevelException(
        final Long chatId,
        final Exception e)
    {
        String stackTrace = "";
        try(final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter))
        {
            e.printStackTrace(printWriter);
            printWriter.flush();
            stackTrace = stringWriter.toString();
        }
        catch(final IOException ex)
        {
            final String errorMessage = "Could not close StringWriter!";
            logger.error(errorMessage, ex);
        }

        final String serverErrorMessage =
            """
            Error on server! Please, try again.
            Ошибка на сервере! Пожалуйста, попробуйте ещё раз.
    
            Contact admin or technical support and provide this stack trace:
            Свяжитесь с администратором или технической поддержкой и предоставьте трассировку стека:
                
            """
            + stackTrace;
        sendMessage(chatId, serverErrorMessage);

        final String errorMessage = "Application level exception!";
        logger.error(errorMessage, e);
    }
}