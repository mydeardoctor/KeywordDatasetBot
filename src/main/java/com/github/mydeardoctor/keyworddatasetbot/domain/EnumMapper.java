package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class EnumMapper
{
    protected EnumMapper()
    {
        super();
    }

    protected static <E extends Enum<E>> E mapFromString(
        final String enumValueAsString,
        final Class<E> enumClass)
        throws NullPointerException, IllegalArgumentException
    {
        if(enumValueAsString != null)
        {
            return Enum.valueOf(enumClass, enumValueAsString.toUpperCase());
        }
        else
        {
            final String errorMessage = "Enum value as String can not be null!";
            throw new NullPointerException(errorMessage);
        }
    }

    protected static String mapToString(final Enum<?> enumValue)
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