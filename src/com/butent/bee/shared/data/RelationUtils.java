package com.butent.bee.shared.data;

import com.google.common.collect.Lists;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RelationUtils {

  private static final BeeLogger logger = LogUtils.getLogger(RelationUtils.class);

  public static List<String> getRenderColumns(DataInfo dataInfo, String colName) {
    List<String> result = Lists.newArrayList();
    if (dataInfo == null || BeeUtils.isEmpty(colName)) {
      return result;
    }

    Collection<ViewColumn> descendants = dataInfo.getDescendants(colName, false);

    if (!descendants.isEmpty()) {
      List<Integer> columnIndexes = Lists.newArrayList();
      for (ViewColumn vc : descendants) {
        if (BeeUtils.isEmpty(vc.getRelation())) {
          int index = dataInfo.getColumnIndex(vc.getName());
          if (!BeeConst.isUndef(index)) {
            columnIndexes.add(index);
          }
        }
      }

      if (columnIndexes.size() > 1) {
        Collections.sort(columnIndexes);
      }
      for (int index : columnIndexes) {
        result.add(dataInfo.getColumnId(index));
      }
    }
    return result;
  }

  public static int setDefaults(DataInfo dataInfo, IsRow row, Collection<String> colNames,
      List<BeeColumn> columns, UserData userData) {
    int result = 0;
    if (dataInfo == null || row == null || BeeUtils.isEmpty(colNames)
        || BeeUtils.isEmpty(columns) || userData == null) {
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
          row.setValue(index, userData.getFirstName());
          result++;
        } else if (BeeUtils.same(vc.getField(), UserData.FLD_LAST_NAME)) {
          row.setValue(index, userData.getLastName());
          result++;
        } else if (BeeUtils.same(vc.getField(), UserData.FLD_COMPANY_PERSON)) {
          row.setValue(index, userData.getCompanyPerson());
          result++;
        }
      }
    }
    return result;
  }

  public static int setRelatedValues(DataInfo dataInfo, String colName, IsRow targetRow,
      IsRow sourceRow) {
    int result = 0;
    if (dataInfo == null || BeeUtils.isEmpty(colName) || targetRow == null
        || sourceRow == null) {
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

  public static int updateRow(DataInfo targetInfo, String targetColumn, IsRow targetRow,
      DataInfo sourceInfo, IsRow sourceRow, boolean updateRelationColumn) {
    int result = BeeConst.UNDEF;
    if (targetInfo == null || sourceInfo == null || BeeUtils.isEmpty(targetColumn)
        || targetRow == null) {
      return result;
    }

    boolean clear = (sourceRow == null);

    if (updateRelationColumn) {
      int index = targetInfo.getColumnIndex(targetColumn);
      if (BeeConst.isUndef(index)) {
        logger.warning(targetInfo.getViewName(), targetColumn, "column not found");
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
      logger.warning(targetInfo.getViewName(), targetColumn, "no descendants found");
      return result;
    }

    result = Math.max(result, 0);
    for (ViewColumn tc : targetColumns) {
      int targetIndex = targetInfo.getColumnIndex(tc.getName());
      if (BeeConst.isUndef(targetIndex)) {
        logger.warning(targetInfo.getViewName(), tc.getName(), "column not found");
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
