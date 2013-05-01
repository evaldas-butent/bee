package com.butent.bee.client.data;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.ColumnMapper;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.math.BigDecimal;
import java.util.List;

public class Data {

  private static final DataInfoProvider DATA_INFO_PROVIDER = new DataInfoProvider();

  private static final ColumnMapper COLUMN_MAPPER = new ColumnMapper(DATA_INFO_PROVIDER);
  
  private static BeeLogger logger = LogUtils.getLogger(Data.class);
  
  public static void clearCell(String viewName, IsRow row, String colName) {
    COLUMN_MAPPER.clearCell(viewName, row, colName);
  }
  
  public static BeeRowSet createRowSet(String viewName) {
    return new BeeRowSet(viewName, getColumns(viewName));
  }

  public static boolean equals(String viewName, IsRow row, String colName, Long value) {
    return Objects.equal(getLong(viewName, row, colName), value);
  }

  public static int getApproximateRowCount(String viewName) {
    DataInfo dataInfo = getDataInfo(viewName);
    return (dataInfo == null) ? BeeConst.UNDEF : dataInfo.getRowCount(); 
  }

  public static Boolean getBoolean(String viewName, IsRow row, String colName) {
    return COLUMN_MAPPER.getBoolean(viewName, row, colName);
  }

  public static BeeColumn getColumn(String viewName, String colName) {
    return getDataInfo(viewName).getColumn(colName);
  }

  public static int getColumnIndex(String viewName, String colName) {
    return COLUMN_MAPPER.getIndex(viewName, colName);
  }

  public static String getColumnLabel(String viewName, String colName) {
    return LocaleUtils.getLabel(getColumn(viewName, colName));
  }

  public static List<String> getColumnLabels(String viewName, List<String> colNames) {
    List<String> result = Lists.newArrayList();
    
    for (BeeColumn column : getColumns(viewName, colNames)) {
      result.add(LocaleUtils.getLabel(column));
    }
    
    return result;
  }
  
  public static ColumnMapper getColumnMapper() {
    return COLUMN_MAPPER;
  }

  public static Integer getColumnPrecision(String viewName, String colName) {
    return getDataInfo(viewName).getColumnPrecision(colName);
  }

  public static List<BeeColumn> getColumns(String viewName) {
    return getDataInfo(viewName).getColumns();
  }
  
  public static List<BeeColumn> getColumns(String viewName, List<String> colNames) {
    List<BeeColumn> result = Lists.newArrayList();
    DataInfo dataInfo = getDataInfo(viewName);
    
    for (String colName : colNames) {
      BeeColumn column = dataInfo.getColumn(colName);
      if (column == null) {
        logger.severe(viewName, "column", colName, "not found");
      } else {
        result.add(column);
      }
    }
    return result;
  }
  
  public static ValueType getColumnType(String viewName, String colName) {
    return getDataInfo(viewName).getColumnType(colName);
  }

  public static DataInfo getDataInfo(String viewName) {
    return getDataInfo(viewName, true);
  }
  
  public static DataInfo getDataInfo(String viewName, boolean warn) {
    return DATA_INFO_PROVIDER.getDataInfo(viewName, warn);
  }
  
  public static DataInfoProvider getDataInfoProvider() {
    return DATA_INFO_PROVIDER;
  }

  public static JustDate getDate(String viewName, IsRow row, String colName) {
    return COLUMN_MAPPER.getDate(viewName, row, colName);
  }

  public static DateTime getDateTime(String viewName, IsRow row, String colName) {
    return COLUMN_MAPPER.getDateTime(viewName, row, colName);
  }
  
  public static BigDecimal getDecimal(String viewName, IsRow row, String colName) {
    return COLUMN_MAPPER.getDecimal(viewName, row, colName);
  }

  public static Double getDouble(String viewName, IsRow row, String colName) {
    return COLUMN_MAPPER.getDouble(viewName, row, colName);
  }
  
  public static String getIdColumn(String viewName) {
    return getDataInfo(viewName).getIdColumn();
  }

  public static Integer getInteger(String viewName, IsRow row, String colName) {
    return COLUMN_MAPPER.getInteger(viewName, row, colName);
  }
  
  public static Long getLong(String viewName, IsRow row, String colName) {
    return COLUMN_MAPPER.getLong(viewName, row, colName);
  }

  public static String getString(String viewName, IsRow row, String colName) {
    return COLUMN_MAPPER.getString(viewName, row, colName);
  }

  public static String getViewCaption(String viewName) {
    DataInfo dataInfo = getDataInfo(viewName);
    return BeeUtils.notEmpty(LocaleUtils.maybeLocalize(dataInfo.getCaption()), viewName);
  }
  
  public static void init(Callback<Integer> callback) {
    DATA_INFO_PROVIDER.load(callback);

    BeeKeeper.getBus().registerRowDeleteHandler(DATA_INFO_PROVIDER, false);
    BeeKeeper.getBus().registerMultiDeleteHandler(DATA_INFO_PROVIDER, false);
    BeeKeeper.getBus().registerRowInsertHandler(DATA_INFO_PROVIDER, false);
  }
  
  public static boolean isNull(String viewName, IsRow row, String colName) {
    return COLUMN_MAPPER.isNull(viewName, row, colName);
  }
  
  public static void setValue(String viewName, IsRow row, String colName, BigDecimal value) {
    COLUMN_MAPPER.setValue(viewName, row, colName, value);
  }
  
  public static void setValue(String viewName, IsRow row, String colName, Boolean value) {
    COLUMN_MAPPER.setValue(viewName, row, colName, value);
  }

  public static void setValue(String viewName, IsRow row, String colName, DateTime value) {
    COLUMN_MAPPER.setValue(viewName, row, colName, value);
  }
  
  public static void setValue(String viewName, IsRow row, String colName, Double value) {
    COLUMN_MAPPER.setValue(viewName, row, colName, value);
  }
  
  public static void setValue(String viewName, IsRow row, String colName, Integer value) {
    COLUMN_MAPPER.setValue(viewName, row, colName, value);
  }

  public static void setValue(String viewName, IsRow row, String colName, JustDate value) {
    COLUMN_MAPPER.setValue(viewName, row, colName, value);
  }

  public static void setValue(String viewName, IsRow row, String colName, Long value) {
    COLUMN_MAPPER.setValue(viewName, row, colName, value);
  }

  public static void setValue(String viewName, IsRow row, String colName, String value) {
    COLUMN_MAPPER.setValue(viewName, row, colName, value);
  }
  
  private Data() {
    super();
  }
}
