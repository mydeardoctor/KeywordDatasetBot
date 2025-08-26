package com.github.mydeardoctor.keyworddatasetbot.telegram;

import com.github.mydeardoctor.keyworddatasetbot.resources.ResourceLoader;
import com.vdurmont.emoji.EmojiParser;
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
import java.util.*;

public class TelegramCommunicationManager
{
    private final TelegramClient telegramClient;

    public static final String REMIND = "remind";
    public static final String CHOOSE = "choose";
    public static final String RECORD_FORMAT = "record_format";
    public static final String VOICE_IS_TOO_LONG_FORMAT = "voice_is_too_long";
    public static final String CHECK = "check";
    public static final String THANK_YOU = "thank_you";
    public static final String STATS_FORMAT = "stats_format";
    public static final String HELP = "help";
    public static final String ABOUT_FORMAT = "about_format";
    public static final String CANCEL = "cancel";
    public static final String ERROR_FORMAT = "error_format";

    private final Map<String, String> telegramMessages;

    private static final String TELEGRAM_MESSAGE_EXTENSION = ".txt";

    public static final String CHAT_ACTION_TYPING = "typing";

    private final Logger logger;

    public TelegramCommunicationManager(final TelegramClient telegramClient)
        throws IOException, IllegalArgumentException
    {
        super();

        this.telegramClient = telegramClient;

        final String telegramMessagesDirectoryPath = "telegram_messages";

        final Set<String> telegramMessagesFileNames = new HashSet<>();
        telegramMessagesFileNames.add(REMIND);
        telegramMessagesFileNames.add(CHOOSE);
        telegramMessagesFileNames.add(RECORD_FORMAT);
        telegramMessagesFileNames.add(VOICE_IS_TOO_LONG_FORMAT);
        telegramMessagesFileNames.add(CHECK);
        telegramMessagesFileNames.add(THANK_YOU);
        telegramMessagesFileNames.add(STATS_FORMAT);
        telegramMessagesFileNames.add(HELP);
        telegramMessagesFileNames.add(ABOUT_FORMAT);
        telegramMessagesFileNames.add(CANCEL);
        telegramMessagesFileNames.add(ERROR_FORMAT);

        try
        {
            telegramMessages = ResourceLoader.loadStrings(
                telegramMessagesDirectoryPath,
                telegramMessagesFileNames,
                TELEGRAM_MESSAGE_EXTENSION);
        }
        catch(final IOException | IllegalArgumentException e)
        {
            throw e;
        }

        for(Map.Entry<String, String> entry : telegramMessages.entrySet())
        {
            final String fileName = entry.getKey();
            final String message = entry.getValue();
            final String messageWithEmoji = EmojiParser.parseToUnicode(message);
            telegramMessages.put(fileName, messageWithEmoji);
        }

        logger = LoggerFactory.getLogger(TelegramCommunicationManager.class);
    }

    public void sendMessage(
        final Long chatId,
        final String message,
        final List<String> buttonsText,
        final List<String> buttonsCallbackData)
    {
        //TODO parse emoji


        final SendMessage.SendMessageBuilder<?, ?> sendMessageBuilder
            = SendMessage.builder();

        sendMessageBuilder
            .chatId(chatId)
            .text(message)
            .parseMode("HTML");

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

    public String getMessage(final String fileName)
    {
        final String message = telegramMessages.get(fileName);
        return message;
    }
}