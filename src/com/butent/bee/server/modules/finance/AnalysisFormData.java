package com.butent.bee.server.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AnalysisFormData {

  private final BeeRow header;
  private final Map<String, Integer> headerIndexes;

  private final Map<String, Integer> columnIndexes;
  private final List<BeeRow> selectedColumns = new ArrayList<>();
  private final List<BeeRow> deselectedColumns = new ArrayList<>();

  private final Map<String, Integer> rowIndexes;
  private final List<BeeRow> selectedRows = new ArrayList<>();
  private final List<BeeRow> deselectedRows = new ArrayList<>();

  private final List<BeeRow> headerFilters = new ArrayList<>();
  private final Map<Long, BeeRow> columnFilters = new HashMap<>();
  private final Map<Long, BeeRow> rowFilters = new HashMap<>();
  private final Map<String, Integer> filterIndexes;

  AnalysisFormData(BeeRowSet headerData, BeeRowSet columnData, BeeRowSet rowData,
      BeeRowSet filterData) {

    this.header = DataUtils.isEmpty(headerData) ? null : headerData.getRow(0);
    this.headerIndexes = AnalysisUtils.getIndexes(headerData);

    this.columnIndexes = AnalysisUtils.getIndexes(columnData);
    if (!DataUtils.isEmpty(columnData)) {
      int index = columnIndexes.get(COL_ANALYSIS_COLUMN_SELECTED);

      for (BeeRow row : columnData) {
        if (row.isTrue(index)) {
          selectedColumns.add(row);
        } else {
          deselectedColumns.add(row);
        }
      }
    }

    this.rowIndexes = AnalysisUtils.getIndexes(rowData);
    if (!DataUtils.isEmpty(rowData)) {
      int index = rowIndexes.get(COL_ANALYSIS_ROW_SELECTED);

      for (BeeRow row : columnData) {
        if (row.isTrue(index)) {
          selectedRows.add(row);
        } else {
          deselectedRows.add(row);
        }
      }
    }

    this.filterIndexes = AnalysisUtils.getIndexes(filterData);

    if (!DataUtils.isEmpty(filterData)) {
      for (BeeRow row : filterData) {
        Long parent = row.getLong(filterIndexes.get(COL_ANALYSIS_HEADER));

        if (DataUtils.isId(parent)) {
          headerFilters.add(row);

        } else {
          parent = row.getLong(filterIndexes.get(COL_ANALYSIS_COLUMN));
          if (DataUtils.isId(parent)) {
            columnFilters.put(parent, row);

          } else {
            parent = row.getLong(filterIndexes.get(COL_ANALYSIS_ROW));
            if (DataUtils.isId(parent)) {
              rowFilters.put(parent, row);
            }
          }
        }
      }
    }
  }

  List<String> validate() {
    List<String> messages = new ArrayList<>();

    if (header == null) {
      messages.add("header not available");
    }
    if (BeeUtils.isEmpty(selectedColumns)) {
      messages.add(BeeUtils.isEmpty(deselectedColumns)
          ? "columns not available" : "columns not selected");
    }
    if (BeeUtils.isEmpty(selectedRows)) {
      messages.add(BeeUtils.isEmpty(deselectedRows)
          ? "rows not available" : "rows not selected");
    }

    return messages;
  }
}
