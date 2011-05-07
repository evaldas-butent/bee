package com.butent.bee.client.visualization;

import com.butent.bee.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.shared.Assert;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Is an abstract class determining how to handle different types of data in columns of
 * visualization tables.
 */

public abstract class DataColumn<T> extends AbstractList<T> {

  /**
   * Determines how to handle columns with primitive data types.
   */

  private abstract static class PrimitiveDataColumn<T> extends DataColumn<T> {
    PrimitiveDataColumn(AbstractDataTable table, int columnIndex, ColumnType columnType) {
      super(table, columnIndex, columnType);
    }

    @Override
    public final T get(int rowIndex) {
      if (data.isValueNull(rowIndex, columnIndex)) {
        return null;
      } else {
        return getNonNullValue(rowIndex);
      }
    }

    abstract T getNonNullValue(int row);
  }

  public static DataColumn<Boolean> booleans(AbstractDataTable table, int columnIndex) {
    return new PrimitiveDataColumn<Boolean>(table, columnIndex, ColumnType.BOOLEAN) {
      @Override
      Boolean getNonNullValue(int row) {
        return data.getValueBoolean(row, columnIndex);
      }

      @Override
      void setNonNullValue(int row, Boolean value) {
        ((DataTable) data).setValue(row, columnIndex, value);
      }
    };
  }

  public static DataColumn<Boolean> booleans(AbstractDataTable table, String columnId) {
    return booleans(table, getColumnIndex(table, columnId));
  }

  public static DataColumn<Date> dates(AbstractDataTable table, int columnIndex) {
    return datetimes(table, columnIndex);
  }

  public static DataColumn<Date> dates(AbstractDataTable table, String columnId) {
    return dates(table, getColumnIndex(table, columnId));
  }

  public static DataColumn<Date> datetimes(AbstractDataTable table, int columnIndex) {
    return new DataColumn<Date>(table, columnIndex, ColumnType.DATE, ColumnType.DATETIME) {
      @Override
      public Date get(int row) {
        return data.getValueDate(row, columnIndex);
      }

      @Override
      void setNonNullValue(int row, Date value) {
        ((DataTable) data).setValue(row, columnIndex, value);
      }
    };
  }

  public static DataColumn<Date> datetimes(AbstractDataTable table, String columnId) {
    return datetimes(table, getColumnIndex(table, columnId));
  }

  public static DataColumn<Double> doubles(AbstractDataTable table, int columnIndex) {
    return new PrimitiveDataColumn<Double>(table, columnIndex, ColumnType.NUMBER) {
      @Override
      Double getNonNullValue(int row) {
        return data.getValueDouble(row, columnIndex);
      }

      @Override
      void setNonNullValue(int row, Double value) {
        ((DataTable) data).setValue(row, columnIndex, value.doubleValue());
      }
    };
  }

  public static DataColumn<Double> doubles(AbstractDataTable table, String columnId) {
    return doubles(table, getColumnIndex(table, columnId));
  }

  public static List<DataColumn<?>> getAllColumns(AbstractDataTable dataTable) {
    List<DataColumn<?>> columns = new ArrayList<DataColumn<?>>();
    for (int i = 0; i < dataTable.getNumberOfColumns(); i++) {
      columns.add(DataColumn.of(dataTable, i));
    }
    return columns;
  }

  public static DataColumn<Integer> integers(AbstractDataTable table, int columnIndex) {
    return new PrimitiveDataColumn<Integer>(table, columnIndex, ColumnType.NUMBER) {
      @Override
      Integer getNonNullValue(int row) {
        return data.getValueInt(row, columnIndex);
      }

      @Override
      void setNonNullValue(int row, Integer value) {
        ((DataTable) data).setValue(row, columnIndex, value);
      }
    };
  }

  public static DataColumn<Integer> integers(AbstractDataTable table, String columnId) {
    return integers(table, getColumnIndex(table, columnId));
  }

  public static DataColumn<?> of(AbstractDataTable table, int columnIndex) {
    ColumnType columnType = table.getColumnType(columnIndex);
    switch (columnType) {
      case BOOLEAN:
        return booleans(table, columnIndex);
      case NUMBER:
        return numbers(table, columnIndex);
      case STRING:
        return strings(table, columnIndex);
      case DATE:
        return dates(table, columnIndex);
      case DATETIME:
        return datetimes(table, columnIndex);
      case TIMEOFDAY:
        return timeOfDays(table, columnIndex);
      default:
        throw new AssertionError(columnType.name());
    }
  }

  public static DataColumn<?> of(AbstractDataTable table, String columnId) {
    return of(table, getColumnIndex(table, columnId));
  }

  public static DataColumn<String> strings(AbstractDataTable table, int columnIndex) {
    return new DataColumn<String>(table, columnIndex, ColumnType.STRING) {
      @Override
      public String get(int row) {
        return data.getValueString(row, columnIndex);
      }

      @Override
      void setNonNullValue(int row, String value) {
        ((DataTable) data).setValue(row, columnIndex, value);
      }
    };
  }

  public static DataColumn<String> strings(AbstractDataTable table, String columnId) {
    return strings(table, getColumnIndex(table, columnId));
  }

  public static DataColumn<TimeOfDay> timeOfDays(AbstractDataTable table, int columnIndex) {
    return new DataColumn<TimeOfDay>(table, columnIndex, ColumnType.TIMEOFDAY) {
      @Override
      public TimeOfDay get(int row) {
        return data.getValueTimeOfDay(row, columnIndex);
      }

      @Override
      void setNonNullValue(int row, TimeOfDay value) {
        ((DataTable) data).setValue(row, columnIndex, value);
      }
    };
  }

  public static DataColumn<TimeOfDay> timeOfDays(AbstractDataTable table, String columnId) {
    return timeOfDays(table, getColumnIndex(table, columnId));
  }

  static DataColumn<Number> numbers(AbstractDataTable table, int columnIndex) {
    return new PrimitiveDataColumn<Number>(table, columnIndex, ColumnType.NUMBER) {
      @Override
      Number getNonNullValue(int row) {
        return data.getValueDouble(row, columnIndex);
      }

      @Override
      void setNonNullValue(int row, Number value) {
        ((DataTable) data).setValue(row, columnIndex, value.doubleValue());
      }
    };
  }

  private static int getColumnIndex(AbstractDataTable table, String columnId) {
    int index = table.getColumnIndex(columnId);
    if (index < 0) {
      throw new IllegalArgumentException(columnId);
    }
    return index;
  }

  final AbstractDataTable data;

  final int columnIndex;

  final ColumnType columnType;

  DataColumn(AbstractDataTable table, int columnIndex, ColumnType... compatibleColumnTypes) {
    Assert.notNull(table);
    Assert.betweenExclusive(columnIndex, 0, table.getNumberOfColumns());
    Assert.notNull(compatibleColumnTypes);

    this.columnType = table.getColumnType(columnIndex);
    Assert.isTrue(Arrays.asList(compatibleColumnTypes).contains(columnType));
    this.data = table;
    this.columnIndex = columnIndex;
  }

  @Override
  public abstract T get(int row);

  public final String getColumnId() {
    return data.getColumnId(columnIndex);
  }

  public final int getColumnIndex() {
    return columnIndex;
  }

  public final String getColumnLabel() {
    return data.getColumnLabel(columnIndex);
  }

  public final ColumnType getColumnType() {
    return data.getColumnType(columnIndex);
  }

  public final AbstractDataTable getDataTable() {
    return data;
  }

  @Override
  public final T set(int row, T value) {
    T oldValue = get(row);
    setValue(row, value);
    return oldValue;
  }

  public final void setValue(int row, T value) {
    DataTable dataTable = (DataTable) data;
    if (value == null) {
      dataTable.setCellNull(row, columnIndex, null, null);
    } else {
      setNonNullValue(row, value);
    }
  }

  @Override
  public final int size() {
    return data.getNumberOfRows();
  }

  abstract void setNonNullValue(int row, T value);
}
