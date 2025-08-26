package com.github.mydeardoctor.keyworddatasetbot.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ResourceLoader
{
    protected ResourceLoader()
    {
        super();
    }

    public static Map<String, String> loadStrings(
        final String directoryPath,
        final Set<String> fileNames,
        final String fileExtension)
        throws IOException, IllegalArgumentException
    {
        final Map<String, String> strings = new HashMap<>();

        for(final String fleName : fileNames)
        {
            final String fileNameWithExtension = fleName + fileExtension;
            final String filePath = Path
                .of(directoryPath)
                .resolve(fileNameWithExtension)
                .toString();

            String string = null;

            try
            {
                string = loadString(filePath);
            }
            catch(final IOException | IllegalArgumentException e)
            {
                throw e;
            }

            strings.put(fleName, string);
        }

        return strings;
    }

    private static String loadString(final String path)
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