import java.io.PrintWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.spi.ToolProvider;
import java.util.stream.Stream;

class Demo {
  public static void main(String... args) {
    var runner = new ToolRunner().uses("javac").uses("jar").uses("jlink");
    runner.run("javac", "--version");
    runner.run("jar", "--version");

    var modules = List.of("org.example", "org.example.app", "org.example.lib");
    runner.run(
        """
        javac
          --module %s
          --module-source-path .
          -d out/classes
        """
            .formatted(String.join(",", modules)));
    for (var module : modules) {
      runner.run(
          """
          jar
            --create
            --file out/modules/%s.jar
            -C out/classes/%s .
          """
              .formatted(module, module));
    }

    runner.run(
        """
        jlink
          --verbose
          --output out/image-%s
          --module-path out/modules
          --add-modules org.example
          --launcher example=org.example.app/org.example.app.Main
        """
            .formatted(Instant.now().getEpochSecond()));
  }

  record ToolRunner(PrintWriter out, PrintWriter err, ToolFinder finder, List<ToolProvider> tools)
      implements ToolFinder {
    ToolRunner() {
      this(
          new PrintWriter(System.out, true),
          new PrintWriter(System.err, true),
          ToolProvider::findFirst,
          List.of());
    }

    ToolRunner uses(String name) {
      var tool = finder.getTool(name);
      return uses(tool);
    }

    ToolRunner uses(ToolProvider tool) {
      var combined = Stream.concat(tools.stream(), Stream.of(tool)).toList();
      return new ToolRunner(out, err, finder, combined);
    }

    @Override
    public Optional<ToolProvider> findTool(String name) {
      return tools.stream().filter(tool -> tool.name().equals(name)).findFirst();
    }

    void run(String command) {
      var lines =
          command
              .lines()
              .map(String::trim)
              .map(line -> line.split("\\s"))
              .flatMap(Stream::of)
              .toArray(String[]::new);
      var tool = lines[0];
      var args = Arrays.copyOfRange(lines, 1, lines.length);
      run(tool, args);
    }

    void run(String tool, String... args) {
      run(getTool(tool), args);
    }

    void run(ToolProvider provider, String... args) {
      out.println("| " + provider.name() + " " + String.join(" ", args));
      var code = provider.run(out, err, args);
      if (code == 0) return;
      throw new RuntimeException("Non-zero exit code from " + provider.name());
    }
  }

  @FunctionalInterface
  interface ToolFinder {
    Optional<ToolProvider> findTool(String name);

    default ToolProvider getTool(String name) {
      var found = findTool(name);
      if (found.isPresent()) return found.get();
      throw new IllegalArgumentException("Tool not found: " + name);
    }
  }
}
