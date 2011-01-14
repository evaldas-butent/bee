package com.butent.bee.egg.server.datasource.query.engine;

import com.butent.bee.egg.shared.data.value.Value;

import java.util.List;

class RowTitle {
  public List<Value> values;

  public RowTitle(List<Value> values) {
    this.values = values;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RowTitle)) {
      return false;
    }
    RowTitle other = (RowTitle) o;
    return values.equals(other.values);
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }
}
