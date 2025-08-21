package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class AudioClassMapper extends EnumMapper
{
    private AudioClassMapper()
    {
        super();
    }

    public static AudioClass fromString(final String audioClassAsString)
        throws NullPointerException
    {
        try
        {
            final AudioClass audioClass =
                mapFromString(audioClassAsString, AudioClass.class);
            return audioClass;
        }
        catch(final NullPointerException e)
        {
            throw e;
        }
    }

    //TODO может быть null
    public static String toString(final AudioClass audioClass)
    {
        return mapToString(audioClass);
    }
}
