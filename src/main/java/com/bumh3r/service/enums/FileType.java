package com.bumh3r.service.enums;

public enum FileType {
    TUTOR("tutor"),
    TUTORADO("tutorado"),
    PAT("pat"),
    ACTIVIDAD("actividad"),
    GRUPO("grupo"),
    EVIDENCIA("evidencia"),
    COORDINADOR("coordinador");

    private final String value;

    FileType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
