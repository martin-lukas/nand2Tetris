package com.lukas.hackassembler.emit;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConsoleEmitterTest {
    @After
    public void cleanUp() {
        System.out.println();
    }

    @Test
    public void testEmitLine() {
        var emitter = new ConsoleEmitter();
        emitter.emitLine("line");
        assertEquals("line\n", emitter.getOutput());
        emitter.close();
    }

    @Test
    public void testEmitString() {
        var emitter = new ConsoleEmitter();
        emitter.emitString("some string");
        assertEquals("some string", emitter.getOutput());
        emitter.close();
    }
}