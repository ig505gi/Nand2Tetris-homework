package vmtranslator;

import java.io.*;

/**
 * VMtranslator
 */
public class Main {

  public static void main(String[] args) throws Exception {
    // 接受入参fileName.vm, 输出fieName.asm
    if (args.length < 1) {
      System.out.println("please input vm file name");
      return;
    }
    String fileName = args[0];
    Parser parser = new Parser(new File(fileName));
    String outFileName = fileName.substring(0, fileName.length() - 2) + "asm";
    CodeWriter codeWriter = new CodeWriter(new File(outFileName));
    while (parser.hasMoreCommands()) {
      parser.advance();
      codeWriter.writeComment(parser.currentCommandLine);
      switch (parser.commandType()) {
        case C_ARITHMETIC:
          codeWriter.writeArithmetic(parser.arg1());
          break;
        case C_PUSH:
        case C_POP:
          codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
          break;
        default:
          break;
      }
    }
    codeWriter.close();
    System.out.println(outFileName);
  }
}