package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class BeeView {

  static final String JOIN_MASK = "[<>]+";

  private final String name;
  private final String source;
  private final boolean readOnly;
  private Map<String, String> fields = new LinkedHashMap<String, String>();

  BeeView(String name, String source, boolean readOnly) {
    Assert.notEmpty(name);
    Assert.notEmpty(source);

    this.name = name;
    this.source = source;
    this.readOnly = readOnly;
  }

  public Map<String, String> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  public int getIdIndex() {
    if (isReadOnly()) {
      return -1;
    }
    return getFields().size() + 1;
  }

  public int getLockIndex() {
    if (isReadOnly()) {
      return -1;
    }
    return getFields().size();
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

  public boolean isReadOnly() {
    return readOnly;
  }

  void addField(String als, String xpr) {
    Assert.notEmpty(als);
    Assert.notEmpty(xpr);
    fields.put(als, xpr);
  }
}
