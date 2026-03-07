package com.notegraph.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_note_id")
    private Note sourceNote;

    @ManyToOne
    @JoinColumn(name = "target_note_id")
    private Note targetNote;

    private String relationshipType;

    private Double weight = 1.0;

    public Connection() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Note getSourceNote() {
        return sourceNote;
    }

    public void setSourceNote(Note sourceNote) {
        this.sourceNote = sourceNote;
    }

    public Note getTargetNote() {
        return targetNote;
    }

    public void setTargetNote(Note targetNote) {
        this.targetNote = targetNote;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
