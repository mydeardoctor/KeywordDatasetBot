package com.github.mydeardoctor.keyworddatasetbot.domain;

public class AnswerMapper
{
    private AnswerMapper()
    {
        super();
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
