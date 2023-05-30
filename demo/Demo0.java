import java.util.spi.ToolProvider;

/** Use tool provider to find and run javac tool. */
class Demo0 {
  public static void main(String... args) {
    var tool = ToolProvider.findFirst("javac").orElseThrow();
    var code = tool.run(System.out, System.err, "--version");
    System.exit(code);
  }
}
