package com.notegraph.backend.model;

import jakarta.persistence.Entity;

@Entity
public class CodeNote extends Note {

    private String codeSnippet;
    private String language;

    public CodeNote() {
    }

    @Override
    public String getNoteType() {
        return "CODE";
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
