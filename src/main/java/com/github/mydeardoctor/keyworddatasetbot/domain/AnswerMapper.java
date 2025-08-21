package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class AnswerMapper extends EnumMapper
{
    private AnswerMapper()
    {
        super();
    }

    public static Answer fromString(final String answerAsString)
    {
        return mapFromString(answerAsString, Answer.class);
    }

    //TODO null exception
    public static String toString(final Answer answer)
    {
        return mapToString(answer);
    }
}
