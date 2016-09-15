package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.payroll.Earnings;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.List;

class EmployeeEarnings extends EarningsWidget {

  private final long employeeId;

  EmployeeEarnings(long employeeId) {
    super(ScheduleParent.EMPLOYEE);

    this.employeeId = employeeId;
  }

  @Override
  public String getCaption() {
    BeeRow row = findEmployee(employeeId);

    if (row == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.joinItems(
          BeeUtils.joinWords(DataUtils.getString(getEmData(), row, COL_FIRST_NAME),
              DataUtils.getString(getEmData(), row, COL_LAST_NAME)),
          DataUtils.getString(getEmData(), row, ALS_COMPANY_NAME),
          BeeUtils.joinWords(Localized.getLabel(getEmData().getColumn(COL_TAB_NUMBER)),
              DataUtils.getString(getEmData(), row, COL_TAB_NUMBER)));
    }
  }

  @Override
  protected List<Integer> getPartitionContactIndexes() {
    return Collections.singletonList(getObData().getColumnIndex(COL_ADDRESS));
  }

  @Override
  protected List<BeeColumn> getPartitionDataColumns() {
    return getObData().getColumns();
  }

  @Override
  protected List<Integer> getPartitionInfoIndexes() {
    return Collections.singletonList(getObData().getColumnIndex(ALS_COMPANY_NAME));
  }

  @Override
  protected List<Integer> getPartitionNameIndexes() {
    return Collections.singletonList(getObData().getColumnIndex(COL_LOCATION_NAME));
  }

  @Override
  protected Long getPartitionId(Earnings item) {
    return item.getObjectId();
  }

  @Override
  protected BeeRow getPartitionRow(Earnings item) {
    return findObject(item.getObjectId());
  }

  @Override
  protected long getRelationId() {
    return employeeId;
  }
}
