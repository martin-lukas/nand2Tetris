package com.lukas.hackassembler;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class AssemblerTest {
    @After
    public void cleanUp() {
        System.out.println();
    }

    @Test
    public void testAssemblerWithoutArgs() {
        System.out.println("Assembler - args[]");

        Assembler.main(new String[]{});
    }

    @Test
    public void testAssemblerWithInputArgNotExists() {
        System.out.println("Assembler - args['input.asm']");

        Assembler.main(new String[]{"input.asm"});
    }

    @Test
    public void testAssemblerWithInputArgExists() throws IOException {
        System.out.println("Assembler - args['src/test/resources/program.asm']");

        Assembler.main(new String[]{"src/test/resources/program.asm"}); // should generate output.hack

        Path outputPath = Paths.get("program.hack");

        assertTrue(Files.exists(outputPath));

        var expectedOutput = Files.readString(Paths.get("src/test/resources/program.hack"));
        var actualOutput = Files.readString(outputPath).replaceAll("\\r\\n?", "\n"); // platform independent
        assertEquals(expectedOutput, actualOutput);

        Files.delete(outputPath);
        assertFalse(Files.exists(outputPath));

        System.out.println("The output file is correct.");
    }

    @Test
    public void testAssemblerWithInputAndOutputArgsValid() throws IOException {
        System.out.println("Assembler - args['src/test/resources/program.asm', 'myProgram.hack']");

        Assembler.main(new String[]{"src/test/resources/program.asm", "myProgram.hack"});

        Path outputPath = Paths.get("myProgram.hack");

        assertTrue(Files.exists(outputPath));

        var expectedOutput = Files.readString(Paths.get("src/test/resources/program.hack"));
        var actualOutput = Files.readString(outputPath).replaceAll("\\r\\n?", "\n"); // platform independent
        assertEquals(expectedOutput, actualOutput);

        Files.delete(outputPath);
        assertFalse(Files.exists(outputPath));

        System.out.println("The output file is correct.");
    }
}