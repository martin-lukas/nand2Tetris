package com.lukas.hackassembler;

import com.lukas.hackassembler.emit.FileEmitter;
import com.lukas.hackassembler.exception.EmitterErrorException;
import com.lukas.hackassembler.exception.LexerErrorException;
import com.lukas.hackassembler.exception.ParserErrorException;
import com.lukas.hackassembler.lex.Lexer;
import com.lukas.hackassembler.parse.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Assembler {
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                Path inputFile = Paths.get(args[0]);
                var input = Files.readString(inputFile);

                var lexer = new Lexer(input);
                var emitter = new FileEmitter(getFilenameWithoutExtension(args.length == 2 ? Paths.get(args[1]) : inputFile));
                new Parser(lexer, emitter).parse();

            } catch (IOException e) {
                System.err.println("The input file couldn't be read (maybe not a file, or a wrong encoding).");
            } catch (LexerErrorException e) {
                System.err.println("Lexer error: " + e.getMessage());
            } catch (ParserErrorException e) {
                System.err.println("Parser error: " + e.getMessage());
            } catch (EmitterErrorException e) {
                System.err.println("Couldn't write into the output file (maybe an invalid name, or need elevated privileges).\n" +
                        "Emitter error: " + e.getMessage());
            }
        } else {
            System.err.println("You need to specify at least the input file.");
        }
    }

    private static String getFilenameWithoutExtension(Path filePath) {
        String filename = Objects.requireNonNull(filePath, "The file path can't be null.").getFileName().toString();

        int extStart = filename.lastIndexOf(".");

        if (extStart == -1) { // file without extension
            return filename;
        }

        return filename.substring(0, extStart);
    }
}
