package com.github.mydeardoctor.keyworddatasetbot.updates;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.domain.DialogueState;
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

public class UpdateHandlingJob implements Runnable
{
    private final Update update;
    private final CommonResourcesManager commonResourcesManager;
    private final DatabaseManager databaseManager;
    private final TelegramClient telegramClient;

    private final Logger logger;

    public UpdateHandlingJob(
        final Update update,
        final CommonResourcesManager commonResourcesManager,
        final DatabaseManager databaseManager,
        final TelegramClient telegramClient)
    {
        super();

        this.update = update;
        this.commonResourcesManager = commonResourcesManager;
        this.databaseManager = databaseManager;
        this.telegramClient = telegramClient;

        logger = LoggerFactory.getLogger(UpdateHandlingJob.class);
    }

    @Override
    public void run()
    {
        Long userId = null;

        try
        {
            logger.debug(
                    "Thread: group = {}, name = {}, priority = {}.",
                    Thread.currentThread().getThreadGroup().getName(),
                    Thread.currentThread().getName(),
                    Thread.currentThread().getPriority());

            //TODO РЕФАКТОРИНГ. Делаю минимал репродюсибл экзампл.
            //Handle update.
            if((update != null) && (update.hasMessage()))
            {
                userId = update.getMessage().getFrom().getId();
                handleUpdate(update);
            }
        }
        catch(final Exception e)
        {
            logger.error("Exception in thread pool!", e);
        }
        finally
        {
            commonResourcesManager.finishHandlingUpdate(userId);
        }
    }

    //TODO вынести в отдельный business logic пакет
    private void handleUpdate(final Update update)
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
        //TODO application level logic
        //TODO application level exception handler
        //Get dialogue state for this user.
        try
        {
            final DialogueState dialogueState =
                databaseManager.getDialogueState(userId);
        }
        catch(final SQLException e)
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
                .chatId(message.getChatId())
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

            final String errorMessage = "SQL Exception!";
            logger.error(errorMessage, e);

            return;
        }
    }
}
