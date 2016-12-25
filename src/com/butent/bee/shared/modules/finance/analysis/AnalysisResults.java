package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnalysisResults implements BeeSerializable {

  public static AnalysisResults restore(String s) {
    AnalysisResults ar = new AnalysisResults();
    ar.deserialize(s);
    return ar;
  }

  private enum Serial {
    HEADER_INDEXES, HEADER, COLUMN_INDEXES, COLUMNS, ROW_INDEXES, ROWS,
    COLUMN_SPLIT_TYPES, ROW_SPLIT_TYPES, COLUMN_SPLIT_VALUES, ROW_SPLIT_VALUES, VALUES,
    INIT_START, VALIDATE_START, COMPUTE_START, COMPUTE_END
  }

  private static final List<AnalysisSplitType> EMPTY_SPLIT_TYPES = Collections.emptyList();
  private static final List<AnalysisSplitValue> EMPTY_SPLIT_VALUES = Collections.emptyList();

  private long initStart;
  private long validateStart;
  private long computeStart;
  private long computeEnd;

  private final Map<String, Integer> headerIndexes = new HashMap<>();
  private BeeRow header;

  private final Map<String, Integer> columnIndexes = new HashMap<>();
  private final List<BeeRow> columns = new ArrayList<>();

  private final Map<String, Integer> rowIndexes = new HashMap<>();
  private final List<BeeRow> rows = new ArrayList<>();

  private final Map<Long, List<AnalysisSplitType>> columnSplitTypes = new HashMap<>();
  private final Map<Long, List<AnalysisSplitType>> rowSplitTypes = new HashMap<>();

  private final Map<Long, Map<AnalysisSplitType, List<AnalysisSplitValue>>> columnSplitValues =
      new HashMap<>();
  private final Map<Long, Map<AnalysisSplitType, List<AnalysisSplitValue>>> rowSplitValues =
      new HashMap<>();

  private final List<AnalysisValue> values = new ArrayList<>();

  private AnalysisResults() {
  }

  public AnalysisResults(Map<String, Integer> headerIndexes, BeeRow header,
      Map<String, Integer> columnIndexes, List<BeeRow> columns,
      Map<String, Integer> rowIndexes, List<BeeRow> rows) {

    this.headerIndexes.putAll(headerIndexes);
    this.header = header;

    this.columnIndexes.putAll(columnIndexes);
    this.columns.addAll(columns);

    this.rowIndexes.putAll(rowIndexes);
    this.rows.addAll(rows);
  }

  public void addValue(AnalysisValue value) {
    if (value != null) {
      values.add(value);
    }
  }

  public void addValues(Collection<AnalysisValue> collection) {
    if (collection != null) {
      collection.forEach(this::addValue);
    }
  }

  public void mergeValue(AnalysisValue value) {
    if (value != null) {
      for (AnalysisValue av : values) {
        if (av.matches(value)) {
          av.add(value);
          return;
        }
      }

      addValue(value);
    }
  }

  public void mergeValues(Collection<AnalysisValue> collection) {
    if (collection != null) {
      collection.forEach(this::mergeValue);
    }
  }

  public void addColumnSplitTypes(long columnId, List<AnalysisSplitType> splitTypes) {
    if (!BeeUtils.isEmpty(splitTypes)) {
      columnSplitTypes.put(columnId, splitTypes);
    }
  }

  public void addColumnSplitValues(long columnId, AnalysisSplitType splitType,
      List<AnalysisSplitValue> splitValues) {

    if (!BeeUtils.isEmpty(splitValues)) {
      Map<AnalysisSplitType, List<AnalysisSplitValue>> map = new HashMap<>();
      map.put(splitType, splitValues);

      columnSplitValues.put(columnId, map);
    }
  }

  public void addRowSplitTypes(long rowId, List<AnalysisSplitType> splitTypes) {
    if (!BeeUtils.isEmpty(splitTypes)) {
      rowSplitTypes.put(rowId, splitTypes);
    }
  }

  public void addRowSplitValues(long rowId, AnalysisSplitType splitType,
      List<AnalysisSplitValue> splitValues) {

    if (!BeeUtils.isEmpty(splitValues)) {
      Map<AnalysisSplitType, List<AnalysisSplitValue>> map = new HashMap<>();
      map.put(splitType, splitValues);

      rowSplitValues.put(rowId, map);
    }
  }

  public List<AnalysisSplitType> getColumnSplitTypes(long columnId) {
    return columnSplitTypes.getOrDefault(columnId, EMPTY_SPLIT_TYPES);
  }

  public List<AnalysisSplitValue> getColumnSplitValues(long columnId, AnalysisSplitType type) {
    Map<AnalysisSplitType, List<AnalysisSplitValue>> map = columnSplitValues.get(columnId);
    List<AnalysisSplitValue> list = (map == null) ? null : map.get(type);

    return (list == null) ? EMPTY_SPLIT_VALUES : list;
  }

  public List<AnalysisSplitType> getRowSplitTypes(long rowId) {
    return rowSplitTypes.getOrDefault(rowId, EMPTY_SPLIT_TYPES);
  }

  public List<AnalysisSplitValue> getRowSplitValues(long rowId, AnalysisSplitType type) {
    Map<AnalysisSplitType, List<AnalysisSplitValue>> map = rowSplitValues.get(rowId);
    List<AnalysisSplitValue> list = (map == null) ? null : map.get(type);

    return (list == null) ? EMPTY_SPLIT_VALUES : list;
  }

  public List<AnalysisValue> getValues() {
    return values;
  }

  public boolean isEmpty() {
    return values.isEmpty();
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, Serial.values().length);

    String[] items;

    for (int i = 0; i < arr.length; i++) {
      String v = arr[i];

      if (!BeeUtils.isEmpty(v)) {
        switch (Serial.values()[i]) {
          case HEADER_INDEXES:
            if (!headerIndexes.isEmpty()) {
              headerIndexes.clear();
            }

            headerIndexes.putAll(deserializeIndexes(v));
            break;

          case HEADER:
            setHeader(BeeRow.restore(v));
            break;

          case COLUMN_INDEXES:
            if (!columnIndexes.isEmpty()) {
              columnIndexes.clear();
            }

            columnIndexes.putAll(deserializeIndexes(v));
            break;

          case COLUMNS:
            if (!columns.isEmpty()) {
              columns.clear();
            }

            items = Codec.beeDeserializeCollection(v);
            if (items != null) {
              for (String item : items) {
                columns.add(BeeRow.restore(item));
              }
            }
            break;

          case ROW_INDEXES:
            if (!rowIndexes.isEmpty()) {
              rowIndexes.clear();
            }

            rowIndexes.putAll(deserializeIndexes(v));
            break;

          case ROWS:
            if (!rows.isEmpty()) {
              rows.clear();
            }

            items = Codec.beeDeserializeCollection(v);
            if (items != null) {
              for (String item : items) {
                rows.add(BeeRow.restore(item));
              }
            }
            break;

          case COLUMN_SPLIT_TYPES:
            if (!columnSplitTypes.isEmpty()) {
              columnSplitTypes.clear();
            }

            columnSplitTypes.putAll(dST(v));
            break;

          case ROW_SPLIT_TYPES:
            if (!rowSplitTypes.isEmpty()) {
              rowSplitTypes.clear();
            }

            rowSplitTypes.putAll(dST(v));
            break;

          case COLUMN_SPLIT_VALUES:
            if (!columnSplitValues.isEmpty()) {
              columnSplitValues.clear();
            }

            columnSplitValues.putAll(dSV(v));
            break;

          case ROW_SPLIT_VALUES:
            if (!rowSplitValues.isEmpty()) {
              rowSplitValues.clear();
            }

            rowSplitValues.putAll(dSV(v));
            break;

          case VALUES:
            if (!values.isEmpty()) {
              values.clear();
            }

            items = Codec.beeDeserializeCollection(v);
            if (items != null) {
              for (String item : items) {
                addValue(AnalysisValue.restore(item));
              }
            }
            break;

          case INIT_START:
            setInitStart(BeeUtils.toLong(v));
            break;
          case VALIDATE_START:
            setValidateStart(BeeUtils.toLong(v));
            break;
          case COMPUTE_START:
            setComputeStart(BeeUtils.toLong(v));
            break;
          case COMPUTE_END:
            setComputeEnd(BeeUtils.toLong(v));
            break;
        }
      }
    }
  }

  private static Map<String, Integer> deserializeIndexes(String s) {
    Map<String, Integer> indexes = new HashMap<>();

    Codec.deserializeHashMap(s).forEach((k, v) -> {
      Integer index = BeeUtils.toIntOrNull(v);
      if (index != null) {
        indexes.put(k, index);
      }
    });

    return indexes;
  }

  private static Map<Long, List<AnalysisSplitType>> dST(String s) {
    Map<Long, List<AnalysisSplitType>> result = new HashMap<>();

    Codec.deserializeHashMap(s).forEach((k, v) -> {
      Long id = BeeUtils.toLongOrNull(k);
      List<AnalysisSplitType> types = new ArrayList<>();

      String[] items = Codec.beeDeserializeCollection(v);
      if (items != null) {
        for (String item : items) {
          AnalysisSplitType type = EnumUtils.getEnumByName(AnalysisSplitType.class, item);
          if (type != null) {
            types.add(type);
          }
        }
      }

      if (DataUtils.isId(id) && !types.isEmpty()) {
        result.put(id, types);
      }
    });

    return result;
  }

  private static Map<Long, Map<AnalysisSplitType, List<AnalysisSplitValue>>> dSV(String s) {
    Map<Long, Map<AnalysisSplitType, List<AnalysisSplitValue>>> result = new HashMap<>();

    Codec.deserializeHashMap(s).forEach((k, v) -> {
      Long id = BeeUtils.toLongOrNull(k);

      if (DataUtils.isId(id)) {
        Map<AnalysisSplitType, List<AnalysisSplitValue>> map = new HashMap<>();

        Codec.deserializeHashMap(v).forEach((ast, asvList) -> {
          AnalysisSplitType type = EnumUtils.getEnumByName(AnalysisSplitType.class, ast);
          List<AnalysisSplitValue> splitValues = new ArrayList<>();

          String[] items = Codec.beeDeserializeCollection(asvList);
          if (items != null) {
            for (String item : items) {
              splitValues.add(AnalysisSplitValue.restore(item));
            }
          }

          if (type != null && !splitValues.isEmpty()) {
            map.put(type, splitValues);
          }
        });

        if (!map.isEmpty()) {
          result.put(id, map);
        }
      }
    });

    return result;
  }

  private void setHeader(BeeRow header) {
    this.header = header;
  }

  @Override
  public String serialize() {
    Object[] arr = new Object[Serial.values().length];
    int i = 0;

    for (Serial member : Serial.values()) {
      switch (member) {
        case HEADER_INDEXES:
          arr[i++] = headerIndexes;
          break;
        case HEADER:
          arr[i++] = header;
          break;

        case COLUMN_INDEXES:
          arr[i++] = columnIndexes;
          break;
        case COLUMNS:
          arr[i++] = columns;
          break;

        case ROW_INDEXES:
          arr[i++] = rowIndexes;
          break;
        case ROWS:
          arr[i++] = rows;
          break;

        case COLUMN_SPLIT_TYPES:
          arr[i++] = columnSplitTypes;
          break;
        case ROW_SPLIT_TYPES:
          arr[i++] = rowSplitTypes;
          break;

        case COLUMN_SPLIT_VALUES:
          arr[i++] = columnSplitValues;
          break;
        case ROW_SPLIT_VALUES:
          arr[i++] = rowSplitValues;
          break;

        case VALUES:
          arr[i++] = values;
          break;

        case INIT_START:
          arr[i++] = initStart;
          break;
        case VALIDATE_START:
          arr[i++] = validateStart;
          break;
        case COMPUTE_START:
          arr[i++] = computeStart;
          break;
        case COMPUTE_END:
          arr[i++] = computeEnd;
          break;
      }
    }

    return Codec.beeSerialize(arr);
  }

  public long getInitStart() {
    return initStart;
  }

  public void setInitStart(long initStart) {
    this.initStart = initStart;
  }

  public long getValidateStart() {
    return validateStart;
  }

  public void setValidateStart(long validateStart) {
    this.validateStart = validateStart;
  }

  public long getComputeStart() {
    return computeStart;
  }

  public void setComputeStart(long computeStart) {
    this.computeStart = computeStart;
  }

  public long getComputeEnd() {
    return computeEnd;
  }

  public void setComputeEnd(long computeEnd) {
    this.computeEnd = computeEnd;
  }

  public String getHeaderString(String key) {
    return header.getString(headerIndexes.get(key));
  }

  public String getColumnString(BeeRow column, String key) {
    return column.getString(columnIndexes.get(key));
  }

  public String getRowString(BeeRow row, String key) {
    return row.getString(rowIndexes.get(key));
  }

  public List<BeeRow> getColumns() {
    return columns;
  }

  public List<BeeRow> getRows() {
    return rows;
  }

  public Map<Long, List<AnalysisSplitType>> getColumnSplitTypes() {
    return columnSplitTypes;
  }

  public Map<Long, List<AnalysisSplitType>> getRowSplitTypes() {
    return rowSplitTypes;
  }

  public Map<Long, Map<AnalysisSplitType, List<AnalysisSplitValue>>> getColumnSplitValues() {
    return columnSplitValues;
  }

  public Map<Long, Map<AnalysisSplitType, List<AnalysisSplitValue>>> getRowSplitValues() {
    return rowSplitValues;
  }
}
