package com.lukas.hackassembler.model;

public enum JumpType {
    JGT("JGT", "001"),
    JEQ("JEQ", "010"),
    JGE("JGE", "011"),
    JLT("JLT", "100"),
    JNE("JNE", "101"),
    JLE("JLE", "110"),
    JMP("JMP", "111");

    private final String literal;
    private final String code;

    JumpType(String literal, String code) {
        this.literal = literal;
        this.code = code;
    }

    public static JumpType getByLiteral(String aLiteral) {
        for (var type : values()) {
            if (type.literal.equals(aLiteral)) {
                return type;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }
}
