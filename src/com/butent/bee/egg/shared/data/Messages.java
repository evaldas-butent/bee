package com.butent.bee.egg.shared.data;

import com.google.common.collect.Maps;

import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Map;

public enum Messages {
  NO_COLUMN,
  AVG_SUM_ONLY_NUMERIC,
  INVALID_AGG_TYPE,
  PARSE_ERROR,
  CANNOT_BE_IN_GROUP_BY,
  CANNOT_BE_IN_PIVOT,
  CANNOT_BE_IN_WHERE,
  SELECT_WITH_AND_WITHOUT_AGG,
  COL_AGG_NOT_IN_SELECT,
  CANNOT_GROUP_WITHOUT_AGG,
  CANNOT_PIVOT_WITHOUT_AGG,
  AGG_IN_SELECT_NO_PIVOT,
  FORMAT_COL_NOT_IN_SELECT,
  LABEL_COL_NOT_IN_SELECT,
  ADD_COL_TO_GROUP_BY_OR_AGG,
  AGG_IN_ORDER_NOT_IN_SELECT,
  NO_AGG_IN_ORDER_WHEN_PIVOT,
  COL_IN_ORDER_MUST_BE_IN_SELECT,
  NO_COL_IN_GROUP_AND_PIVOT,
  INVALID_OFFSET,
  INVALID_SKIPPING,
  COLUMN_ONLY_ONCE;

  private static final Map<Messages, String> QUERY_ERROR_TO_MESSAGE = Maps.newEnumMap(Messages.class);

  static {
    QUERY_ERROR_TO_MESSAGE.put(Messages.NO_COLUMN, "NO_COLUMN");
    QUERY_ERROR_TO_MESSAGE.put(Messages.AVG_SUM_ONLY_NUMERIC, "AVG_SUM_ONLY_NUMERIC");
    QUERY_ERROR_TO_MESSAGE.put(Messages.INVALID_AGG_TYPE, "INVALID_AGG_TYPE");
    QUERY_ERROR_TO_MESSAGE.put(Messages.PARSE_ERROR, "PARSE_ERROR");
    QUERY_ERROR_TO_MESSAGE.put(Messages.CANNOT_BE_IN_GROUP_BY, "CANNOT_BE_IN_GROUP_BY");
    QUERY_ERROR_TO_MESSAGE.put(Messages.CANNOT_BE_IN_PIVOT, "CANNOT_BE_IN_PIVOT");
    QUERY_ERROR_TO_MESSAGE.put(Messages.CANNOT_BE_IN_WHERE, "CANNOT_BE_IN_WHERE");
    QUERY_ERROR_TO_MESSAGE.put(Messages.SELECT_WITH_AND_WITHOUT_AGG,
        "SELECT_WITH_AND_WITHOUT_AGG");
    QUERY_ERROR_TO_MESSAGE.put(Messages.COL_AGG_NOT_IN_SELECT, "COL_AGG_NOT_IN_SELECT");
    QUERY_ERROR_TO_MESSAGE.put(Messages.CANNOT_GROUP_WITHOUT_AGG, "CANNOT_GROUP_WITHOUT_AGG");
    QUERY_ERROR_TO_MESSAGE.put(Messages.CANNOT_PIVOT_WITHOUT_AGG, "CANNOT_PIVOT_WITHOUT_AGG");
    QUERY_ERROR_TO_MESSAGE.put(Messages.AGG_IN_SELECT_NO_PIVOT, "AGG_IN_SELECT_NO_PIVOT");
    QUERY_ERROR_TO_MESSAGE.put(Messages.FORMAT_COL_NOT_IN_SELECT,  "FORMAT_COL_NOT_IN_SELECT");
    QUERY_ERROR_TO_MESSAGE.put(Messages.LABEL_COL_NOT_IN_SELECT, "LABEL_COL_NOT_IN_SELECT");
    QUERY_ERROR_TO_MESSAGE.put(Messages.ADD_COL_TO_GROUP_BY_OR_AGG,
        "ADD_COL_TO_GROUP_BY_OR_AGG");
    QUERY_ERROR_TO_MESSAGE.put(Messages.AGG_IN_ORDER_NOT_IN_SELECT,
        "AGG_IN_ORDER_NOT_IN_SELECT");
    QUERY_ERROR_TO_MESSAGE.put(Messages.NO_AGG_IN_ORDER_WHEN_PIVOT,
        "NO_AGG_IN_ORDER_WHEN_PIVOT");
    QUERY_ERROR_TO_MESSAGE.put(Messages.COL_IN_ORDER_MUST_BE_IN_SELECT,
        "COL_IN_ORDER_MUST_BE_IN_SELECT");
    QUERY_ERROR_TO_MESSAGE.put(Messages.NO_COL_IN_GROUP_AND_PIVOT, "NO_COL_IN_GROUP_AND_PIVOT");
    QUERY_ERROR_TO_MESSAGE.put(Messages.INVALID_OFFSET, "INVALID_OFFSET");
    QUERY_ERROR_TO_MESSAGE.put(Messages.INVALID_SKIPPING, "INVALID_SKIPPING");
    QUERY_ERROR_TO_MESSAGE.put(Messages.COLUMN_ONLY_ONCE, "COLUMN_ONLY_ONCE");
  }

  public String getMessage() {
    return QUERY_ERROR_TO_MESSAGE.get(this);
  }

  public String getMessage(Object... args) {
    return BeeUtils.concat(1, QUERY_ERROR_TO_MESSAGE.get(this), args);
  }
}
