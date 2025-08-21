package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class CommandParser
{
    private CommandParser()
    {
        super();
    }

    public static Command parse(final String commandAsString)
        throws IllegalArgumentException
    {
        if((commandAsString != null) &&
           (commandAsString.length() > 1) &&
           (commandAsString.charAt(0) == '/'))
        {
            try
            {
                final Command command = EnumMapper.map(
                    commandAsString.substring(1),
                    Command.class);
                return command;
            }
            catch(final IllegalArgumentException e)
            {
                throw e;
            }
        }
        else
        {
            final String errorMessage = "Invalid command!";
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
