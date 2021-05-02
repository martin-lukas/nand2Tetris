package com.lukas.hackassembler.parse;

import com.lukas.hackassembler.emit.FileEmitter;
import com.lukas.hackassembler.exception.EmitterErrorException;
import com.lukas.hackassembler.exception.LexerErrorException;
import com.lukas.hackassembler.exception.ParserErrorException;
import com.lukas.hackassembler.lex.Lexer;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ParserTest {
    @Test
    public void testLabelDeclarationOutput() throws LexerErrorException, ParserErrorException, EmitterErrorException {
        var parser = new Parser("(LABEL)\n(OTHER_LABEL)");
        parser.parse();
        var output = parser.getOutput();
        // labels themselves don't generate any output
        assertEquals("", output);
    }

    @Test
    public void testAInstructionNumberOutput() throws LexerErrorException, ParserErrorException, EmitterErrorException {
        var parser = new Parser("@3");
        parser.parse();
        var output = parser.getOutput();
        assertEquals("0000000000000011\n", output);

        parser = new Parser("@63\n@64\n");
        parser.parse();
        output = parser.getOutput();
        assertEquals("0000000000111111\n0000000001000000\n", output);

        parser = new Parser("@32767");
        parser.parse();
        output = parser.getOutput();
        assertEquals("0111111111111111\n", output);

        assertThrows(LexerErrorException.class, () -> new Parser("@ 32767\n").parse());

        assertThrows(ParserErrorException.class, () -> new Parser("@32768\n").parse());

        assertThrows(ParserErrorException.class, () -> new Parser("@32 @21").parse());
    }

    @Test
    public void testAInstructionSymbolOutput() throws LexerErrorException, ParserErrorException, EmitterErrorException {
        var parser = new Parser("@var");
        parser.parse();
        var output = parser.getOutput();
        assertEquals("0000000000010000\n", output);

        parser = new Parser("@var\n@otherVar");
        parser.parse();
        output = parser.getOutput();
        assertEquals("0000000000010000\n0000000000010001\n", output);

        parser = new Parser("@var\n@3");
        parser.parse();
        output = parser.getOutput();
        assertEquals("0000000000010000\n0000000000000011\n", output);

        parser = new Parser("@THAT\n@3");
        parser.parse();
        output = parser.getOutput();
        assertEquals("0000000000000100\n0000000000000011\n", output);

        assertThrows(LexerErrorException.class, () -> new Parser("@?var\n@3").parse());
    }

    @Test
    public void testAInstructionLabelOutput() throws LexerErrorException, ParserErrorException, EmitterErrorException {
        var parser = new Parser("@3\n(LABEL)\n@4\n@LABEL");
        parser.parse();
        var output = parser.getOutput();
        assertEquals("0000000000000011\n0000000000000100\n0000000000000001\n", output);

        parser = new Parser("@3\n(LABEL)\n@4\n@ALABEL");
        parser.parse();
        output = parser.getOutput();
        // the third A-instruction is going to be a variable creatiion because of a typo
        assertEquals("0000000000000011\n0000000000000100\n0000000000010000\n", output);

        // THIS is a reserved address name - can't be used as a label or a variable
        assertThrows(ParserErrorException.class, () -> new Parser("@3\n(THIS)\n@THIS").parse());
    }

    @Test
    public void testCInstructionOutput() throws LexerErrorException, ParserErrorException, EmitterErrorException {
        var parser = new Parser("1\nD+1");
        parser.parse();
        var output = parser.getOutput();
        assertEquals("1110111111000000\n1110011111000000\n", output);

        parser = new Parser("AM=A-1\nMD=D+M");
        parser.parse();
        output = parser.getOutput();
        assertEquals("1110110010101000\n1111000010011000\n", output);

        parser = new Parser("0;JMP");
        parser.parse();
        output = parser.getOutput();
        assertEquals("1110101010000111\n", output);

        parser = new Parser("M+1;JEQ\nD=D|A;JNE");
        parser.parse();
        output = parser.getOutput();
        assertEquals("1111110111000010\n1110010101010101\n", output);

        assertThrows(ParserErrorException.class, () -> new Parser("DM=M-1;JMP").parse());

        assertThrows(ParserErrorException.class, () -> new Parser("M-2;JMP").parse());

        assertThrows(ParserErrorException.class, () -> new Parser("M-1;JUMPHIGH").parse());
    }

    @Test
    public void testAAndCInstructionsOutput() throws LexerErrorException, ParserErrorException, EmitterErrorException, URISyntaxException, IOException {
        var parser = new Parser("@24\nD=A\n@age\nM=D");
        parser.parse();
        var output = parser.getOutput();
        assertEquals("0000000000011000\n1110110000010000\n0000000000010000\n1110001100001000\n", output);

        // RAM[0] = 2 + 3
        parser = new Parser("@2\nD=A\n@3\nD=D+A\n@0\nM=D");
        parser.parse();
        output = parser.getOutput();
        assertEquals("0000000000000010\n1110110000010000\n0000000000000011\n1110000010010000\n0000000000000000\n1110001100001000\n", output);

        // RAM[2] = RAM[0] * RAM[1] - Mult.asm
        var input = Files.readString(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("Mult.asm")).toURI()));
        var expectedOutput = Files.readString(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("Mult.hack")).toURI()));
        parser = new Parser(input);
        parser.parse();
        var actualOutput = parser.getOutput();
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testParserWithFaultyFileEmitter() throws IOException {
        Path outputDir = Paths.get("output.hack");
        Files.createDirectory(outputDir);
        assertThrows(EmitterErrorException.class, () -> new Parser(new Lexer("@2\nM=M+1"), new FileEmitter("output")).parse());
        Files.delete(outputDir);
    }
}
