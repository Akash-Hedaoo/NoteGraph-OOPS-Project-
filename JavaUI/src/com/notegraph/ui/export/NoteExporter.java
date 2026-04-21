package com.notegraph.ui.export;

import java.io.File;

/**
 * Strategy interface for exporting notes.
 * Applies the OOP Strategy Pattern to allow different export formats.
 */
public interface NoteExporter {
    String getFormatName();
    String getExtension();
    void export(String title, String content, File targetFile) throws Exception;
}
