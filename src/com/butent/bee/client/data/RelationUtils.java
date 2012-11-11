package com.butent.bee.client.data;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.Defaults;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class RelationUtils {

  private static final BeeLogger logger = LogUtils.getLogger(RelationUtils.class);
  
  public static int setDefaults(String viewName, IsRow row, Collection<String> colNames,
      List<BeeColumn> columns) {
    int result = 0;
    if (BeeUtils.isEmpty(viewName) || BeeUtils.isEmpty(colNames)) {
      return result;
    }

    DataInfo dataInfo = Data.getDataInfo(viewName);
    if (dataInfo == null) {
      return result;
    }
    
    return setDefaults(dataInfo, row, colNames, columns);
  }

  public static int setDefaults(DataInfo dataInfo, IsRow row, Collection<String> colNames,
      List<BeeColumn> columns) {
    int result = 0;
    if (dataInfo == null || row == null || BeeUtils.isEmpty(colNames)
        || BeeUtils.isEmpty(columns)) {
      return result;
    }

    if (!BeeKeeper.getUser().isLoggedIn()) {
      return result;
    }

    for (String colName : colNames) {
      BeeColumn column = DataUtils.getColumn(colName, columns);
      if (column == null || !column.hasDefaults()) {
        continue;
      }
      if (!Defaults.DefaultExpression.CURRENT_USER.equals(column.getDefaults().getA())) {
        continue;
      }

      Collection<ViewColumn> descendants = dataInfo.getDescendants(column.getId(), false);
      if (descendants.isEmpty()) {
        continue;
      }

      for (ViewColumn vc : descendants) {
        int index = DataUtils.getColumnIndex(vc.getName(), columns);
        if (BeeConst.isUndef(index)) {
          continue;
        }

        if (BeeUtils.same(vc.getField(), UserData.FLD_FIRST_NAME)) {
          row.setValue(index, BeeKeeper.getUser().getFirstName());
          result++;
        } else if (BeeUtils.same(vc.getField(), UserData.FLD_LAST_NAME)) {
          row.setValue(index, BeeKeeper.getUser().getLastName());
          result++;
        }
      }
    }
    return result;
  }
  
  public static int setRelatedValues(String viewName, String colName, IsRow targetRow,
      IsRow sourceRow) {
    int result = 0;
    if (BeeUtils.isEmpty(viewName) || BeeUtils.isEmpty(colName) || targetRow == null
        || sourceRow == null) {
      return result;
    }

    DataInfo dataInfo = Data.getDataInfo(viewName);
    if (dataInfo == null) {
      return result;
    }

    Collection<ViewColumn> descendants = dataInfo.getDescendants(colName, false);
    if (descendants.isEmpty()) {
      return result;
    }

    for (ViewColumn vc : descendants) {
      int index = dataInfo.getColumnIndex(vc.getName());
      if (!BeeConst.isUndef(index)) {
        targetRow.setValue(index, sourceRow.getString(index));
        result++;
      }
    }
    return result;
  }

  public static int updateRow(String targetView, String targetColumn, IsRow targetRow,
      String sourceView, IsRow sourceRow, boolean updateRelationColumn) {
    Assert.notEmpty(targetView);
    Assert.notEmpty(targetColumn);
    Assert.notNull(targetRow);
    Assert.notEmpty(sourceView);

    boolean clear = (sourceRow == null);

    int result = BeeConst.UNDEF;

    DataInfo targetInfo = Data.getDataInfo(targetView);
    if (targetInfo == null) {
      return result;
    }
    DataInfo sourceInfo = Data.getDataInfo(sourceView);
    if (sourceInfo == null) {
      return result;
    }

    if (updateRelationColumn) {
      int index = targetInfo.getColumnIndex(targetColumn);
      if (BeeConst.isUndef(index)) {
        logger.warning(targetView, targetColumn, "column not found");
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
      logger.warning(targetView, targetColumn, "no descendants found");
      return result;
    }

    result = Math.max(result, 0);
    for (ViewColumn tc : targetColumns) {
      int targetIndex = targetInfo.getColumnIndex(tc.getName());
      if (BeeConst.isUndef(targetIndex)) {
        logger.warning(targetView, tc.getName(), "column not found");
        continue;
      }

      if (clear) {
        if (!targetRow.isNull(targetIndex)) {
          targetRow.clearCell(targetIndex);
          result++;
        }

      } else {
        int sourceIndex = sourceInfo.getColumnIndexBySource(tc.getTable(), tc.getField(),
            tc.getLevel() - 1);
        if (!BeeConst.isUndef(sourceIndex) && 
            !BeeUtils.equalsTrimRight(targetRow.getString(targetIndex),
                sourceRow.getString(sourceIndex))) {
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
