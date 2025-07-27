package com.github.mydeardoctor.keyworddatasetbot.telegramuser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

//TODO singleton
public class TelegramUserCommunicationManager
{
    private final TelegramClient telegramClient;

    //TODO добавить emoji
    public static final String MESSAGE_CHOOSE =
        """
        Choose keyword to record:
        Выберите ключевое слово для записи:""";

    //TODO абстрагировать 2 секунды.
    public static final String MESSAGE_RECORD_FORMAT =
        """
        Record a voice message saying the chosen keyword. The voice message should contain only the keyword itself and nothing else. The voice message should be no more than %1$d seconds long.
        
        Запишите голосовое сообщение, в котором произносите выбранное ключевое слово. В голосовом сообщении должно содержаться только произнесённое вами ключевое слово и ничего лишнего. Голосовое сообщение должно быть не более %1$d секунд.""";

    //TODO ссылка не меня в телеге
    //TODO лого от Леры, ссылка на неё на гитхабе
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

    public TelegramUserCommunicationManager(
        final TelegramClient telegramClient)
    {
        super();

        this.telegramClient = telegramClient;
        logger = LoggerFactory.getLogger(
            TelegramUserCommunicationManager.class);
    }

    public void sendMessage(
        final Long chatId,
        final String message,
        final List<String> buttonsText,
        final List<String> buttonsCallbackData)
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
}