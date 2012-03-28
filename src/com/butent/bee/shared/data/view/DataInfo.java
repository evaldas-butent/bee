package com.butent.bee.shared.data.view;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

/**
 * Enables to get main information about data objects, like row count, ID column etc.
 */

public class DataInfo implements BeeSerializable, Comparable<DataInfo>, HasExtendedInfo {

  public static DataInfo restore(String s) {
    Assert.notEmpty(s);
    DataInfo ti = new DataInfo();
    ti.deserialize(s);
    return ti;
  }

  private String name;
  
  private String idColumn;
  private String versionColumn;
  
  private List<BeeColumn> columns = Lists.newArrayList();

  private int rowCount;

  public DataInfo(String name, String idColumn, String versionColumn, List<BeeColumn> columns,
      int rowCount) {
    this.name = name;
    this.idColumn = idColumn;
    this.versionColumn = versionColumn;
    
    if (columns != null) {
      for (BeeColumn column : columns) {
        this.columns.add(column);
      }
    }

    this.rowCount = rowCount;
  }

  private DataInfo() {
  }

  public int compareTo(DataInfo o) {
    if (o == null) {
      return BeeConst.COMPARE_MORE;
    }
    return BeeUtils.compareNormalized(getName(), o.getName());
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 5);

    setName(arr[0]);
    setIdColumn(arr[1]);
    setVersionColumn(arr[2]);
    
    getColumns().clear();
    String[] cArr = Codec.beeDeserializeCollection(arr[3]);
    if (!BeeUtils.isEmpty(cArr)) {
      for (String col : cArr) {
        getColumns().add(BeeColumn.restore(col));
      }
    }
    
    setRowCount(BeeUtils.toInt(arr[4]));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DataInfo)) {
      return false;
    }
    return BeeUtils.same(getName(), ((DataInfo) obj).getName());
  }
  
  public int getColumnCount() {
    return (getColumns() == null) ? BeeConst.UNDEF : getColumns().size();
  }

  public List<BeeColumn> getColumns() {
    return columns;
  }

  public List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> result = Lists.newArrayList();
    PropertyUtils.addProperties(result, false,
        "Name", getName(),
        "Id Column", getIdColumn(),
        "Version Column", getVersionColumn(),
        "Row Count", getRowCount(),
        "Column Count", getColumnCount());
    
    int cc = getColumnCount();
    for (int i = 0; i < cc; i++) {
      BeeColumn column = getColumns().get(i);
      PropertyUtils.appendChildrenToExtended(result, BeeUtils.progress(i + 1, cc, column.getId()),
          column.getInfo());
    }
    return result;
  }

  public String getIdColumn() {
    return idColumn;
  }

  public String getName() {
    return name;
  }

  public int getRowCount() {
    return rowCount;
  }

  public String getVersionColumn() {
    return versionColumn;
  }

  @Override
  public int hashCode() {
    return BeeUtils.normalize(getName()).hashCode();
  }

  public String serialize() {
    return Codec.beeSerialize(
        new Object[] {getName(), getIdColumn(), getVersionColumn(), getColumns(), getRowCount()});
  }

  public void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }

  private void setIdColumn(String idColumn) {
    this.idColumn = idColumn;
  }

  private void setName(String name) {
    this.name = name;
  }

  private void setVersionColumn(String versionColumn) {
    this.versionColumn = versionColumn;
  }
}
