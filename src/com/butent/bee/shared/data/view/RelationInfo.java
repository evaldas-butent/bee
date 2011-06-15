package com.butent.bee.shared.data.view;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class RelationInfo {
  
  public static RelationInfo create(List<BeeColumn> columns, ColumnDescription descr) {
    if (columns == null || descr == null) {
      return null;
    }
    String relSrc = descr.getRelSource();
    if (BeeUtils.isEmpty(relSrc) || BeeUtils.isEmpty(descr.getRelTable())
        || BeeUtils.isEmpty(descr.getRelField())) {
      return null;
    }
    
    int relIdx = BeeConst.UNDEF;
    for (int i = 0; i < columns.size(); i++) {
      if (BeeUtils.same(columns.get(i).getLabel(), relSrc)) {
        relIdx = i;
        break;
      }
    }
    if (BeeConst.isUndef(relIdx)) {
      return null;
    }
    
    return new RelationInfo(descr.getSource(), relSrc, descr.getRelTable(), descr.getRelField(),
        columns.get(relIdx), relIdx);
  }

  private final String source;
  private final String relSource;

  private final String relTable;
  private final String relField;
  
  private final BeeColumn dataColumn;
  private final int dataIndex;

  private RelationInfo(String source, String relSource, String relTable, String relField,
      BeeColumn dataColumn, int dataIndex) {
    super();
    this.source = source;
    this.relSource = relSource;
    this.relTable = relTable;
    this.relField = relField;
    this.dataColumn = dataColumn;
    this.dataIndex = dataIndex;
  }

  public BeeColumn getDataColumn() {
    return dataColumn;
  }

  public int getDataIndex() {
    return dataIndex;
  }

  public String getRelField() {
    return relField;
  }

  public String getRelSource() {
    return relSource;
  }

  public String getRelTable() {
    return relTable;
  }

  public String getSource() {
    return source;
  }

  public boolean isNullable() {
    return (getDataColumn() == null) ? true : getDataColumn().isNullable();
  }
}
