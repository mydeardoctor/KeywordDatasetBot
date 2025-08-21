package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class AudioClassMapper extends EnumMapper
{
    private AudioClassMapper()
    {
        super();
    }

    public static AudioClass fromString(final String audioClassAsString)
        throws NullPointerException, IllegalArgumentException
    {
        try
        {
            final AudioClass audioClass =
                mapFromString(audioClassAsString, AudioClass.class);
            return audioClass;
        }
        catch(final NullPointerException | IllegalArgumentException e)
        {
            throw e;
        }
    }

    public static String toString(final AudioClass audioClass)
    {
        return mapToString(audioClass);
    }
}
