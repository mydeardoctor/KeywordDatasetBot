package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class AnswerMapper extends EnumMapper
{
    private AnswerMapper()
    {
        super();
    }

    public static Answer fromString(final String answerAsString)
        throws NullPointerException, IllegalArgumentException
    {
        try
        {
            final Answer answer = mapFromString(answerAsString, Answer.class);
            return answer;
        }
        catch(final NullPointerException | IllegalArgumentException e)
        {
            throw e;
        }
    }

    public static String toString(final Answer answer)
        throws NullPointerException
    {
        final String answerAsString = mapToString(answer);
        if(answerAsString != null)
        {
            return answerAsString;
        }
        else
        {
            throw new NullPointerException("Answer can not be null!");
        }
    }
}
