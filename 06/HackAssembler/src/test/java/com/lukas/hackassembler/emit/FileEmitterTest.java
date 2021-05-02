package com.lukas.hackassembler.emit;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FileEmitterTest {
    @Test
    public void testFileEmitterInitialization() throws IOException {
        var emitter = new FileEmitter("test");
        assertNotNull(emitter);
        assertEquals("test.hack", emitter.getFilename());
        emitter.close();
        var emitterPath = Paths.get("test.hack");
        assertTrue(Files.exists(emitterPath));
        Files.delete(emitterPath);
        assertFalse(Files.exists(emitterPath));

        assertThrows(NullPointerException.class, () -> new FileEmitter(null));
    }

    @Test
    public void testEmitLine() throws IOException {
        var emitter = new FileEmitter("output");
        emitter.emitLine("line");
        assertEquals("line\n", emitter.getOutput());
        emitter.close();
        Files.delete(Paths.get("output.hack"));
    }

    @Test
    public void testEmitString() throws IOException {
        var emitter = new FileEmitter("output");
        emitter.emitString("string");
        assertEquals("string", emitter.getOutput());
        emitter.close();
        Files.delete(Paths.get("output.hack"));
    }

    @Test
    public void testCheckEmittedFile() throws IOException {
        var emitter = new FileEmitter("output");
        emitter.emitString("one ");
        emitter.emitString("two");
        emitter.close();

        var fileContent = Files.readString(Paths.get("output.hack")).replaceAll("\\r\\n?", "\n"); // platform independent
        assertEquals("one two\n", fileContent);
    }
}