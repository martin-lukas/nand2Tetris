package com.lukas.hackassembler.model;

public enum TokenType {
    EOF,
    NEWLINE,
    A_INST_MARK,
    SYMBOL,
    NUMBER,
    LABEL_START,
    LABEL_END,
    COMP_PART,
    DEST_SEPARATOR,
    JUMP_SEPARATOR;

    @Override
    public String toString() {
        return this.name();
    }
}
