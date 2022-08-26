package vmtranslator;

import java.io.*;
import java.util.HashMap;

public class CodeWriter {
  private BufferedWriter writer;
  private String FILE_NAME;
  private int TRUE_LABEL_COUNT = 0;
  // 1.在每个function中的label需要加前缀变成：fileName.functionName$label
  // 2.vm文件一般由Jack文件得到，得到的functionName已经是fileName.functionName了
  // 3.对于特殊的vm文件，没有function的，那么则直接调用label。
  // 4.由于不会出现function和return之间再次出现function的情况:
  // 因此当调用writeFunction时，记录当前的functionName;当调用writeReturn时，删除名字信息
  private String curFunctionName = "";

  public CodeWriter(File file) throws IOException {
    FILE_NAME = file.getName().substring(0, file.getName().length() - 4);
    writer = new BufferedWriter(new FileWriter(file));
  }

  public void setFileName(String fileName) {
    // do nothing
    // 在构造函数里已经做了
  }

  public void writeInit() throws IOException {
    writer.write("sp=256\ncall Sys.init");
  }

  public void writeComment(String command) throws IOException {
    writer.write("// " + command + "\n");
  }

  public void writeLabel(String label) throws IOException {
    writer.write("(" + curFunctionName + "$" + label + ")\n");
  }

  public void writeGoto(String label) throws IOException {
    writer.write(
      "@" + curFunctionName + "$" + label + "\n" +
      "0;JMP\n"
    );
  }

  public void writeIf(String label) throws IOException {
    writer.write(
      // 如果是true，全为1，那么为负数，应该用JLT
      popStackToD() +
      "@" + curFunctionName + "$" + label + "\n" +
      "D;JLT\n"
    );
  }

  public void writeFunction(String functionName, int numVars) throws IOException {
    
  }

  public void writeCall(String functionName, int numVars) throws IOException {
    
  }

  public void writeReturn() throws IOException {

  }

  public void writeArithmetic(String command) throws Exception {
    switch (command) {
      case "add":
        writer.write(arithmeticTemplate1("M=D+M"));
        break;
      case "sub":
        writer.write(arithmeticTemplate1("M=M-D"));
        break;
      case "and":
        writer.write(arithmeticTemplate1("M=D&M"));
        break;
      case "or":
        writer.write(arithmeticTemplate1("M=D|M"));
        break;
      case "neg":
        writer.write(arithmeticTemplate2("M=-M"));
        break;
      case "not":
        writer.write(arithmeticTemplate2("M=!M"));
        break;
      case "eq":
        writer.write(arithmeticTemplate3("JEQ"));
        break;
      case "gt":
        writer.write(arithmeticTemplate3("JGT"));
        break;
      case "lt":
        writer.write(arithmeticTemplate3("JLT"));
        break;
      default:
        throw new Exception("use arithmeticTemplate incorrectly!");
    }
  }

  public void writePushPop(Command command, String segment, int index) throws Exception {
    switch (segment) {
      case "constant":
        if (command == Command.C_PUSH) {
          writer.write(pushConstantTemplate(index));
        } else {
          throw new Exception("no pop constant command!");
        }
        break;
      case "static":
        if (command == Command.C_PUSH) {
          writer.write(pushStaticTemplate(index));
        } else {
          writer.write(popStaticTemplate(index));
        }
        break;
      case "pointer":
        if (command == Command.C_PUSH) {
          writer.write(pushPointerTemplate(index > 0 ? "THAT" : "THIS"));
        } else {
          writer.write(popPointerTemplate(index > 0 ? "THAT" : "THIS"));
        }
        break;
      case "local":
      case "this":
      case "that":
      case "argument":
      case "temp":
        if (command == Command.C_PUSH) {
          writer.write(pushCommonTemplate(getSegmentLabel(segment), index));
        } else {
          writer.write(popCommonTemplate(getSegmentLabel(segment), index));
        }
        break;
      default:
        throw new Exception("no this segement: " + "\"" + segment + "\"!");
    }
  }

  private String pushCommonTemplate(String segmentLabel, int index) {
    return "@" + index + "\n" +
        "D=A\n" +
        "@" + segmentLabel + "\n" +
        (segmentLabel.equals("5") ? "A=A+D\n" : "A=M+D\n") + // temp这里比较特殊
        "D=M\n" +
        pushDToStack();
  }

  private String popCommonTemplate(String segmentLabel, int index) {
    return "@" + index + "\n" +
        "D=A\n" +
        "@" + segmentLabel + "\n" +
        (segmentLabel.equals("5") ? "D=A+D\n" : "D=M+D\n") + // temp这里比较特殊
        "@R13\n" + // 暂存
        "M=D\n" +
        popStackToD() +
        "@R13\n" +
        "A=M\n" +
        "M=D\n";
  }

  private String getSegmentLabel(String segment) throws Exception {
    HashMap<String, String> labelMap = new HashMap<String, String>();
    labelMap.put("local", "LCL");
    labelMap.put("this", "THIS");
    labelMap.put("that", "THAT");
    labelMap.put("argument", "ARG");
    labelMap.put("temp", "5");
    if (labelMap.containsKey(segment)) {
      return labelMap.get(segment);
    }
    throw new Exception("no this segment!");
  }

  private String popPointerTemplate(String thisOrThat) {
    return popStackToD() +
        "@" + thisOrThat + "\n" +
        "M=D\n";
  }

  private String pushPointerTemplate(String thisOrThat) {
    return "@" + thisOrThat + "\n" +
        "D=M\n" +
        pushDToStack();
  }

  private String popStaticTemplate(int index) {
    return popStackToD() +
        "@" + FILE_NAME + "." + index + "\n" +
        "M=D\n";
  }

  private String pushStaticTemplate(int index) {
    return "@" + FILE_NAME + "." + index + "\n" +
        "D=M\n" +
        pushDToStack();
  }

  private String pushConstantTemplate(int index) {
    return "@" + index + "\n" +
        "D=A\n" +
        pushDToStack();
  }

  /** *sp=D; sp++ */
  private String pushDToStack() {
    return "@SP\n" +
        "A=M\n" +
        "M=D\n" +
        "@SP\n" +
        "M=M+1\n";
  }

  /** sp--; D=*sp */
  private String popStackToD() {
    return "@SP\n" +
        "AM=M-1\n" +
        "D=M\n";
  }

  /** for add sub and or */
  private String arithmeticTemplate1(String lastInstruction) throws Exception {
    StringBuffer sb = new StringBuffer();
    sb.append("@SP\n")
        .append("AM=M-1\n")
        .append("D=M\n")
        .append("A=A-1\n")
        .append(lastInstruction + "\n");
    return sb.toString();
  }

  /** neg not */
  private String arithmeticTemplate2(String lastInstruction) {
    StringBuffer sb = new StringBuffer();
    sb.append("@SP\n")
        .append("A=M-1\n")
        .append(lastInstruction + "\n");
    return sb.toString();
  }

  /** for eq gt lt */
  private String arithmeticTemplate3(String jump) {
    StringBuffer sb = new StringBuffer();
    sb.append("@SP\n")
        .append("AM=M-1\n")
        .append("D=M\n")
        .append("A=A-1\n")
        .append("D=M-D\n")
        .append("@TRUE_" + TRUE_LABEL_COUNT + "\n")
        .append("D;" + jump + "\n")
        .append("@SP\n")
        .append("A=M-1\n")
        .append("M=0\n")
        .append("@CONTINUE_" + TRUE_LABEL_COUNT + "\n")
        .append("0;JMP\n")
        .append("(TRUE_" + TRUE_LABEL_COUNT + ")\n")
        .append("@SP\n")
        .append("A=M-1\n")
        .append("M=-1\n")
        .append("(CONTINUE_" + TRUE_LABEL_COUNT + ")\n");
    TRUE_LABEL_COUNT++;
    return sb.toString();
  }

  public void close() throws IOException {
    writer.close();
  }
}
