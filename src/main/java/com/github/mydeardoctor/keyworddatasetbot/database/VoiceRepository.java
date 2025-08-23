package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class VoiceRepository
{
    private final VoiceDAO voiceDAO;
    private final TelegramUserVoiceDAO telegramUserVoiceDAO;
    private final Logger logger;

    public VoiceRepository(
        final VoiceDAO voiceDAO,
        final TelegramUserVoiceDAO telegramUserVoiceDAO)
    {
        super();

        this.voiceDAO = voiceDAO;
        this.telegramUserVoiceDAO = telegramUserVoiceDAO;

        logger = LoggerFactory.getLogger(VoiceRepository.class);
    }

    //TODO methods
    public void saveVoice(
        final String fileUniqueId,
        final String fileId,
        final int durationRoundedUpSeconds,
        final AudioClass audioClass,
        final Long userId)
        throws SQLException
    {
        try
        {
            voiceDAO.saveVoice(
                fileUniqueId,
                fileId,
                durationRoundedUpSeconds,
                audioClass,
                userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    public Map<AudioClass, Long> getVoiceCount(final Long userId)
        throws SQLException
    {
        try
        {
            final Map<AudioClass, Long> voiceCount =
                voiceDAO.getVoiceCount(userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    public long getTotalVoiceCount() throws SQLException
    {
        try
        {
            final long totalVoiceCount = voiceDAO.getTotalVoiceCount();
            return totalVoiceCount;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    //TODO redo
    public List<String> getVoiceFileIdsAndAudioClass(final Long userId)
        throws SQLException
    {
        try
        {
            final List<String> fileIdsAndAudioClass =
                telegramUserVoiceDAO.getVoiceFileIdsAndAudioClass(userId);
            return fileIdsAndAudioClass;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    public void deleteMostRecentVoice(final Long userId) throws SQLException
    {
        try
        {
            telegramUserVoiceDAO.deleteMostRecentVoice(userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
