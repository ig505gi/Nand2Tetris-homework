import * as fs from 'fs';

class Assembler {
  private filePath: string;
  private originFileContent: string;
  /** 无空行，无注释，无label的指令 */
  private instructions: string[] = [];
  private parser: Parser;
  private code: Code;
  private symbolTable: SymbolTable;
  private machineLanContent: string = '';

  constructor(filePath: string) {
    this.filePath = filePath;
    this.originFileContent = fs.readFileSync(filePath, 'utf-8');
    this.parser = new Parser();
    this.symbolTable = new SymbolTable();
    this.code = new Code(this.symbolTable);
  }

  run() {
    this.initNoLabelInstructions();
    console.log(this.instructions);
    this.translateInstructions();
    this.writeToFile();
  }

  initNoLabelInstructions() {
    const lines = this.originFileContent.split('\n');
    lines.forEach((line, index) => {
      this.parser.init(line);
      switch (this.parser.type()) {
        case 'A':
        case 'C':
          this.instructions.push(this.parser.getEscapeSpaceLine());
          break;
        case 'LABEL':
          if (this.parser.symbol() !== null) {
            this.symbolTable.set(this.parser.symbol()!, this.instructions.length);
          } else {
            throw new Error("somethint wrong!");
          }
          break;
        default:
          break;
      }
    })
  }

  translateInstructions() {
    this.instructions.forEach((instruction, lineNum) => {
      this.parser.init(instruction);
      switch (this.parser.type()) {
        case 'A':
          const value = this.code.num(this.parser.num()!);
          this.machineLanContent += '0' + value;
          break;
        case 'C':
          const comp = this.code.comp(this.parser.comp()!);
          const dest = this.code.dest(this.parser.dest());
          const jump = this.code.jump(this.parser.jump());
          this.machineLanContent += '111' + comp + dest + jump;
          break;
        default:
          throw new Error("somethint wrong!!!");
      }
      if (lineNum !== this.instructions.length - 1) {
        this.machineLanContent += '\n';
      }
    })
  }

  writeToFile() {
    fs.writeFile(this.filePath.replace('asm', 'hack'), this.machineLanContent, err => {
      if (err) {
        console.error(err)
        return;
      }
    })
  }
}

class Parser {
  private line: string = '';
  private _num: string | null = null;
  private _dest: string | null = null;
  private _comp: string | null = null;
  private _jump: string | null = null;
  private _symbol: string | null = null;

  getEscapeSpaceLine() {
    return this.line;
  }

  init(line: string) {
    this.line = line.replaceAll(' ', ''); // 删除所有的空格
    this.line = this.line.replaceAll('\n', ''); // 删除所有的换行
    this.line = this.line.replaceAll('\r', ''); // 删除所有的空格
    this._num = null;
    this._dest = null;
    this._comp = null;
    this._jump = null;
    this._jump = null;
    switch (this.type()) {
      case 'A':
        this.line = this.line.split('//')[0]; // 删除这一行注释后面的东西
        this._num = this.line .slice(1);;
        break;
      case 'C':
        this.line = this.line.split('//')[0]; // 删除这一行注释后面的东西
        const split1 = this.line.split(';');
        if (split1[1] !== undefined) {
          this._jump = split1[1];
        }
        const split2 = split1[0].split('=');
        if (split2[1] !== undefined) {
          this._comp = split2[1];
          this._dest = split2[0];
        } else {
          this._comp = split2[0];
        }
        break;
      case 'LABEL':
        this._symbol = this.line.slice(1, -1);
        break;
      default:
        break;
    }
  }

  num() { return this._num; }
  comp() { return this._comp; }
  dest() { return this._dest; }
  jump() { return this._jump; }
  symbol() { return this._symbol; }

  type(): 'A' | 'C' | 'LABEL' | 'COMMENT' | 'EMPTY' {
    if (this.line == '') return 'EMPTY';
    if (this.line.startsWith('/')) return 'COMMENT';
    if (this.line.startsWith('@')) return 'A';
    if (this.line.startsWith('(')) return 'LABEL';
    return 'C';
  }
}

class Code {
  private DEST_MAP = {
    M: '001',
    D: '010',
    MD: '011',
    A: '100',
    AM: '101',
    AD: '110',
    AMD: '111'
  }
  private JUMP_MAP = {
    JGT: '001',
    JEQ: '010',
    JGE: '011',
    JLT: '100',
    JNE: '101',
    JLE: '110',
    JMP: '111'
  }
  private COMP_MAP = {
    '0': '101010',
    '1': '111111',
    '-1': '111010',
    'D': '001100',
    'A': '110000',
    '!D': '001101',
    '!A': '110001',
    '-D': '001111',
    '-A': '110011',
    'D+1': '011111',
    'A+1': '110111',
    'D-1': '001110',
    'A-1': '110010',
    'D+A': '000010',
    'D-A': '010011',
    'A-D': '000111',
    'D&A': '000000',
    'D|A': '010101'
  }

  private startMemoAddr = 16;
  private symbolTable: SymbolTable;

  constructor(_symbolTable: SymbolTable) {
    this.symbolTable = _symbolTable;
  }

  num(_num: string): string {
    let value: number;
    if (_num.match(/^[0-9]+$/)) {
      value = parseInt(_num);
    } else if (this.symbolTable.has(_num)) {
      value = this.symbolTable.get(_num);
    } else {
      value = this.startMemoAddr++;
      this.symbolTable.set(_num, value);
    }
    return this.numTobit15(value);
  }

  numTobit15(v: number): string {
    let bit15 = '';
    let quotient = v;
    if (v == 0) {
      bit15 = '0';
    } else {
      while (quotient > 1) {
        bit15 = (quotient % 2) + bit15;
        quotient = parseInt((quotient / 2).toString());
      }
      bit15 = '1' + bit15;
    }
    const complementNum = 15 - bit15.length;
    for (let i = 0; i < complementNum; i++) {
      bit15 = '0' + bit15;
    }
    return bit15;
  }

  dest(_dest: string | null): string {
    if (_dest === null) return '000';
    return this.DEST_MAP[_dest as keyof typeof this.DEST_MAP];
  }

  comp(_comp: string): string {
    if (_comp.includes('M')) {
      const key = _comp.replace('M', 'A');
      return '1' + this.COMP_MAP[key as keyof typeof this.COMP_MAP];
    }
    return '0' + this.COMP_MAP[_comp as keyof typeof this.COMP_MAP];
  }

  jump(_jump: string | null): string {
    if (_jump === null) return '000';
    return this.JUMP_MAP[_jump as keyof typeof this.JUMP_MAP];
  }

}

class SymbolTable {
  private map = new Map();

  constructor() {
    // R0-R15
    for (let i = 0; i < 16; i++) {
      this.set(`R${i}`, i);
    }
    this.set('SCREEN', 16384);
    this.set('KBD', 24576);
    this.set('SP', 0);
    this.set('LCL', 1);
    this.set('ARG', 2);
    this.set('THIS', 3);
    this.set('THAT', 4);
  }

  get(symbol: string): number {
    return this.map.get(symbol);
  }

  set(symbol: string, value: number): void {
    this.map.set(symbol, value);
  }

  has(symbol: string): boolean {
    return this.map.has(symbol);
  }
}


try {
  const filePath = process.argv[2];
  const assembler = new Assembler(filePath);
  assembler.run();
} catch (e) {
  console.log(e);
}
