import java.util.Arrays;
import java.util.spi.ToolProvider;

/** Run an arbitrary tool with any arguments. */
class Demo1 {
  public static void main(String... args) {
    /* Empty args array given? Show usage message and exit. */ {
      if (args.length == 0) {
        System.err.printf("Usage: %s TOOL-NAME TOOL-ARGS...%n", Demo1.class.getSimpleName());
        System.exit(-1);
      }
    }

    /* Run an arbitrary tool. */ {
      var name = args[0];
      var tool = ToolProvider.findFirst(name).orElseThrow();
      var code = tool.run(System.out, System.err, Arrays.copyOfRange(args, 1, args.length));
      System.out.println("Demo finishes with exit code " + code);
      System.exit(code);
    }
  }
}
