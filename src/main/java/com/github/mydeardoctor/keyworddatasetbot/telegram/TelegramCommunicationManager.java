package com.github.mydeardoctor.keyworddatasetbot.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TelegramCommunicationManager
{
    private final TelegramClient telegramClient;

    public static final String MESSAGE_REMIND =
        """
        Hello there!
        Please record a couple of voice messages when you have the time.
        Thank you very much!
        
        Приветик!
        Пожалуйста, запишите парочку голосовых сообщений, когда будет удобно.
        Спасибо большое!""";

    public static final String MESSAGE_CHOOSE =
        """
        Choose keyword to record:
        
        Выберите ключевое слово для записи:""";

    public static final String MESSAGE_RECORD_FORMAT =
        """
        Record a voice message saying the chosen keyword. The voice message should contain only the keyword itself and nothing else. The voice message should be no more than <strong>%1$d</strong> seconds long.
        
        Запишите голосовое сообщение, в котором произносите выбранное ключевое слово. В голосовом сообщении должно содержаться только произнесённое вами ключевое слово и ничего лишнего. Голосовое сообщение должно быть не более <strong>%1$d</strong> секунд.""";

    public static final String MESSAGE_VOICE_IS_TOO_LONG_FORMAT =
        """
        Your recorded voice message is longer than <strong>%1$d</strong> seconds!
        Please, try again.
        
        Записанное вами голосовое сообщение дольше <strong>%1$d</strong> секунд!
        Пожалуйста, попробуйте ещё раз.""";

    public static final String MESSAGE_CHECK =
        """
        Please, listen to your recorded voice message. Are you sure it is correct?
        
        Пожалуйста, послушайте записанное вами голосовое сообщение. Вы уверены, что оно получилось?""";

    public static final String MESSAGE_THANK_YOU =
        """
        Thank you!
        
        Спасибо!""";

    public static final String MESSAGE_HELP =
        """
        This telegram bot collects audio dataset of keywords.
        
        The bot presents you a list of keywords and asks you to choose one. You choose one keyword from the list and then record a voice message saying that keyword. The voice message should contain only the keyword itself and nothing else. The voice message is then saved on the server.
        
        The purpose of this bot is to collect a large audio dataset of these keywords in a semi-automated way. The collected audio dataset will later be used to train a keyword spotting neural net model. The model will recognize a specific keyword from speech and react to it. The final model will be used for fun, probably for cosplay.
        
        The collected audio dataset must be as big as possible. Please, record as much voice messages per keyword as you can.
        
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
        
        Собранный аудио-датасет должен быть очень большим. Пожалуйста, запишите как можно больше голосовых сообщений для каждого ключевого слова.
        
        Доступные команды:
        
        /start - Начать процесс записи голосового сообщения. Бот предоставляет список ключевых слов. Вы записываете голосовое сообщение, в котором произносите это ключевое слово.
        
        /stats - Показать статистику:
        Количество записанных вами голосовых сообщений для каждого ключевого слова.
        Общее количество записанных вами голосовых сообщений.
        Общее количество записанных голосовых сообщений для всех пользователей.
        
        /help - Показать это сообщение с подсказкой.
        
        /cancel - Отменить текущую операцию.""";

    public static final String MESSAGE_CANCEL =
        """
        Operation cancelled.
        
        Операция отменена.""";

    public static final String MESSAGE_ERROR =
        """
        Error on server! Please, try again.
        Contact admin or technical support and provide this stack trace:
        
        Ошибка на сервере! Пожалуйста, попробуйте ещё раз.
        Свяжитесь с администратором или технической поддержкой и предоставьте трассировку стека:
            
        """;

    public static final String CHAT_ACTION_TYPING = "typing";

    private final Logger logger;

    public TelegramCommunicationManager(
        final TelegramClient telegramClient)
    {
        super();

        this.telegramClient = telegramClient;
        logger = LoggerFactory.getLogger(
            TelegramCommunicationManager.class);
    }

    public void sendMessage(
        final Long chatId,
        final String message,
        final List<String> buttonsText,
        final List<String> buttonsCallbackData,
        final boolean parseAsHtml)
    {
        final SendMessage.SendMessageBuilder<?, ?> sendMessageBuilder
            = SendMessage.builder();

        sendMessageBuilder
            .chatId(chatId)
            .text(message);

        if((buttonsText != null) &&
           (!buttonsText.isEmpty()) &&
           (buttonsCallbackData != null) &&
           (!buttonsCallbackData.isEmpty()) &&
           (buttonsText.size() == buttonsCallbackData.size()))
        {
            final List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
            for(int i = 0; i < buttonsText.size(); ++i)
            {
                final String buttonText = buttonsText.get(i);
                final String buttonCallbackData = buttonsCallbackData.get(i);

                final InlineKeyboardButton inlineKeyboardButton
                    = InlineKeyboardButton
                        .builder()
                        .text(buttonText)
                        .callbackData(buttonCallbackData)
                        .build();

                final InlineKeyboardRow inlineKeyboardRow =
                    new InlineKeyboardRow(inlineKeyboardButton);
                keyboardRows.add(inlineKeyboardRow);
            }

            final InlineKeyboardMarkup inlineKeyboardMarkup
                = InlineKeyboardMarkup
                    .builder()
                    .keyboard(keyboardRows)
                    .build();
            sendMessageBuilder.replyMarkup(inlineKeyboardMarkup);
        }

        if(parseAsHtml)
        {
            sendMessageBuilder.parseMode("HTML");
        }

        final SendMessage sendMessageMethod = sendMessageBuilder.build();

        try
        {
            telegramClient.execute(sendMessageMethod);
        }
        catch(final TelegramApiException e)
        {
            final String errorMessage =
                "Could not send message to telegram user!";
            logger.error(errorMessage, e);
        }
    }

    public void sendChatAction(final Long chatId, final String chatAction)
    {
        final SendChatAction sendChatActionMethod = SendChatAction
            .builder()
            .chatId(chatId)
            .action(chatAction)
            .build();
        try
        {
            telegramClient.execute(sendChatActionMethod);
        }
        catch(final TelegramApiException e)
        {
            final String errorMessage =
                "Could not send char action to telegram user!";
            logger.error(errorMessage, e);
        }
    }

    public void answerCallbackQuery(final String callbackQueryId)
    {
        final AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery
            .builder()
            .callbackQueryId(callbackQueryId)
            .build();
        try
        {
            telegramClient.execute(answerCallbackQuery);
        }
        catch(final TelegramApiException e)
        {
            final String errorMessage = "Could not answer callback query!";
            logger.error(errorMessage, e);
        }
    }

    public void downloadFile(
        final String fileId,
        final String targetDirectory,
        final String targetSubdirectory,
        final String targetFileName,
        final String targetFileExtension)
        throws TelegramApiException, IOException
    {
        //Get file path from telegram.
        final GetFile getFileMethod = GetFile
            .builder()
            .fileId(fileId)
            .build();
        String filePath = null;
        try
        {
            final File file = telegramClient.execute(getFileMethod);
            filePath = file.getFilePath();
        }
        catch(final TelegramApiException e)
        {
            throw e;
        }

        // Create subdirectory on disk if needed.
        final Path targetDirectoryPath = Path.of(targetDirectory);
        final Path targetSubdirectoryPath =
            targetDirectoryPath.resolve(targetSubdirectory);
        final boolean exists = Files.exists(targetSubdirectoryPath);
        if(!exists)
        {
            try
            {
                final Set<PosixFilePermission> posixPermissions =
                    PosixFilePermissions.fromString("rwx------");
                final FileAttribute<Set<PosixFilePermission>> fileAttribute =
                    PosixFilePermissions.asFileAttribute(posixPermissions);
                Files.createDirectories(targetSubdirectoryPath, fileAttribute);
            }
            catch(final IOException e)
            {
                throw e;
            }
        }
        final String targetFileNameWithExtension =
            targetFileName + targetFileExtension;
        final Path targetFilePath =
            targetSubdirectoryPath.resolve(targetFileNameWithExtension);

        // Download file.
        try(final InputStream inputStream =
                telegramClient.downloadFileAsStream(filePath))
        {
            Files.copy(inputStream, targetFilePath);
            final Set<PosixFilePermission> posixPermissions =
                PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(targetFilePath, posixPermissions);
        }
        catch(final TelegramApiException | IOException e)
        {
            throw e;
        }
    }
}