package com.lukas.hackassembler.emit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class FileEmitter implements Emitter {
    private static final String OUTPUT_FILE_EXTENSION = ".hack";
    private final StringBuilder outputBuilder;

    private final String filename;

    public FileEmitter(String filename) {
        this.filename = Objects.requireNonNull(filename, "FileEmitter doesn't accept null filename.") + OUTPUT_FILE_EXTENSION;
        outputBuilder = new StringBuilder();
    }

    @Override
    public void emitLine(String line) {
        outputBuilder.append(line).append('\n');
    }

    @Override
    public void emitString(String str) {
        outputBuilder.append(str);
    }

    @Override
    public String getOutput() {
        return outputBuilder.toString();
    }

    @Override
    public void close() throws IOException {
        Files.write(Paths.get(filename), Arrays.asList(outputBuilder.toString().split("\\n")));
    }

    public String getFilename() {
        return filename;
    }
}
