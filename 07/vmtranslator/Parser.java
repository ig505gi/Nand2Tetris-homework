
package vmtranslator;

import java.io.*;
import java.util.*;

public class Parser {
  private Scanner scanner;
  private String[] currentCommand; // split by space
  private String nextCommandLine;
  public Parser(FileInputStream is) {
    scanner = new Scanner(is);
  }

  private boolean isCommentOrBlank(String line) {
    return line.startsWith("/") || line.startsWith("*") || line == "";
  }

  public Boolean hasMoreCommands() {
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      if (!isCommentOrBlank(line)) {
        nextCommandLine = line;
        return true;
      }
    }
    return false;
  }

  public void advance() {
    currentCommand = nextCommandLine.split(" ");
  }

  public Command commandType() {
    if (currentCommand[0].equals("pop")) {
      return Command.C_POP;
    }
    if (currentCommand[0].equals("push")) {
      return Command.C_PUSH;
    }
    String[] arithmeticTypes = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};
    if (Arrays.asList(arithmeticTypes).contains(currentCommand[0])) {
      return Command.C_ARITHMETIC;
    }
    return Command.C_RETURN;
  }

  public String arg1() {
    switch (commandType()) {
      case C_ARITHMETIC:
        return currentCommand[0];
      case C_PUSH:
      case C_POP:
        return currentCommand[1];
      default:
        break;
    }
    return "";
  }

  public int arg2() {
    switch (commandType()) {
      case C_PUSH:
      case C_POP:
        return Integer.parseInt(currentCommand[2]);
      default:
        break;
    }
    return 0;
  }
}

enum Command {
  C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL
}
