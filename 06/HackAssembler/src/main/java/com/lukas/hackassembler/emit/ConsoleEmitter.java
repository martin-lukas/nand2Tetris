package com.lukas.hackassembler.emit;

public class ConsoleEmitter implements Emitter {
    private final StringBuilder outputBuilder;

    public ConsoleEmitter() {
        outputBuilder = new StringBuilder();
    }

    @Override
    public void emitLine(String line) {
        outputBuilder.append(line).append('\n');
        System.out.println(line);
    }

    @Override
    public void emitString(String str) {
        outputBuilder.append(str);
        System.out.print(str);
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
