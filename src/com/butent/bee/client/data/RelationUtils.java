package com.butent.bee.client.data;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.ViewColumn;

import java.util.Collection;

public class RelationUtils {

  public static int updateRow(String targetView, String targetColumn, IsRow targetRow,
      String sourceView, IsRow sourceRow) {
    return updateRow(targetView, targetColumn, targetRow, sourceView, sourceRow, false);
  }
  
  public static int updateRow(String targetView, String targetColumn, IsRow targetRow,
      String sourceView, IsRow sourceRow, boolean updateRelationColumn) {
    Assert.notEmpty(targetView);
    Assert.notEmpty(targetColumn);
    Assert.notNull(targetRow);
    Assert.notEmpty(sourceView);
    
    boolean clear = (sourceRow == null);
    
    int result = BeeConst.UNDEF;
    
    DataInfo targetInfo = Global.getDataInfo(targetView, true);
    if (targetInfo == null) {
      return result;
    }
    DataInfo sourceInfo = Global.getDataInfo(sourceView, true);
    if (sourceInfo == null) {
      return result;
    }

    if (updateRelationColumn) {
      int index = targetInfo.getColumnIndex(targetColumn);
      if (BeeConst.isUndef(index)) {
        BeeKeeper.getLog().warning(targetView, targetColumn, "column not found");
      } else {
        if (clear) {
          targetRow.clearCell(index);
        } else {
          targetRow.setValue(index, sourceRow.getId());
        }
        result = 1;
      }
    }
    
    Collection<ViewColumn> targetColumns = targetInfo.getDescendants(targetColumn, false);
    if (targetColumns.isEmpty()) {
      BeeKeeper.getLog().warning(targetView, targetColumn, "no descendants found");
      return result;
    }
    
    result = Math.max(result, 0);
    for (ViewColumn tc : targetColumns) {
      int targetIndex = targetInfo.getColumnIndex(tc.getName()); 
      if (BeeConst.isUndef(targetIndex)) {
        BeeKeeper.getLog().warning(targetView, tc.getName(), "column not found");
        continue;
      }
      
      if (clear) {
        targetRow.clearCell(targetIndex);
        result++;
      } else {
        int sourceIndex = sourceInfo.getColumnIndexBySource(tc.getTable(), tc.getField());
        if (!BeeConst.isUndef(sourceIndex)) {
          targetRow.setValue(targetIndex, sourceRow.getString(sourceIndex));
          result++;
        }
      }
    }
    return result;
  }

  private RelationUtils() {
  }
}
