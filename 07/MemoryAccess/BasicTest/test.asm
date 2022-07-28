// add
// @0
// M=M-1
// A=M
// D=M
// @0
// A=M-1
// M=M+D

//NEG
// @0
// A=M-1
// M=-M

//EQ
// @0
// AM=M-1
// D=M
// A=A-1
// D=M-D
// @TRUE_1
// D;JEQ
// @0
// A=M-1
// M=-1
// @CONTINUE_1
// 0;JMP
// (TRUE_1)
// @0
// A=M-1
// M=0
// (CONTINUE_1)

// push pointer 0 假设this是3
@3
D=M
@SP // *SP=D
A=M
M=D
@SP
M=M+1

// POP STATIC N