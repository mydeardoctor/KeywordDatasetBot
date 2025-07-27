package com.github.mydeardoctor.keyworddatasetbot.domain;

public class AnswerMapper
{
    private AnswerMapper()
    {
        super();
    }

    public static Answer map(final String answerAsString)
    {
        if(answerAsString != null)
        {
            return Answer.valueOf(answerAsString.toUpperCase());
        }
        else
        {
            return null;
        }
    }

    public static String map(final Answer answer)
    {
        if(answer != null)
        {
            return answer.name().toLowerCase();
        }
        else
        {
            return null;
        }
    }
}
