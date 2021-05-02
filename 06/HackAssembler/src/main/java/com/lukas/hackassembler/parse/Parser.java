package com.lukas.hackassembler.parse;

import com.lukas.hackassembler.emit.Emitter;
import com.lukas.hackassembler.emit.InMemoryEmitter;
import com.lukas.hackassembler.exception.EmitterErrorException;
import com.lukas.hackassembler.exception.LexerErrorException;
import com.lukas.hackassembler.exception.ParserErrorException;
import com.lukas.hackassembler.lex.Lexer;
import com.lukas.hackassembler.model.CompType;
import com.lukas.hackassembler.model.JumpType;
import com.lukas.hackassembler.model.Token;
import com.lukas.hackassembler.model.TokenType;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.lukas.hackassembler.model.TokenType.*;

public class Parser {
    private static final int MAX_A_VALUE = (int) (Math.pow(2, 15) - 1); // only 15 bits for A inst. value

    private static final int DYNAMIC_MEMORY_START_ADDRESS = 16;

    private final Lexer lexer;
    private final Emitter emitter;

    private final Map<String, Integer> labels;
    private final Map<String, Integer> variables;

    private Token curToken;
    private Token peekToken;

    private int nextVariableAddress;

    public Parser(String input) throws LexerErrorException {
        this(new Lexer(input));
    }

    public Parser(Lexer lexer) throws LexerErrorException {
        this(lexer, new InMemoryEmitter());
    }

    public Parser(Lexer lexer, Emitter emitter) throws LexerErrorException {
        this.lexer = lexer;
        this.emitter = emitter;

        labels = new HashMap<>();
        variables = new HashMap<>();

        // add all built-in symbols to the variables
        Arrays.stream(BuiltInSymbol.values())
                .forEach(symbol -> variables.put(symbol.name(), symbol.address));

        reset();
    }

    /**
     * Fetches the output from the emitter.
     *
     * @return output from the parsing fetched through the emitter
     */
    public String getOutput() {
        return emitter.getOutput();
    }

    /**
     * Parses the input and emits it into the emitter (either provided or the default {@link InMemoryEmitter}.
     * It parses the code using 2 passes - label scanning and then the parsing and emitting itself.
     *
     * @throws LexerErrorException   if there was problem during tokenization
     * @throws ParserErrorException  if there was an invalid sequence of tokens
     * @throws EmitterErrorException if there was a problem during the output creation
     */
    public void parse() throws LexerErrorException, ParserErrorException, EmitterErrorException {
        scanLabels();
        reset();
        program();

        try {
            emitter.close();
        } catch (IOException e) {
            throw new EmitterErrorException(e);
        }
    }

    private void reset() throws LexerErrorException {
        lexer.reset();
        nextToken();
        nextToken();
        nextVariableAddress = DYNAMIC_MEMORY_START_ADDRESS;
    }

    private void scanLabels() throws LexerErrorException, ParserErrorException {
        var curLine = 0;
        while (curToken.getType() != EOF) {
            switch (curToken.getType()) {
                case LABEL_START:
                    nextToken();
                    assertTokenType(SYMBOL);

                    var symbol = curToken.getValue();

                    nextToken();
                    assertTokenType(LABEL_END);
                    assertPeekTokenType(NEWLINE);

                    if (!variables.containsKey(symbol)) {
                        labels.put(symbol, curLine);
                    } else {
                        abort("The label '" + symbol + "' is invalid (shadows in-built symbol).");
                    }
                    break;
                case A_INST_MARK:
                case COMP_PART:
                    curLine++;
                    break;
                case NEWLINE:
                    break;
                default:
                    abort("A statement can't start with the token type " + curToken.getType());
            }
            skipToEndOfline();
            nextNonNewlineToken();
        }
    }

    private void program() throws LexerErrorException, ParserErrorException {
        while (curToken.getType() != EOF) {
            statement();
        }
    }

    private void statement() throws LexerErrorException, ParserErrorException {
        switch (curToken.getType()) {
            // LABEL DECLARATION - already checked during label scanning
            case LABEL_START:
                skipToEndOfline();
                break;
            // A-INSTRUCTION
            case A_INST_MARK:
                aInstruction();
                nextToken();
                assertTokenType(NEWLINE);
                break;
            // C-INSTRUCTION
            case COMP_PART:
                cInstruction();
                nextToken();
                assertTokenType(NEWLINE);
                break;
            case NEWLINE:
                break;
            default:
                abort("A statement can't start with the token type " + curToken.getType());
        }

        nextToken();
    }

    private void aInstruction() throws LexerErrorException, ParserErrorException {
        nextToken();
        switch (curToken.getType()) {
            case SYMBOL:
                symbol();
                break;
            case NUMBER:
                number();
                break;
            default:
                abort("The A-instruction contains a different token than allowed: " + curToken.getType());
        }
    }

    private void symbol() {
        var symbol = curToken.getValue();

        int symbolAddress;
        if (labels.containsKey(symbol)) {
            symbolAddress = labels.get(symbol);
        } else if (variables.containsKey(symbol)) {
            symbolAddress = variables.get(symbol);
        } else {
            symbolAddress = nextVariableAddress++;
            variables.put(symbol, symbolAddress);
        }

        emitter.emitLine(convertToBinaryString(symbolAddress));
    }

    private void number() throws ParserErrorException {
        int numValue = Integer.parseInt(curToken.getValue());
        if (numValue <= MAX_A_VALUE) {
            // the number is at most 15 bit long, but A instruction has the MSB set to 0 anyway
            emitter.emitLine(convertToBinaryString(numValue));
        } else {
            abort("The number exceeds the max. possible value: " + numValue);
        }
    }

    private void cInstruction() throws LexerErrorException, ParserErrorException {
        if (peekToken.getType() == DEST_SEPARATOR) {
            var dest = curToken;

            nextToken();
            assertTokenType(DEST_SEPARATOR);
            nextToken();
            assertTokenType(COMP_PART);

            var comp = curToken;
            if (peekToken.getType() == JUMP_SEPARATOR) {
                nextToken();
                assertTokenType(JUMP_SEPARATOR);
                nextToken();

                var jump = curToken;
                emitCInstruction(dest, comp, jump);
            } else {
                emitCInstruction(dest, comp, null);
            }
        } else {
            var comp = curToken;

            if (peekToken.getType() == JUMP_SEPARATOR) {
                nextToken();
                assertTokenType(JUMP_SEPARATOR);
                nextToken();
                assertTokenType(COMP_PART);

                var jump = curToken;
                emitCInstruction(null, comp, jump);
            } else {
                emitCInstruction(null, comp, null);
            }
        }
    }

    private void nextToken() throws LexerErrorException {
        curToken = peekToken;
        peekToken = lexer.getToken();
    }

    private void skipToEndOfline() throws LexerErrorException {
        do {
            nextToken();
        } while (curToken.getType() != NEWLINE);
    }

    private void nextNonNewlineToken() throws LexerErrorException {
        do {
            nextToken();
        } while (curToken.getType() == NEWLINE);
    }

    private void assertTokenType(TokenType expected) throws ParserErrorException {
        assertTokenTypeOf(curToken, expected);
    }

    private void assertPeekTokenType(TokenType expected) throws ParserErrorException {
        assertTokenTypeOf(peekToken, expected);
    }

    private void assertTokenTypeOf(Token token, TokenType expected) throws ParserErrorException {
        if (token.getType() != expected) {
            abortUnexpectedToken(expected, token.getType());
        }
    }

    private String convertToBinaryString(int number) {
        return String.format("%16s", Integer.toBinaryString(number)).replace(" ", "0");
    }

    private void emitCInstruction(Token dest, Token comp, Token jump) throws ParserErrorException {
        emitter.emitString("111"); // 3 MSBs are 1 for C-instructions

        emitComp(comp.getValue()); // 1 bit for A/M and

        if (dest != null) { // dest code - 3 bits 6 bits for the computation type
            emitDest(dest.getValue());
        } else {
            emitter.emitString("000");
        }

        if (jump != null) { // jump code - 3 bits
            emitJump(jump.getValue());
        } else {
            emitter.emitString("000");
        }

        emitter.emitLine("");
    }

    private void emitDest(String destString) throws ParserErrorException {
        if (destString.length() >= 1 && destString.length() <= 3 && destString.matches("A?M?D?")) {
            emitter.emitString((destString.contains("A") ? "1" : "0")
                    + (destString.contains("D") ? "1" : "0")
                    + (destString.contains("M") ? "1" : "0")
            );
        } else {
            abort("The destination registers \"" + destString + "\" are not valid.");
        }
    }

    private void emitComp(String compString) throws ParserErrorException {
        emitter.emitString(compString.contains("M") ? "1" : "0"); // computing with A value vs. RAM[A] value

        emitter.emitString(Optional
                .ofNullable(CompType.getByLiteral(compString))
                .orElseThrow(() -> new ParserErrorException("The computation \""
                        + compString + "\" doesn't have a corresponding machine code."))
                .getCode() // computation code - 6 bits
        );
    }

    private void emitJump(String jumpString) throws ParserErrorException {
        emitter.emitString(Optional
                .ofNullable(JumpType.getByLiteral(jumpString))
                .orElseThrow(() -> new ParserErrorException("The jump literal \""
                        + jumpString + "\" is not valid."))
                .getCode()
        );
    }

    private void abortUnexpectedToken(TokenType expected, TokenType actual) throws ParserErrorException {
        abort("Expected token " + expected + ", but got " + actual + " instead.");
    }

    private void abort(String message) throws ParserErrorException {
        throw new ParserErrorException(message);
    }

    public enum BuiltInSymbol {
        SP(0),
        LCL(1),
        ARG(2),
        THIS(3),
        THAT(4),
        SCREEN(16384),
        KBD(24576),
        R0(0),
        R1(1),
        R2(2),
        R3(3),
        R4(4),
        R5(5),
        R6(6),
        R7(7),
        R8(8),
        R9(9),
        R10(10),
        R11(11),
        R12(12),
        R13(13),
        R14(14),
        R15(15);

        private final int address;

        BuiltInSymbol(int address) {
            this.address = address;
        }
    }
}
