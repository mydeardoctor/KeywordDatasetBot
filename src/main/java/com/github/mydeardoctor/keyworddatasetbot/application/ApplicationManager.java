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
//TODO separate thread for periodic notification fo all users. how to get chatid from userid. save it in DB
public class ApplicationManager
{
    private final DatabaseManager databaseManager;
    private final TelegramClient telegramClient;

    //TODO вынести текст куда-то в другой класс для текста
    private static final String MESSAGE_INVALID_COMMAND =
    """
    Invalid command! Try again.
    Недопустимая команда! Попробуйте ещё раз.
    """;

    //TODO добавить emoji в help, statistics
    private static final String MESSAGE_HELP =
    """
    This telegram bot collects audio dataset of keywords.
    
    The bot presents you a list of keywords and asks you to choose one. You choose one keyword from the list and then record a voice message saying that keyword. The voice message should contain only the keyword itself and nothing else. The voice message is then saved on the server.
    
    The purpose of this bot is to collect a large audio dataset of these keywords in a semi-automated way. The collected audio dataset will later be used to train a keyword spotting neural net model. The model will recognize a specific keyword from speech and react to it. The final model will be used for fun, probably for cosplay.
    
    Available commands:
    
    /start - Start recording voice process. The bot presents you a list of keywords. You record a voice message saying that keyword.
    
    /stats - Show statistics:
    Your count of recorded voice messages per keyword.
    Your total count of recorded voice messages.
    Total count of recorded voice messages for all users.
    
    /help - Show this help message.
    
    /cancel - Cancel ongoing operation.
    
    
    Этот телеграм-бот собирает аудио-датасет ключевых слов.
    
    Бот предоставляет список ключевых слов и просит выбрать одно из них. Вы выбираете одно ключевое слово из списка и записываете голосовое сообщение, в котором произносите это ключевое слово. В голосовом сообщении должно содержаться только произнесённое вами ключевое слово и ничего лишнего. Затем голосовое сообщение сохраняется на сервер.
    
    Цель этого бота - собрать большой аудио-датасет ключевых слов в полуавтоматическом режиме. Собранный аудио-датасет позже будет использован для тренировки нейросети. Нейросеть будет распознавать ключевые слова из человеческой речи и реагировать на них. Итоговая нейросеть будет использована в развлекательных целях, скорее всего для косплея.
    
    Доступные команды:
    
    /start - Начать процесс записи голосового сообщения. Бот предоставляет список ключевых слов. Вы записываете голосовое сообщение, в котором произносите это ключевое слово.
    
    /stats - Показать статистику:
    Количество записанных вами голосовых сообщений для каждого ключевого слова.
    Общее количество записанных вами голосовых сообщений.
    Общее количество записанных голосовых сообщений для всех пользователей.
    
    /help - Показать это сообщение с подсказкой.
    
    /cancel - Отменить текущую операцию.
    """;

    private static final String MESSAGE_CANCEL =
    """
    Operation cancelled.
    Операция отменена.
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

        final Long chatId = message.getChatId();
        final Long userId = user.getId();

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

        //TODO при неправильном вводе выводить подсказку?
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
                    onStartReceive(chatId);
                }

                case Command.STATS ->
                {
                    onStatsReceive(chatId, userId);
                }

                case Command.HELP ->
                {
                    onHelpReceive(chatId, userId);
                }

                case Command.CANCEL ->
                {
                    onCancelReceive(chatId, userId);
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

    private void enterStart(final Long chatId, final Long userId)
    {
        try
        {
            databaseManager.updateDialogueStateAndAudioClass(
                userId,
                DialogueState.START,
                null);
        }
        catch(final SQLException e)
        {
            handleApplicationLevelException(chatId, e);
        }
    }

    //TODO в процессе предупредить пользоателя, то надо записать голосовуху со словом и только со словом
    private void onStartReceive(final Long chatId)
    {
        //TODO
        sendMessage(chatId, "started");
    }

    private void onStatsReceive(final Long chatId, final Long userId)
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

        enterStart(chatId, userId);
    }

    private void onHelpReceive(final Long chatId, final Long userId)
    {
        //TODO
        sendMessage(chatId, MESSAGE_HELP);

        enterStart(chatId, userId);
    }

    private void onCancelReceive(final Long chatId, final Long userId)
    {
        //TODO
        sendMessage(chatId, MESSAGE_CANCEL);

        enterStart(chatId, userId);
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