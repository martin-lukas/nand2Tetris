// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

    @pixels_value
    M=0
    @last_key
    M=0

// Resets the variables and jumps to keyboard scanning.
(RESET_POINTER)
    @SCREEN
    D=A
    @pixel_group
    M=D
    @SCAN_KBD
    0;JMP

// Scan keyboard input.
(SCAN_KBD)
    @KBD
    D=M
    @last_key
    D=D-M
    @HANDLE_DRAW
    D;JNE
    @SCAN_KBD
    0;JMP

// Decides what to draw on screen.
(HANDLE_DRAW)
    @KBD
    D=M
    @last_key
    M=D
    @FILL
    D;JNE
    @WIPE
    0;JMP

// Sets up variables for filling the screen.
(FILL)
    @pixels_value
    M=-1
    @DRAW
    0;JMP

// Sets up variables for emptying the screen.
(WIPE)
    @pixels_value
    M=0
    @DRAW
    0;JMP

// Fills the screen with predefined value, and then jumps to keyboard scanning again.
(DRAW)
    @KBD
    D=A
    @pixel_group
    D=D-M
    @RESET_POINTER
    D;JLE

    @pixels_value
    D=M
    @pixel_group
    A=M
    M=D

    @pixel_group
    M=M+1
    @DRAW
    0;JMP