package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.domain.*;
import com.github.mydeardoctor.keyworddatasetbot.telegramuser.TelegramUserCommunicationManager;
import com.sun.jdi.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.InaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

//TODO скрипт создания client user. создать папки для audio, логов (сделать доступной для остальных для чтения)
//TODO maven пакует скрипты в релизный архив
//TODO РЕФАКТОРИНГ. Делаю минимал репродюсибл экзампл.
//TODO separate thread for periodic notification fo all users. how to get chatid from userid. save it in DB
public class ApplicationManager
{
    private final DatabaseManager databaseManager;
    private final TelegramUserCommunicationManager
        telegramUserCommunicationManager;
    private final EnumMap<DialogueState, StateHandler> stateHandlers;

    private final Logger logger;

    //TODO методы actionStart actiobStats actionHelp. Абстрагировать их в стостояния с полиморфизмом?
    //TODO singletones

    public ApplicationManager(
        final DatabaseManager databaseManager,
        final TelegramUserCommunicationManager telegramUserCommunicationManager)
    {
        super();

        this.databaseManager = databaseManager;
        this.telegramUserCommunicationManager
            = telegramUserCommunicationManager;

        //TODO почему они приходят не из конструктора?
        stateHandlers = new EnumMap<>(DialogueState.class);
        final StartStateHandler startStateHandler
            = new StartStateHandler(
                databaseManager,
                telegramUserCommunicationManager);
        final ChooseStateHandler chooseStateHandler
            = new ChooseStateHandler(
                databaseManager,
                telegramUserCommunicationManager);
        final RecordStateHandler recordStateHandler
            = new RecordStateHandler(
                databaseManager,
                telegramUserCommunicationManager);
        final CheckStateHandler checkStateHandler
            = new CheckStateHandler(
                databaseManager,
                telegramUserCommunicationManager);
        stateHandlers.put(DialogueState.START, startStateHandler);
        stateHandlers.put(DialogueState.CHOOSE, chooseStateHandler);
        stateHandlers.put(DialogueState.RECORD, recordStateHandler);
        stateHandlers.put(DialogueState.CHECK, checkStateHandler);

        logger = LoggerFactory.getLogger(ApplicationManager.class);
    }

    public void handleUpdate(final Update update)
    {
        final boolean isValidUpdate = UpdateUtilities.getIsValid(update);
        if(!isValidUpdate)
        {
            return;
        }

        final Long chatId = UpdateUtilities.getChatId(update);
        final User user = UpdateUtilities.getUser(update);
        final Long userId = user.getId();

        //Query DB for current state.
        DialogueState dialogueState = null;
        try
        {
            dialogueState = getDialogueState(user);
        }
        catch(final SQLException e)
        {
            handleApplicationLevelException(chatId, e);
            return;
        }

        // Get corresponding state handler.
        final StateHandler stateHandler = stateHandlers.get(dialogueState);
        if(stateHandler == null)
        {
            final String errorMessage = "State handler is null!";
            handleApplicationLevelException(
                chatId,
                new NullPointerException(errorMessage));
            return;
        }

        // Handle update in corresponding state handler.
        try
        {
            stateHandler.handleUpdate(
                update,
                chatId,
                userId);
        }
        catch(final SQLException e)
        {
            handleApplicationLevelException(chatId, e);
        }
    }

    private DialogueState getDialogueState(final User user)
        throws SQLException
    {
        final Long userId = user.getId();

        //Get dialogue state for current user.
        DialogueState dialogueState = null;
        try
        {
            dialogueState = databaseManager.getDialogueState(userId);
        }
        catch(final SQLException e)
        {
            throw e;
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
                    user.getLastName());
            }
            catch(final SQLException e)
            {
                throw e;
            }

            //Get dialogue state for current user again.
            try
            {
                dialogueState = databaseManager.getDialogueState(userId);
            }
            catch(final SQLException e)
            {
                throw e;
            }
            if(dialogueState == null)
            {
                final String errorMessage =
                    "Could not get dialogue state for existing user!";
                throw new SQLException(errorMessage);
            }
        }

        return dialogueState;
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

        final String errorMessageWithStackTrace =
            TelegramUserCommunicationManager.MESSAGE_ERROR + stackTrace;
        telegramUserCommunicationManager.sendMessage(
            chatId,
            errorMessageWithStackTrace,
            null,
            null);

        final String errorMessage = "Application level exception!";
        logger.error(errorMessage, e);
    }
}