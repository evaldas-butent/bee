package com.butent.bee.client.js;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(namespace = JsPackage.GLOBAL, name = "micromarkdown")
public final class Markdown {

  private static final String LINE_BREAK = "<br/>";

  public static native String parse(String input);

  public static String toHtml(String input) {
    if (BeeUtils.isEmpty(input)) {
      return input;
    }

    String s = BeeUtils.removePrefixAndSuffix(parse(input), BeeConst.CHAR_EOL);
    if (BeeUtils.equalsTrim(s, input)) {
      return input;
    }

    if (s.contains(LINE_BREAK)) {
      s = BeeUtils.replace(s, LINE_BREAK, BeeConst.STRING_EMPTY);
    }

    return s;
  }

  private Markdown() {
  }
}
