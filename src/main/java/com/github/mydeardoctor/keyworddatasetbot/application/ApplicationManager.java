package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.*;
import com.github.mydeardoctor.keyworddatasetbot.domain.*;
import com.github.mydeardoctor.keyworddatasetbot.telegram.TelegramCommunicationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;

public class ApplicationManager
{
    private final TelegramUserRepository telegramUserRepository;
    private final AudioClassRepository audioClassRepository;
    private final VoiceRepository voiceRepository;
    private final TelegramCommunicationManager
        telegramCommunicationManager;
    private final EnumMap<DialogueState, StateHandler> stateHandlers;

    private final Logger logger;

    public ApplicationManager(
        final TelegramUserRepository telegramUserRepository,
        final AudioClassRepository audioClassRepository,
        final VoiceRepository voiceRepository,
        final TelegramCommunicationManager telegramCommunicationManager,
        final String appAudioDirectory,
        final String voiceExtension)
    {
        super();

        this.telegramUserRepository = telegramUserRepository;
        this.audioClassRepository = audioClassRepository;
        this.voiceRepository = voiceRepository;
        this.telegramCommunicationManager
            = telegramCommunicationManager;

        //TODO почему они приходят не из конструктора?
        stateHandlers = new EnumMap<>(DialogueState.class);
        final StartStateHandler startStateHandler
            = new StartStateHandler(
                telegramUserRepository,
                audioClassRepository,
                voiceRepository,
                telegramCommunicationManager,
                appAudioDirectory,
                voiceExtension);
        final ChooseStateHandler chooseStateHandler
            = new ChooseStateHandler(
                telegramUserRepository,
                audioClassRepository,
                voiceRepository,
                telegramCommunicationManager,
                appAudioDirectory,
                voiceExtension);
        final RecordStateHandler recordStateHandler
            = new RecordStateHandler(
                telegramUserRepository,
                audioClassRepository,
                voiceRepository,
                telegramCommunicationManager,
                appAudioDirectory,
                voiceExtension);
        final CheckStateHandler checkStateHandler
            = new CheckStateHandler(
                telegramUserRepository,
                audioClassRepository,
                voiceRepository,
                telegramCommunicationManager,
                appAudioDirectory,
                voiceExtension);
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
            dialogueState = getDialogueState(user, chatId);
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
        catch(final SQLException | TelegramApiException | IOException e)
        {
            handleApplicationLevelException(chatId, e);
        }
    }

    public void remindUsers()
    {
        boolean usersRemaining = true;
        Long lastUserId = -1L;

        while(usersRemaining)
        {
            List<UserIdAndChatId> userAndChatIds = null;
            try
            {
                userAndChatIds =
                    telegramUserRepository.getUserAndChatIds(lastUserId);
            }
            catch(final SQLException e)
            {
                final String errorMessage =
                    "Could not get chat ids from database!";
                logger.error(errorMessage, e);
            }

            if((userAndChatIds != null) && (!(userAndChatIds.isEmpty())))
            {
                for(final UserIdAndChatId userIdAndChatId : userAndChatIds)
                {
                    final Long userId = userIdAndChatId.getUserId();
                    final Long chatId = userIdAndChatId.getChatId();

                    lastUserId = userId;

                    final String message =
                        telegramCommunicationManager.getMessage(
                            TelegramCommunicationManager.REMIND);
                    telegramCommunicationManager.sendMessage(
                        chatId,
                        message,
                        null,
                        null);
                }

                userAndChatIds.clear();
            }
            else
            {
                usersRemaining = false;
            }
        }
    }

    private DialogueState getDialogueState(final User user, final Long chatId)
        throws SQLException
    {
        final Long userId = user.getId();

        //Get dialogue state for current user.
        DialogueState dialogueState = null;
        try
        {
            dialogueState = telegramUserRepository.getDialogueState(userId);
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
                telegramUserRepository.saveUser(
                    userId,
                    user.getUserName(),
                    user.getFirstName(),
                    user.getLastName(),
                    chatId);
            }
            catch(final SQLException e)
            {
                throw e;
            }

            //Get dialogue state for current user again.
            try
            {
                dialogueState = telegramUserRepository.getDialogueState(userId);
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

        final String message =
            telegramCommunicationManager.getMessage(
                TelegramCommunicationManager.ERROR_FORMAT);
        final String errorMessageWithStackTrace = String.format(
            message,
            stackTrace);
        telegramCommunicationManager.sendMessage(
            chatId,
            errorMessageWithStackTrace,
            null,
            null);

        final String errorMessage = "Application level exception!";
        logger.error(errorMessage, e);
    }
}