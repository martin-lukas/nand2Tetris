package com.lukas.hackassembler.model;

public enum CompType {
    ZERO("0", "101010"),
    ONE("1", "111111"),
    MINUS_ONE("-1", "111010"),
    D("D", "001100"),
    A("A", "110000"),
    M("M", "110000"),
    NOT_D("!D", "001101"),
    NOT_A("!A", "110001"),
    NOT_M("!M", "110001"),
    MINUS_D("-D", "001111"),
    MINUS_A("-A", "110011"),
    MINUS_M("-M", "110011"),
    D_PLUS_ONE("D+1", "011111"),
    A_PLUS_ONE("A+1", "110111"),
    M_PLUS_ONE("M+1", "110111"),
    D_MINUS_ONE("D-1", "001110"),
    A_MINUS_ONE("A-1", "110010"),
    M_MINUS_ONE("M-1", "110010"),
    D_PLUS_A("D+A", "000010"),
    D_PLUS_M("D+M", "000010"),
    D_MINUS_A("D-A", "010011"),
    D_MINUS_M("D-M", "010011"),
    A_MINUS_D("A-D", "000111"),
    M_MINUS_D("M-D", "000111"),
    D_AND_A("D&A", "000000"),
    D_AND_M("D&M", "000000"),
    D_OR_A("D|A", "010101"),
    D_OR_M("D|M", "010101");

    private final String literal;
    private final String code;

    CompType(String literal, String code) {
        this.literal = literal;
        this.code = code;
    }

    public static CompType getByLiteral(String aLiteral) {
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
