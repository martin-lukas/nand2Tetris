package com.lukas.hackassembler.lex;

import com.lukas.hackassembler.exception.LexerErrorException;
import com.lukas.hackassembler.model.Token;
import com.lukas.hackassembler.model.TokenType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.lukas.hackassembler.model.TokenType.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;

public class LexerTest {
    @Test
    public void testEmptyInput() throws LexerErrorException {
        final var emptyLexer = new Lexer("");
        // '\n' character gets added to the input automatically
        assertArrayEquals(new TokenType[]{NEWLINE, EOF}, getAllTokenTypes(emptyLexer).toArray());
    }

    @Test
    public void testSkipWhitespace() throws LexerErrorException {
        var whitespaceLexer = new Lexer("   \n");
        assertArrayEquals(new TokenType[]{NEWLINE, NEWLINE, EOF}, getAllTokenTypes(whitespaceLexer).toArray());
        whitespaceLexer = new Lexer("   \n     \n");
        assertArrayEquals(new TokenType[]{NEWLINE, NEWLINE, NEWLINE, EOF}, getAllTokenTypes(whitespaceLexer).toArray());
    }

    @Test
    public void testLabelDeclaration() throws LexerErrorException {
        var labelLexer = new Lexer("(LABEL)");
        assertArrayEquals(new TokenType[]{LABEL_START, SYMBOL, LABEL_END, NEWLINE, EOF}, getAllTokenTypes(labelLexer).toArray());
        labelLexer = new Lexer("(La.bel3$:)");
        assertArrayEquals(new TokenType[]{LABEL_START, SYMBOL, LABEL_END, NEWLINE, EOF}, getAllTokenTypes(labelLexer).toArray());
        labelLexer = new Lexer("(Label) // this is a comment");
        assertArrayEquals(new TokenType[]{LABEL_START, SYMBOL, LABEL_END, NEWLINE, EOF}, getAllTokenTypes(labelLexer).toArray());

        final var invalidLabelStartLexer = new Lexer("(0Label)");
        assertThrows(LexerErrorException.class, () -> getAllTokenTypes(invalidLabelStartLexer));

        final var invalidLabelEndLexer = new Lexer("NotLabel");
        assertArrayEquals(new TokenType[]{COMP_PART, NEWLINE, EOF}, getAllTokenTypes(invalidLabelEndLexer).toArray());
    }

    @Test
    public void testAInstructionExact() throws LexerErrorException {
        var aInstLexer = new Lexer("@123");
        assertArrayEquals(new TokenType[]{A_INST_MARK, NUMBER, NEWLINE, EOF}, getAllTokenTypes(aInstLexer).toArray());

        final var invalidAInstSpaceLexer = new Lexer("@ 123");
        // @ needs to be immediately followed by the address / symbol
        assertThrows(LexerErrorException.class, () -> getAllTokenTypes(invalidAInstSpaceLexer));

        final var invalidAInstNumberLexer = new Lexer("@123d");
        assertThrows(LexerErrorException.class, () -> getAllTokenTypes(invalidAInstNumberLexer));
    }

    @Test
    public void testAInstructionSymbol() throws LexerErrorException {
        var aInstLexer = new Lexer("@var");
        assertArrayEquals(new TokenType[]{A_INST_MARK, SYMBOL, NEWLINE, EOF}, getAllTokenTypes(aInstLexer).toArray());
        aInstLexer = new Lexer("@_La.bel3$:");
        assertArrayEquals(new TokenType[]{A_INST_MARK, SYMBOL, NEWLINE, EOF}, getAllTokenTypes(aInstLexer).toArray());
        aInstLexer = new Lexer("@:");
        assertArrayEquals(new TokenType[]{A_INST_MARK, SYMBOL, NEWLINE, EOF}, getAllTokenTypes(aInstLexer).toArray());

        final var invalidAInstSymbolLexer = new Lexer("@Label??");
        assertThrows(LexerErrorException.class, () -> getAllTokenTypes(invalidAInstSymbolLexer));
    }

    @Test
    public void testCInstructionCompOnly() throws LexerErrorException {
        var cInstLexer = new Lexer("M+1");
        assertArrayEquals(new TokenType[]{COMP_PART, NEWLINE, EOF}, getAllTokenTypes(cInstLexer).toArray());
        cInstLexer = new Lexer("0sdf34dsf");
        assertArrayEquals(new TokenType[]{COMP_PART, NEWLINE, EOF}, getAllTokenTypes(cInstLexer).toArray());
        cInstLexer = new Lexer("0");
        assertArrayEquals(new TokenType[]{COMP_PART, NEWLINE, EOF}, getAllTokenTypes(cInstLexer).toArray());
    }

    @Test
    public void testCInstructionDestAndComp() throws LexerErrorException {
        var cInstLexer = new Lexer("M=M+1");
        assertArrayEquals(new TokenType[]{COMP_PART, DEST_SEPARATOR, COMP_PART, NEWLINE, EOF}, getAllTokenTypes(cInstLexer).toArray());
        cInstLexer = new Lexer("AMD=0");
        assertArrayEquals(new TokenType[]{COMP_PART, DEST_SEPARATOR, COMP_PART, NEWLINE, EOF}, getAllTokenTypes(cInstLexer).toArray());
    }

    @Test
    public void testCInstructionCompAndJump() throws LexerErrorException {
        var cInstLexer = new Lexer("M+1;JMP");
        assertArrayEquals(new TokenType[]{COMP_PART, JUMP_SEPARATOR, COMP_PART, NEWLINE, EOF}, getAllTokenTypes(cInstLexer).toArray());
        cInstLexer = new Lexer("0;JEQ");
        assertArrayEquals(new TokenType[]{COMP_PART, JUMP_SEPARATOR, COMP_PART, NEWLINE, EOF}, getAllTokenTypes(cInstLexer).toArray());
    }

    @Test
    public void testCInstructionDestAndCompAndJump() throws LexerErrorException {
        var cInstLexer = new Lexer("AMD=M+1;JMP");
        assertArrayEquals(
                new TokenType[]{COMP_PART, DEST_SEPARATOR, COMP_PART, JUMP_SEPARATOR, COMP_PART, NEWLINE, EOF},
                getAllTokenTypes(cInstLexer).toArray()
        );
    }

    // PRIVATE HELPER METHODS

    private static List<Token> getAllTokens(Lexer lexer) throws LexerErrorException {
        List<Token> tokens = new ArrayList<>();

        Token curToken;
        do {
            curToken = lexer.getToken();
            tokens.add(curToken);
        } while (curToken.getType() != TokenType.EOF);

        return tokens;
    }

    private static List<TokenType> getAllTokenTypes(Lexer lexer) throws LexerErrorException {
        return getAllTokens(lexer).stream()
                .map(Token::getType)
                .collect(Collectors.toList());
    }
}