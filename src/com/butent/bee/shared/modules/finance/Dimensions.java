package com.butent.bee.shared.modules.finance;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

import java.util.List;

public final class Dimensions {

  private static final String COL_DEPARTMENT = "Department";
  private static final String COL_ACTIVITY_TYPE = "ActivityType";
  private static final String COL_COST_CENTER = "CostCenter";
  private static final String COL_OBJECT = "Object";

  public static Dimensions create(BeeRowSet rowSet, IsRow row) {
    Assert.notNull(rowSet);
    return create(rowSet.getColumns(), row);
  }

  public static Dimensions create(List<? extends IsColumn> columns, IsRow row) {
    Assert.notEmpty(columns);
    Assert.notNull(row);

    Long department = DataUtils.getLong(columns, row, COL_DEPARTMENT);
    Long activityType = DataUtils.getLong(columns, row, COL_ACTIVITY_TYPE);
    Long costCenter = DataUtils.getLong(columns, row, COL_COST_CENTER);
    Long object = DataUtils.getLong(columns, row, COL_OBJECT);

    return new Dimensions(department, activityType, costCenter, object);
  }

  public static Dimensions merge(List<Dimensions> list) {
    Assert.notEmpty(list);

    Long department = null;
    Long activityType = null;
    Long costCenter = null;
    Long object = null;

    for (Dimensions dim : list) {
      if (dim != null && !dim.isEmpty()) {
        if (department == null) {
          department = dim.getDepartment();
        }
        if (activityType == null) {
          activityType = dim.getActivityType();
        }
        if (costCenter == null) {
          costCenter = dim.getCostCenter();
        }
        if (object == null) {
          object = dim.getObject();
        }

        if (department != null && activityType != null && costCenter != null && object != null) {
          break;
        }
      }
    }

    return new Dimensions(department, activityType, costCenter, object);
  }

  private final Long department;
  private final Long activityType;
  private final Long costCenter;
  private final Long object;

  private Dimensions(Long department, Long activityType, Long costCenter, Long object) {
    this.department = department;
    this.activityType = activityType;
    this.costCenter = costCenter;
    this.object = object;
  }

  public void applyTo(List<? extends IsColumn> columns, IsRow row) {
    Assert.notEmpty(columns);
    Assert.notNull(row);

    if (getDepartment() != null) {
      row.setValue(DataUtils.getColumnIndex(COL_DEPARTMENT, columns), getDepartment());
    }
    if (getActivityType() != null) {
      row.setValue(DataUtils.getColumnIndex(COL_ACTIVITY_TYPE, columns), getActivityType());
    }
    if (getCostCenter() != null) {
      row.setValue(DataUtils.getColumnIndex(COL_COST_CENTER, columns), getCostCenter());
    }
    if (getObject() != null) {
      row.setValue(DataUtils.getColumnIndex(COL_OBJECT, columns), getObject());
    }
  }

  public Long getDepartment() {
    return department;
  }

  public Long getActivityType() {
    return activityType;
  }

  public Long getCostCenter() {
    return costCenter;
  }

  public Long getObject() {
    return object;
  }

  public boolean isEmpty() {
    return department == null && activityType == null && costCenter == null && object == null;
  }
}
