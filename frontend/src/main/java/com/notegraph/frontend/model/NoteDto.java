package com.notegraph.frontend.model;

// Encapsulates the data fetched from the API
public class NoteDto {
    private Long id;
    private String title;
    private String content;
    private String noteType;

    public NoteDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    private java.util.List<java.util.Map<String, Object>> tags;

    public java.util.List<java.util.Map<String, Object>> getTags() {
        return tags;
    }

    public void setTags(java.util.List<java.util.Map<String, Object>> tags) {
        this.tags = tags;
    }
}
