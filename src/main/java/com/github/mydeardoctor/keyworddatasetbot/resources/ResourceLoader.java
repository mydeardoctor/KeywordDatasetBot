package com.github.mydeardoctor.keyworddatasetbot.resources;

import java.io.IOException;
import java.io.InputStream;

public abstract class ResourceLoader
{
    private ResourceLoader()
    {
        super();
    }

    public static String loadString(final String path)
        throws IOException, IllegalArgumentException
    {
        try(final InputStream inputStream =
                ResourceLoader.class.getClassLoader().getResourceAsStream(path))
        {
            if(inputStream != null)
            {
                final String string = new String(inputStream.readAllBytes());
                return string;
            }
            else
            {
                final String errorMessage = new StringBuilder()
                    .append("Resource at \"")
                    .append(path)
                    .append("\" does not exist!")
                    .toString();
                throw new IllegalArgumentException(errorMessage);
            }
        }
        catch(final IOException | IllegalArgumentException e)
        {
            throw e;
        }
    }
}