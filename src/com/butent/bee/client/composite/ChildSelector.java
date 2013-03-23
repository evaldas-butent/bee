package com.butent.bee.client.composite;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class ChildSelector extends MultiSelector {

  private static final String ATTR_CHILD_TABLE = "childTable";
  private static final String ATTR_TARGET_REL_COLUMN = "targetRelColumn";
  private static final String ATTR_SOURCE_REL_COLUMN = "sourceRelColumn";
  
  public static ChildSelector create(String targetView, Relation relation,
      Map<String, String> attributes) {
    if (relation == null || BeeUtils.isEmpty(attributes)) {
      return null;
    }
    
    String table = attributes.get(ATTR_CHILD_TABLE);
    if (BeeUtils.isEmpty(table)) {
      return null;
    }
    
    DataInfo childInfo = null;

    String targetColumn = attributes.get(ATTR_TARGET_REL_COLUMN);
    if (BeeUtils.isEmpty(targetColumn) && !BeeUtils.isEmpty(targetView)) {
      childInfo = Data.getDataInfo(table);
      if (childInfo == null) {
        return null;
      }
      
      DataInfo targetInfo = Data.getDataInfo(targetView);
      String targetTable = (targetInfo == null) ? targetView : targetInfo.getTableName();
      
      targetColumn = childInfo.getRelationField(targetTable);
      if (BeeUtils.isEmpty(targetColumn)) {
        return null;
      }
    }
    
    String sourceColumn = attributes.get(ATTR_SOURCE_REL_COLUMN);
    if (BeeUtils.isEmpty(sourceColumn) && !BeeUtils.isEmpty(relation.getViewName())) {
      if (childInfo == null) {
        childInfo = Data.getDataInfo(table);
        if (childInfo == null) {
          return null;
        }
      }
      
      DataInfo sourceInfo = Data.getDataInfo(relation.getViewName());
      String sourceTable = (sourceInfo == null) ? relation.getViewName() : sourceInfo.getTableName();
      
      sourceColumn = childInfo.getRelationField(sourceTable);
      if (BeeUtils.isEmpty(sourceColumn)) {
        return null;
      }
    }
    
    if (BeeUtils.same(targetColumn, sourceColumn)) {
      return null;
    }
    
    return new ChildSelector(relation, table, targetColumn, sourceColumn);
  }
  
  @SuppressWarnings("unused")
  private final String childTable;
  @SuppressWarnings("unused")
  private final String targetRelColumn;
  @SuppressWarnings("unused")
  private final String sourceRelColumn;
  
  private ChildSelector(Relation relation, String childTable, String targetRelColumn,
      String sourceRelColumn) {
    super(relation, true, null);
    
    this.childTable = childTable;
    this.targetRelColumn = targetRelColumn;
    this.sourceRelColumn = sourceRelColumn;
  }
}
