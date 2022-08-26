
package vmtranslator;

import java.io.*;
import java.util.*;

public class Parser {
  private Scanner scanner;
  private String[] currentCommand; // split by space
  public String currentCommandLine;
  private String nextCommandLine;
  public Parser(File file) throws FileNotFoundException {
    scanner = new Scanner(file);
  }

  private boolean isCommentOrBlank(String line) {
    String trimedLine = line.replaceAll(" ", "");
    return trimedLine.startsWith("/") || trimedLine.startsWith("*") || trimedLine.equals("");
  }

  public Boolean hasMoreCommands() {
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      if (!isCommentOrBlank(line)) {
        nextCommandLine = line;
        return true;
      }
    }
    scanner.close();
    return false;
  }

  public void advance() {
    currentCommand = nextCommandLine.split(" ");
    currentCommandLine = nextCommandLine;
  }

  public Command commandType() throws Exception {
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
    if (currentCommand[0].equals("label")) {
      return Command.C_LABEL;
    }
    if (currentCommand[0].equals("goto")) {
      return Command.C_GOTO;
    }
    if (currentCommand[0].equals("if-goto")) {
      return Command.C_IF;
    }
    if (currentCommand[0].equals("function")) {
      return Command.C_FUNCTION;
    }
    if (currentCommand[0].equals("call")) {
      return Command.C_CALL;
    }
    if (currentCommand[0].equals("function")) {
      return Command.C_RETURN;
    }
    throw new Exception("incorrect syntax!");
  }

  public String arg1() throws Exception {
    switch (commandType()) {
      case C_ARITHMETIC:
        return currentCommand[0];
      case C_PUSH:
      case C_POP:
      case C_FUNCTION:
      case C_CALL:
      case C_GOTO:
      case C_IF:
      case C_LABEL:
        return currentCommand[1];
      default:
        break;
    }
    // C_RETURN shouldn't call
    throw new Exception("incorrect syntax!");
  }

  public int arg2() throws NumberFormatException, Exception {
    switch (commandType()) {
      case C_PUSH:
      case C_POP:
      case C_FUNCTION:
      case C_CALL:
        return Integer.parseInt(currentCommand[2]);
      default:
        break;
    }
    throw new Exception("incorrect syntax!");
  }
}

enum Command {
  C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL
}
