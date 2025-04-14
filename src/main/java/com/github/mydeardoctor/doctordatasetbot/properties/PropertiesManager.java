package com.github.mydeardoctor.doctordatasetbot.properties;


import com.sun.jdi.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PropertiesManager
{
    //TODO мб сделать статиком?
    private final Properties properties = new Properties();
    private final Pattern pattern =
        Pattern.compile("^\\$\\{(\\w+)\\}$");

    private final Logger logger =
        LoggerFactory.getLogger(PropertiesManager.class);

    public PropertiesManager(final String pathToPropertiesFile)
    {
//        throw new InternalException();
        // If the path begins with '/',
        // then it is the absolute path relative to the root of the classpath.
//        try(final InputStream inputStream =
//                getClass().getResourceAsStream(pathToPropertiesFile))
//        {
//            properties.load(inputStream);
//        }
//        //TODO убрать обработчик
//        catch(IOException e)
//        {
//            final String errorMessage =
//                "Could not load application.properties!";
//            final InternalException ex = new InternalException(errorMessage);
//            logger.error(errorMessage, ex);
//            throw ex;
//        }
    }

    public String getProperty(final String key)
    {
        final String property = properties.getProperty(key);
//        if(property == null)
//        {
//
//        }
//
//        //TODO parse env variables
//
//        final Matcher matcher = pattern.matcher(property);
//        boolean r = matcher.matches();
//
//
//
//        if(r == true)
//        {
//            int g = matcher.groupCount();
//            String s0 = matcher.group(0);
//            String s1 = matcher.group(1);
//            String s2 = matcher.group(2);
//            String s3 = matcher.group(3);
//            String s4 = matcher.group(4);
//            String s5 = matcher.group(5);
//            if(s1 == null)
//            {
//
//            }
//            else
//            {
//                String envVAr = System.getenv(s1);
//                return envVAr;
//            }
//        }

        return properties.getProperty(key);
    }
}