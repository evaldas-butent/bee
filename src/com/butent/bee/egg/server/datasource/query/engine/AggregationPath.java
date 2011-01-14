package com.butent.bee.egg.server.datasource.query.engine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.butent.bee.egg.shared.data.value.Value;

import java.util.Collections;
import java.util.List;

public class AggregationPath {
  private List<Value> values;

  public AggregationPath() {
    values = Lists.newArrayList();
  }

  public void add(Value value) {
    values.add(value);
  }

  public List<Value> getValues() {
    return ImmutableList.copyOf(values);
  }

  public void reverse() {
    Collections.reverse(values);
  }
}
