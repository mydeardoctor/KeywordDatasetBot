package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class AnswerMapper extends EnumMapper
{
    private AnswerMapper()
    {
        super();
    }

    public static Answer fromString(final String answerAsString)
        throws NullPointerException
    {
        try
        {
            final Answer answer = mapFromString(answerAsString, Answer.class);
            return answer;
        }
        catch(final NullPointerException e)
        {
            throw e;
        }
    }

    //TODO null exception
    public static String toString(final Answer answer)
    {
        return mapToString(answer);
    }
}
