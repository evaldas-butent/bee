package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

import java.util.List;

public class IsTrueFilter extends Filter {

  protected IsTrueFilter() {
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
    return true;
  }

  @Override
  public String serialize() {
    return super.serialize(null);
  }

  @Override
  public String toString() {
    return "1=1";
  }
}
