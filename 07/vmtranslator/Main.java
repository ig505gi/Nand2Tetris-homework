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
    Parser parser = new Parser(new FileInputStream(new File(fileName)));
    String outFileName = fileName.substring(0, fileName.length() - 2) + "asm";
    CodeWriter codeWriter = new CodeWriter(new FileOutputStream(new File(outFileName)));
    while (parser.hasMoreCommands()) {
      parser.advance();
      System.out.println(parser.arg1() + "  " + parser.arg2());
    }

    System.out.println(outFileName);
  }
}