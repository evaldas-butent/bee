package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

import java.util.List;

public class IsFalseFilter extends Filter {

  protected IsFalseFilter() {
    super();
  }

  @Override
  public void deserialize(String s) {
    setSafe();
  }

  @Override
  public boolean involvesColumn(String colName) {
    return false;
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    return false;
  }

  @Override
  public String serialize() {
    return super.serialize(null);
  }

  @Override
  public String toString() {
    return "FALSE";
  }
}
