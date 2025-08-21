package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class CommandMapper extends EnumMapper
{
    private CommandMapper()
    {
        super();
    }

    public static Command fromString(final String commandAsString)
        throws NullPointerException, IllegalArgumentException
    {
        try
        {
            final Command command =
                mapFromString(commandAsString, Command.class);
            return command;
        }
        catch(final NullPointerException | IllegalArgumentException e)
        {
            throw e;
        }
    }
}
