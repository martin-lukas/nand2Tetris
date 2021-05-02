package com.lukas.hackassembler.lex;

import com.lukas.hackassembler.exception.LexerErrorException;
import com.lukas.hackassembler.model.Token;
import com.lukas.hackassembler.model.TokenType;

/**
 * A simple lexer for lexing / tokenizing the HACK assembly language.
 */
public class Lexer {
    private final String input;
    private int curPos;
    private char curChar;

    // flags
    private boolean aInstMode;

    public Lexer(String input) {
        this.input = input + '\n';
        reset();
    }

    /**
     * Resets the lexer to the start position and state.
     */
    public void reset() {
        curPos = -1;
        nextChar();
    }

    /**
     * Assembles the next token from the input.
     *
     * @return the next valid token
     * @throws LexerErrorException if the token wasn't valid
     */
    public Token getToken() throws LexerErrorException {
        skipWhitespace();
        skipComment();

        Token token = null;

        if (aInstMode) {
            aInstMode = false;
            if (Character.isDigit(curChar)) {
                var startPos = curPos;
                while (Character.isDigit(peek())) {
                    nextChar();
                }

                if (Character.isWhitespace(peek())) {
                    token = new Token(TokenType.NUMBER, input.substring(startPos, curPos + 1));
                } else {
                    abortUnexpectedCharacter(peek(), curPos + 1);
                }
            } else {
                if (isSymbolChar(curChar)) {
                    var startPos = curPos;
                    while (isSymbolChar(peek())) { // first char of symbol is not digit, but the following can be
                        nextChar();
                    }

                    if (Character.isWhitespace(peek())) {
                        token = new Token(TokenType.SYMBOL, input.substring(startPos, curPos + 1));
                    } else {
                        abortUnexpectedCharacter(peek(), curPos + 1);
                    }
                } else {
                    abortUnexpectedCharacter(curChar, curPos);
                }
            }
        } else if (curChar == '\0') {
            token = new Token(TokenType.EOF);
        } else if (curChar == '\n') {
            token = new Token(TokenType.NEWLINE);
        } else if (curChar == '@') {
            if (!Character.isWhitespace(peek())) {
                token = new Token(TokenType.A_INST_MARK);
                aInstMode = true; // turn on A-instruction mode - next character will be interpreted differently
            } else {
                abortUnexpectedCharacter(peek(), curPos + 1);
            }
        } else if (curChar == '=') {
            token = new Token(TokenType.DEST_SEPARATOR);
        } else if (curChar == ';') {
            token = new Token(TokenType.JUMP_SEPARATOR);
        } else if (curChar == '(') {
            token = new Token(TokenType.LABEL_START);
        } else if (curChar == ')') {
            token = new Token(TokenType.LABEL_END);
        } else { // LABEL DECLARATION OR C-INSTRUCTION PART
            boolean isValidLabelStart = isSymbolChar(curChar) && !Character.isDigit(curChar);

            var startPos = curPos;

            while (peek() != '='
                    && peek() != ';'
                    && !(peek() == ')' && Character.isWhitespace(peekTo(2))) // end of label
                    && !Character.isWhitespace(peek())
                    && peek() != '\0') {
                nextChar();
            }

            if (peek() == ')') { // LABEL
                var label = input.substring(startPos, curPos + 1);
                if (isValidLabelStart) {
                    token = new Token(TokenType.SYMBOL, label);
                } else {
                    abort("Invalid label: " + label);
                }
            } else { // C-INSTRUCTION PART
                token = new Token(TokenType.COMP_PART, input.substring(startPos, curPos + 1));
            }
        }

        nextChar();
        return token;
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(curChar) && curChar != '\n') {
            nextChar();
        }
    }

    private void skipComment() {
        if (curChar == '/' && peek() == '/') {
            while (peek() != '\n') {
                nextChar();
            }
            nextChar();
        }
    }

    private void nextChar() {
        curPos++;
        if (curPos < input.length()) {
            curChar = input.charAt(curPos);
        } else {
            curChar = '\0';
        }
    }

    private char peek() {
        return peekTo(1);
    }

    private char peekTo(int position) {
        return (curPos + position < input.length())
                ? input.charAt(curPos + position)
                : '\0';
    }

    private boolean isSymbolChar(char aChar) {
        return Character.isDigit(aChar) // 0-9
                || (aChar >= 65 && aChar <= 90) // A-Z
                || (aChar >= 97 && aChar <= 122) // a-z
                || aChar == '_' || aChar == '$' || aChar == '.' || aChar == ':';
    }

    private void abortUnexpectedCharacter(char character, int position) throws LexerErrorException {
        abort("Unexpected character at position " + position + " - " + character);
    }

    private void abort(String message) throws LexerErrorException {
        throw new LexerErrorException(message);
    }
}
