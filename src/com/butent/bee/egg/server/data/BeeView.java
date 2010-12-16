package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BeeView {

  static final String JOIN_MASK = "[<>]+";

  private final String name;
  private final String source;
  private Map<String, String> fields = new LinkedHashMap<String, String>();

  BeeView(String name, String source) {
    Assert.notEmpty(name);
    Assert.notEmpty(source);

    this.name = name;
    this.source = source;
  }

  public Map<String, String> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  public String getName() {
    return name;
  }

  public String getSource() {
    return source;
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getFields());
  }

  void addField(String fld, String als) {
    if (!BeeUtils.isEmpty(fld)) {
      fields.put(BeeUtils.ifString(als, fld.replaceAll(JOIN_MASK, "")), fld);
    }
  }
}
