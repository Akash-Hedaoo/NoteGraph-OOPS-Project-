package com.notegraph.backend.model;

import jakarta.persistence.Entity;

@Entity
public class TextNote extends Note {

    private String content;

    public TextNote() {
    }

    @Override
    public String getNoteType() {
        return "TEXT";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
