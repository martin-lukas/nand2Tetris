    @product
    M=0
    @i
    M=0

(LOOP)
    @R0
    D=M
    @i
    D=D-M
    @MOVE_RESULT
    D;JLE

    @R1
    D=M
    @product
    M=D+M
    @i
    M=M+1
    @LOOP
    0;JMP

(MOVE_RESULT)
    @product
    D=M
    @R2
    M=D

(END)
    @END
    0;JMP