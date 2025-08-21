package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class AnswerMapper
{
    private AnswerMapper()
    {
        super();
    }

    public static Answer map(final String answerAsString)
    {
        return EnumMapper.map(answerAsString, Answer.class);
    }

    public static String map(final Answer answer)
    {
        return EnumMapper.map(answer);
    }
}
