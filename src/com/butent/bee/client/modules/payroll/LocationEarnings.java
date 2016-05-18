package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.payroll.Earnings;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

class LocationEarnings extends EarningsWidget {

  private final long objectId;

  LocationEarnings(long objectId) {
    super(ScheduleParent.LOCATION);

    this.objectId = objectId;
  }

  @Override
  public String getCaption() {
    BeeRow row = findObject(objectId);

    if (row == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.joinItems(DataUtils.getString(getObData(), row, COL_LOCATION_NAME),
          DataUtils.getString(getObData(), row, COL_ADDRESS),
          DataUtils.getString(getObData(), row, ALS_COMPANY_NAME));
    }
  }

  @Override
  protected List<Integer> getPartitionContactIndexes() {
    List<Integer> contactIndexes = new ArrayList<>();
    contactIndexes.add(getEmData().getColumnIndex(ALS_DEPARTMENT_NAME));
    contactIndexes.add(getEmData().getColumnIndex(COL_MOBILE));
    contactIndexes.add(getEmData().getColumnIndex(COL_PHONE));
    return contactIndexes;
  }

  @Override
  protected List<BeeColumn> getPartitionDataColumns() {
    return getEmData().getColumns();
  }

  @Override
  protected List<Integer> getPartitionInfoIndexes() {
    List<Integer> infoIndexes = new ArrayList<>();
    infoIndexes.add(getEmData().getColumnIndex(ALS_COMPANY_NAME));
    infoIndexes.add(getEmData().getColumnIndex(COL_TAB_NUMBER));
    return infoIndexes;
  }

  @Override
  protected List<Integer> getPartitionNameIndexes() {
    List<Integer> nameIndexes = new ArrayList<>();
    nameIndexes.add(getEmData().getColumnIndex(COL_FIRST_NAME));
    nameIndexes.add(getEmData().getColumnIndex(COL_LAST_NAME));
    return nameIndexes;
  }

  @Override
  protected Long getPartitionId(Earnings item) {
    return item.getEmployeeId();
  }

  @Override
  protected BeeRow getPartitionRow(Earnings item) {
    return findEmployee(item.getEmployeeId());
  }

  @Override
  protected long getRelationId() {
    return objectId;
  }
}
