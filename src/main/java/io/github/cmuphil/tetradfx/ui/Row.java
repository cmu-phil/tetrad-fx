package io.github.cmuphil.tetradfx.ui;

import javafx.beans.property.SimpleStringProperty;

public class Row {
    private final SimpleStringProperty variableName;
    private final SimpleStringProperty variableType;
    private final SimpleStringProperty stats;
    private final SimpleStringProperty notes;

    public Row(String variableName, String variableType, String description) {
        this.variableName = new SimpleStringProperty(variableName);
        this.variableType = new SimpleStringProperty(variableType);
        this.stats = new SimpleStringProperty(description);
        this.notes = new SimpleStringProperty();
    }

    public String getVariableName() {
        return variableName.get();
    }

    public void setVariableName(String s) {
        variableName.set(s);
    }

    public String getVariableType() {
        return variableType.get();
    }

    public void setVariableType(String s) {
        variableType.set(s);
    }

    public String getNotes() {
        return notes.get();
    }

    public void setNotes(String s) {
        notes.set(s);
    }

    public String getStats() {
        return stats.get();
    }

    public void setStats(String s) {
        stats.set(s);
    }
}

