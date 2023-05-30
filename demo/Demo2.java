import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.spi.ToolProvider;
import java.util.stream.Stream;

/** Towards an "init-run-done" style application. */
class Demo2 {
  public static void main(String... args) {
    /* Init. */
    var runner = new ToolRunner().uses("javac").uses("jar");

    /* Run. */
    var status = runner.run(args);

    /* Done. */
    System.exit(status);
  }

  record ToolRunner(PrintWriter out, PrintWriter err, List<ToolProvider> uses) {
    ToolRunner() {
      this(new PrintWriter(System.out, true), new PrintWriter(System.err, true), List.of());
    }

    ToolRunner uses(String tool) {
      return uses(ToolProvider.findFirst(tool).orElseThrow());
    }

    ToolRunner uses(ToolProvider tool) {
      return new ToolRunner(out, err, Stream.concat(uses.stream(), Stream.of(tool)).toList());
    }

    ToolProvider getTool(String name) {
      var found = uses.stream().filter(provider -> provider.name().equals(name)).findFirst();
      if (found.isPresent()) return found.get();
      throw new IllegalArgumentException("Tool not found: " + name);
    }

    int run(String... args) {
      /* Empty args array given? Show usage message and exit. */ {
        if (args.length == 0) {
          err.printf("Usage: %s TOOL-NAME TOOL-ARGS...%n", Demo2.class.getSimpleName());
          err.println();
          uses.stream().map(ToolProvider::name).sorted().forEach(System.err::println);
          return -1;
        }
      }

      return run(getTool(args[0]), Arrays.copyOfRange(args, 1, args.length));
    }

    void run(String tool, String... args) {
      var code = run(getTool(tool), args);
      if (code == 0) return;
      throw new RuntimeException("Non-zero exit code: " + code);
    }

    int run(ToolProvider tool, String... args) {
      return tool.run(out, err, args);
    }
  }
}
