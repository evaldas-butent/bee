package com.butent.bee.shared.data.view;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Enables using relations between two data sources.
 */

public class RelationInfo {

  public static RelationInfo create(List<BeeColumn> columns, String source, String relSource,
      String relView, String relColumn) {
    if (columns == null) {
      return null;
    }
    if (BeeUtils.isEmpty(relSource) || BeeUtils.same(relSource, source)
        || BeeUtils.isEmpty(relView) || BeeUtils.isEmpty(relColumn)) {
      return null;
    }

    int relIdx = BeeConst.UNDEF;
    for (int i = 0; i < columns.size(); i++) {
      if (BeeUtils.same(columns.get(i).getId(), relSource)) {
        relIdx = i;
        break;
      }
    }
    if (BeeConst.isUndef(relIdx)) {
      return null;
    }

    return new RelationInfo(source, relSource, relView, relColumn, columns.get(relIdx), relIdx);
  }
  
  public static RelationInfo create(List<BeeColumn> columns, ColumnDescription descr) {
    if (descr == null) {
      return null;
    }
    return create(columns, descr.getSource(), descr.getRelSource(),
        descr.getRelView(), descr.getRelColumn());
  }

  private final String source;
  private final String relSource;

  private final String relView;
  private final String relColumn;

  private final BeeColumn dataColumn;
  private final int dataIndex;

  private RelationInfo(String source, String relSource, String relView, String relColumn,
      BeeColumn dataColumn, int dataIndex) {
    super();
    this.source = source;
    this.relSource = relSource;
    this.relView = relView;
    this.relColumn = relColumn;
    this.dataColumn = dataColumn;
    this.dataIndex = dataIndex;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RelationInfo)) {
      return false;
    }
    return BeeUtils.same(getSource(), ((RelationInfo) obj).getSource());
  }

  public BeeColumn getDataColumn() {
    return dataColumn;
  }

  public int getDataIndex() {
    return dataIndex;
  }

  public String getRelColumn() {
    return relColumn;
  }

  public String getRelSource() {
    return relSource;
  }

  public String getRelView() {
    return relView;
  }

  public String getSource() {
    return source;
  }

  @Override
  public int hashCode() {
    return BeeUtils.normalize(getSource()).hashCode();
  }

  public boolean isNullable() {
    return (getDataColumn() == null) ? true : getDataColumn().isNullable();
  }
}
