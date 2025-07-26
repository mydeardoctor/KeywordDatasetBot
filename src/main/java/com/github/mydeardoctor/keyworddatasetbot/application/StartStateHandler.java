package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.telegramuser.TelegramUserCommunicationManager;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StartStateHandler extends StateHandler
{
    public StartStateHandler(
        final DatabaseManager databaseManager,
        final TelegramUserCommunicationManager telegramUserCommunicationManager)
    {
        super(
            databaseManager,
            telegramUserCommunicationManager,
            LoggerFactory.getLogger(StartStateHandler.class));
    }

    @Override
    protected boolean getIsExpectedCallbackQuery(Update update)
    {
        return false;
    }

    @Override
    protected boolean getIsExpectedVoice(Update update)
    {
        return false;
    }
}
