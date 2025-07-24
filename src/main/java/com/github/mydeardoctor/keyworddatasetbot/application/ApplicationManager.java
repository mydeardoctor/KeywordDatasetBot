package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.domain.DialogueState;
import com.github.mydeardoctor.keyworddatasetbot.updates.UpdateHandlingJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

//TODO РЕФАКТОРИНГ. Делаю минимал репродюсибл экзампл.
public class ApplicationManager
{
    private final DatabaseManager databaseManager;
    private final TelegramClient telegramClient;
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

        //Get dialogue state for current user.
        DialogueState dialogueState = null;
        try
        {
            dialogueState = databaseManager.getDialogueState(userId);
        }
        catch(final SQLException e)
        {
            handleApplicationLevelException(e, message.getChatId());
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
                handleApplicationLevelException(e, message.getChatId());
                return;
            }
        }
    }

    private void handleApplicationLevelException(
        final Exception e,
        final Long telegramChatId)
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

        final SendMessage sendMessage = SendMessage
            .builder()
            .chatId(telegramChatId)
            .text(serverErrorMessage)
            .build();
        try
        {
            telegramClient.execute(sendMessage);
        }
        catch(final TelegramApiException ex)
        {
            final String errorMessage =
                "Telegram client could not send message!";
            logger.error(errorMessage, ex);
        }

        final String errorMessage = "Application level exception!";
        logger.error(errorMessage, e);
    }
}
