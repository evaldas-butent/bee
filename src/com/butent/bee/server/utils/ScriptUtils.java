package com.butent.bee.server.utils;

import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public final class ScriptUtils {

  private static final String JS_ENGINE_NAME = "js";

  public static List<Property> getEngineInfo() {
    List<Property> result = new ArrayList<>();

    ScriptEngineManager manager = new ScriptEngineManager();

    for (ScriptEngineFactory factory : manager.getEngineFactories()) {
      PropertyUtils.addProperties(result,
          "Engine Name", factory.getEngineName(),
          "Engine Version", factory.getEngineVersion(),
          "Extensions", factory.getExtensions(),
          "Language Name", factory.getLanguageName(),
          "Language Version", factory.getLanguageVersion(),
          "Method Call Syntax", factory.getMethodCallSyntax("obj", "method", "p1", "p2", "p3"),
          "Mime Types", factory.getMimeTypes(),
          "Names", factory.getNames(),
          "Output Statement", factory.getOutputStatement("input"),
          "Program", factory.getProgram("statement 1", "statement 2", "statement 3"));
    }

    return result;
  }

  public static ResponseObject eval(String script) {
    if (BeeUtils.isEmpty(script)) {
      return ResponseObject.warning("script is empty");
    }

    ScriptEngine engine = getEngine();
    if (engine == null) {
      return ResponseObject.error("script engine", JS_ENGINE_NAME, "not available");
    }

    try {
      Object result = engine.eval(script);
      return ResponseObject.response(result);

    } catch (ScriptException ex) {
      return ResponseObject.error(ex, script);
    }
  }

  public static ScriptEngine getEngine() {
    ScriptEngineManager manager = new ScriptEngineManager();
    return manager.getEngineByName(JS_ENGINE_NAME);
  }

  private ScriptUtils() {
  }
}
