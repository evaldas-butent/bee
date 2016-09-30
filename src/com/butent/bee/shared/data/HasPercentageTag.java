package com.butent.bee.shared.data;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;

public interface HasPercentageTag {

  String ATTR_PERCENTAGE_TAG = "percentageTag";

  static boolean isPercentage(Double x) {
    return BeeUtils.nonZero(x) && Math.abs(x) <= BeeConst.DOUBLE_ONE_HUNDRED;
  }

  static boolean maybeUpdate(DataInfo dataInfo, IsRow row, Double x, String percentageTag) {
    if (dataInfo == null || row == null || BeeUtils.isEmpty(percentageTag)) {
      return false;
    }

    int index = dataInfo.getColumnIndex(percentageTag);
    if (BeeConst.isUndef(index)) {
      return false;
    }

    boolean oldValue = BeeUtils.isTrue(row.getBoolean(index));
    boolean newValue = isPercentage(x);

    if (oldValue == newValue) {
      return false;
    }

    if (newValue) {
      row.setValue(index, newValue);
    } else {
      row.clearCell(index);
    }

    return true;
  }

  String getPercentageTag();

  void setPercentageTag(String percentageTag);
}
