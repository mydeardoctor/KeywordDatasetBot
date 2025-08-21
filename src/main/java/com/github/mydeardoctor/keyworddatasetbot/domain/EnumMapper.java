package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class  EnumMapper
{
    private EnumMapper()
    {
        super();
    }

    public static <E extends Enum<E>> E map(
        final String enumValueAsString,
        final Class<E> enumClass)
    {
        if(enumValueAsString != null)
        {
            return Enum.valueOf(enumClass, enumValueAsString.toUpperCase());
        }
        else
        {
            return null;
        }
    }

    public static String map(final Enum<?> enumValue)
    {
        if(enumValue != null)
        {
            return enumValue.name().toLowerCase();
        }
        else
        {
            return null;
        }
    }
}