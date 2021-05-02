package com.lukas.hackassembler.emit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InMemoryEmitterTest {
    @Test
    public void testEmitLine() {
        var emitter = new InMemoryEmitter();
        emitter.emitLine("line");
        assertEquals("line\n", emitter.getOutput());
        emitter.close();
    }

    @Test
    public void testEmitString() {
        var emitter = new InMemoryEmitter();
        emitter.emitString("some string");
        assertEquals("some string", emitter.getOutput());
        emitter.close();
    }
}