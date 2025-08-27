package com.github.mydeardoctor.keyworddatasetbot.domain;

public class FileIdsAndAudioClass
{
    private final String fileUniqueId;
    private final String fileId;
    private final AudioClass audioClass;

    public FileIdsAndAudioClass(
        final String fileUniqueId,
        final String fileId,
        final AudioClass audioClass)
    {
        super();

        this.fileUniqueId = fileUniqueId;
        this.fileId = fileId;
        this.audioClass = audioClass;
    }

    public String getFileUniqueId()
    {
        return fileUniqueId;
    }

    public String getFileId()
    {
        return fileId;
    }

    public AudioClass getAudioClass()
    {
        return audioClass;
    }
}
