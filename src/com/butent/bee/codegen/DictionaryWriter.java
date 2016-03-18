package com.butent.bee.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class DictionaryWriter {

  private static final char PARAMETER_PREFIX = '{';
  private static final char PARAMETER_SUFFIX = '}';

  public static void main(String[] args) {
    run();
  }

  /**
   * 
   */
  public static void run() {
    Map<String, String> properties =
        readProperties("war/WEB-INF/config/dictionaries/dictionary_en.properties");
    if (properties.isEmpty()) {
      return;
    }

    List<String> lines = new ArrayList<>();

    lines.add("//@formatter:off");
    lines.add("// CHECKSTYLE:OFF");
    lines.add("package com.butent.bee.shared.i18n;");
    lines.add("");
    lines.add("import java.util.HashMap;");
    lines.add("import java.util.Map;");
    lines.add("");
    lines.add("@FunctionalInterface");
    lines.add("public interface Dictionary {");

    lines.add("");
    lines.add(indent(2, "String g(String key);"));

    List<String> keys = new ArrayList<>(properties.keySet());
    keys.sort(null);

    for (String key : keys) {
      List<String> parameters = extractParameters(properties.get(key));

      lines.add("");
      if (parameters.isEmpty()) {
        lines.add(indent(2, "default String " + key + "() {return g(\"" + key + "\");}"));
      } else {
        lines.addAll(buildMessage(key, parameters));
      }
    }

    lines.add("}");
    lines.add("//@formatter:on");

    Path out = Paths.get("src/com/butent/bee/shared/i18n/Dictionary.java");

    try {
      Files.write(out, lines, Charset.defaultCharset());
      System.out.println("" + lines.size() + "  " + out.toString());
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private static List<String> buildMessage(String key, List<String> parameters) {
    List<String> lines = new ArrayList<>();
    lines.add(indent(4, "Map<String, Object> _m = new HashMap<>();"));

    StringBuilder sb = new StringBuilder();
    sb.append("default String " + key + "(");

    for (int i = 0; i < parameters.size(); i++) {
      String x = parameters.get(i);
      String y = Character.isDigit(x.charAt(0)) ? ("p" + x) : x;

      if (i > 0) {
        sb.append(", ");
      }
      sb.append("Object " + y);

      lines.add(indent(4,
          "_m.put(\"" + PARAMETER_PREFIX + x + PARAMETER_SUFFIX + "\", " + y + ");"));
    }

    sb.append(") {");
    lines.add(0, indent(2, sb.toString()));

    lines.add(indent(4, "return Localized.format(g(\"" + key + "\"), _m);"));
    lines.add(indent(2, "}"));

    return lines;
  }

  private static List<String> extractParameters(String s) {
    List<String> result = new ArrayList<>();

    if (s != null && s.indexOf(PARAMETER_PREFIX) >= 0 && s.indexOf(PARAMETER_SUFFIX) > 1) {
      int start = -1;

      for (int i = 0; i < s.length(); i++) {
        if (s.charAt(i) == PARAMETER_PREFIX) {
          if (i == 0 || s.charAt(i - 1) != '\\') {
            start = i;
          }

        } else if (s.charAt(i) == PARAMETER_SUFFIX && start >= 0 && i > start + 1) {
          String p = s.substring(start + 1, i);
          if (isParameterName(p) && !result.contains(p)) {
            result.add(p);
          }

          start = -1;
        }
      }
    }

    return result;
  }

  private static boolean isParameterName(String p) {
    if (p == null || p.isEmpty()) {
      return false;

    } else {
      for (int i = 0; i < p.length(); i++) {
        if (!Character.isLetterOrDigit(p.charAt(i))) {
          return false;
        }
      }
      return true;
    }
  }

  private static Map<String, String> readProperties(String path) {
    Map<String, String> result = new HashMap<>();

    File file = new File(path);
    if (!file.exists()) {
      System.err.println(path + " not found");
      return result;
    }

    Properties properties = new Properties();

    try (InputStreamReader reader =
        new InputStreamReader(new FileInputStream(file), Charset.defaultCharset())) {

      properties.load(reader);
      for (String name : properties.stringPropertyNames()) {
        result.put(name, properties.getProperty(name));
      }

    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return result;
  }

  private static String indent(int n, String s) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < n; i++) {
      sb.append(' ');
    }
    sb.append(s);

    return sb.toString();
  }

  private DictionaryWriter() {
  }
}
