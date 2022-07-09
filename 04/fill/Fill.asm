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

// Put your code here.

(LISTEN)
  // if KBD > 0 goto BLACK
  @KBD
  D=M
  @BLACK
  D;JGT

  // if KBD == 0 goto WHITE
  @KBD
  D=M
  @WHITE
  D;JEQ

  // go to Listen
  @LISTEN
  0;JMP

(BLACK)
  @color
  M=-1

  // if SCREEN == 0 goto PAINT
  @SCREEN
  D=M
  @PAINT
  D;JEQ

  // go to Listen
  @LISTEN
  0;JMP

(WHITE)
  @color
  M=0

  // if SCREEN == -1 goto PAINT
  @SCREEN
  D=M+1
  @PAINT
  D;JEQ

  // go to Listen
  @LISTEN
  0;JMP
  
(PAINT)
  // i = 0
  @i
  M=0

  // n = 8192
  @8192
  D=A
  @n
  M=D

(LOOP)
  // RAM[SCREEN + i] = color
  @SCREEN
  D=A
  @cur
  M=D // cur = SCREEN
  @i
  D=M
  @cur
  M=D+M // cur = cur + i
  @color
  D=M
  @cur
  A=M
  M=D // RAM[cur] = color

  // i++
  @i
  M=M+1

  // if i < n go to LOOP
  D=M
  @n
  D=D-M
  @LOOP
  D; JLT

  // go to Listen
  @LISTEN
  0;JMP
