package com.butent.bee.shared.modules.finance;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Dimensions {

  public static final int SPACETIME = 10;

  public static final String TBL_NAMES = "DimensionNames";
  public static final String VIEW_NAMES = "DimensionNames";

  public static final String COL_ORDINAL = "Ordinal";
  public static final String COL_PLURAL_NAME = "PluralName";
  public static final String COL_SINGULAR_NAME = "SingularName";

  public static final String GRID_NAMES = "DimensionNames";

  public static final String PRM_DIMENSIONS = "Dimensions";

  private static final String COL_DEPARTMENT = "Department";
  private static final String COL_ACTIVITY_TYPE = "ActivityType";
  private static final String COL_COST_CENTER = "CostCenter";
  private static final String COL_OBJECT = "Object";

  private static final BeeLogger logger = LogUtils.getLogger(Dimensions.class);

  private static int observed;

  private static final Map<Integer, String> pluralNames = new HashMap<>();
  private static final Map<Integer, String> singularNames = new HashMap<>();

  public static String plural(int ordinal) {
    if (isValid(ordinal)) {
      String name = pluralNames.get(ordinal);
      return BeeUtils.isEmpty(name) ? Localized.dictionary().dimensionNameDefault(ordinal) : name;

    } else {
      return null;
    }
  }

  public static String singular(int ordinal) {
    if (isValid(ordinal)) {
      String name = singularNames.get(ordinal);
      return BeeUtils.isEmpty(name) ? Localized.dictionary().dimensionNameDefault(ordinal) : name;

    } else {
      return null;
    }
  }

  public static String getViewName(int ordinal) {
    if (isValid(ordinal)) {
      return "Dimensions" + BeeUtils.toLeadingZeroes(ordinal, 2);
    } else {
      return null;
    }
  }

  public static String menuParameter(int ordinal) {
    return isValid(ordinal) ? BeeUtils.toString(ordinal) : null;
  }

  public static void load(String serialized) {
    BeeRowSet rowSet = BeeRowSet.restore(serialized);

    setObserved(BeeUtils.toIntOrNull(rowSet.getTableProperty(PRM_DIMENSIONS)));

    if (!rowSet.isEmpty()) {
      String language = Localized.dictionary().languageTag();
      int ordinalIndex = rowSet.getColumnIndex(COL_ORDINAL);

      for (BeeRow row : rowSet) {
        Integer ordinal = row.getInteger(ordinalIndex);

        setPlural(ordinal, DataUtils.getTranslation(rowSet, row, COL_PLURAL_NAME, language));
        setSingular(ordinal, DataUtils.getTranslation(rowSet, row, COL_SINGULAR_NAME, language));
      }
    }

    logger.info("dimensions", observed);
  }

  public static int getObserved() {
    return observed;
  }

  public static void setObserved(Integer count) {
    if (count != null) {
      Dimensions.observed = BeeUtils.clamp(count, 0, SPACETIME);
    }
  }

  public static void setPlural(Integer ordinal, String name) {
    if (isValid(ordinal) && !BeeUtils.isEmpty(name)) {
      pluralNames.put(ordinal, name.trim());
    }
  }

  public static void setSingular(Integer ordinal, String name) {
    if (isValid(ordinal) && !BeeUtils.isEmpty(name)) {
      singularNames.put(ordinal, name.trim());
    }
  }

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

  private static boolean isValid(Integer ordinal) {
    return ordinal != null && ordinal >= 1 && ordinal <= SPACETIME;
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

  public Filter getFilter() {
    CompoundFilter filter = Filter.and();

    filter.add(Filter.equalsOrIsNull(COL_DEPARTMENT, getDepartment()));
    filter.add(Filter.equalsOrIsNull(COL_ACTIVITY_TYPE, getActivityType()));
    filter.add(Filter.equalsOrIsNull(COL_COST_CENTER, getCostCenter()));
    filter.add(Filter.equalsOrIsNull(COL_OBJECT, getObject()));

    return filter;
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return BeeConst.EMPTY;
    } else {
      return BeeUtils.joinOptions(COL_DEPARTMENT, department, COL_ACTIVITY_TYPE, activityType,
          COL_COST_CENTER, costCenter, COL_OBJECT, object);
    }
  }
}
