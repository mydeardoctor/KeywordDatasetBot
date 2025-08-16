package com.github.mydeardoctor.keyworddatasetbot.domain;

public enum AudioClass
{
    DOCTOR("doctor", "доктор"),
    SAMEHADA("samehada", "самехада");

    private final String englishName;
    private final String russianName;
    private final String humanReadableName;

    private AudioClass(final String englishName, final String russianName)
    {
        this.englishName = englishName;
        this.russianName = russianName;
        this.humanReadableName = new StringBuilder()
            .append(this.englishName)
            .append(" / ")
            .append(this.russianName)
            .toString();
    }

    public String getEnglishName()
    {
        return englishName;
    }

    public String getRussianName()
    {
        return russianName;
    }

    @Override
    public String toString()
    {
        return humanReadableName;
    }
}