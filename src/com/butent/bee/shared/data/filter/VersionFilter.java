package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.LongValue;

import java.util.List;

/**
 * Enables to compare VERSION column against some value.
 */
public class VersionFilter extends ColumnValueFilter {

  protected VersionFilter() {
    super();
  }

  protected VersionFilter(Operator operator, long value) {
    super(DataUtils.VERSION_TAG, operator, new LongValue(value));
  }

  @Override
  public boolean involvesColumn(String colName) {
    return false;
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    return isOperatorMatch(new LongValue(row.getVersion()), getValue());
  }
}
