package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class AudioClassRepository
{
    private final AudioClassDAO audioClassDAO;
    private final TelegramUserAudioClassDAO telegramUserAudioClassDAO;
    private final Logger logger;

    public AudioClassRepository(
        final AudioClassDAO audioClassDAO,
        final TelegramUserAudioClassDAO telegramUserAudioClassDAO)
    {
        super();

        this.audioClassDAO = audioClassDAO;
        this.telegramUserAudioClassDAO = telegramUserAudioClassDAO;

        logger = LoggerFactory.getLogger(AudioClassRepository.class);
    }

    //TODO methods
    public List<AudioClass> getAudioClasses() throws SQLException
    {
        try
        {
            final List<AudioClass> audioClasses =
                audioClassDAO.getAudioClasses();
            return audioClasses;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    public int getMaxDuration(final AudioClass audioClass) throws SQLException
    {
        try
        {
            final int maxDurationSeconds =
                audioClassDAO.getMaxDuration(audioClass);
            return maxDurationSeconds;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    public int getMaxDuration(final Long userId) throws SQLException
    {
        try
        {
            final int maxDurationSeconds =
                telegramUserAudioClassDAO.getMaxDuration(userId);
            return maxDurationSeconds;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
