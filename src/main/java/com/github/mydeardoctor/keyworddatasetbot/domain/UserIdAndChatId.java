package com.github.mydeardoctor.keyworddatasetbot.domain;

public class UserIdAndChatId
{
    private final Long userId;
    private final Long chatId;

    public UserIdAndChatId(final Long userId, final Long chatId)
    {
        super();

        this.userId = userId;
        this.chatId = chatId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public Long getChatId()
    {
        return chatId;
    }
}
