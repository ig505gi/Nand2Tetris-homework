// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

// Put your code here.

  // sum = 0
  @sum
  M=0

  // small = R0
  @R0
  D=M
  @small
  M=D

  // if samll == 0 goto ENDLOOP
  @small
  D=M
  @ENDLOOP
  D;JEQ

  // large = R1
  @R1
  D=M
  @large
  M=D

  // if large == 0 goto ENDLOOP
  @large
  D=M
  @ENDLOOP
  D;JEQ

  // if small <= large goto LOOP
  @small
  D=M
  @large
  D=D-M
  @LOOP
  D;JLE

  // swap large and small
  @large
  D=M
  @temp
  M=D // temp = large
  @small
  D=M
  @large
  M=D // large = samll
  @temp
  D=M
  @small
  M=D // small = temp

(LOOP)
  // if small == 0 goto ENDLOOP
  @small
  D=M
  @ENDLOOP
  D;JEQ

  // sum = sum + large
  @large
  D=M
  @sum
  M=M+D

  // small--
  @small
  M=M-1

  // goto LOOP
  @LOOP
  0;JMP

(ENDLOOP)
  // R[2] = sum
  @sum
  D=M
  @R2
  M=D

(END)
  @END
  0;JMP
