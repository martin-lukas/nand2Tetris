package com.lukas.hackassembler.emit;

public class InMemoryEmitter implements Emitter {
    private final StringBuilder outputBuilder;

    public InMemoryEmitter() {
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
    public void close() {
        // empty on purpose
    }
}
