package com.butent.bee.server.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AnalysisFormData {

  private static Map<String, Integer> getIndexes(BeeRowSet rowSet) {
    Map<String, Integer> indexes = new HashMap<>();

    if (rowSet != null) {
      for (int i = 0; i < rowSet.getNumberOfColumns(); i++) {
        indexes.put(rowSet.getColumn(i).getId(), i);
      }
    }

    return indexes;
  }

  private final BeeRow header;
  private final Map<String, Integer> headerIndexes;

  private final List<BeeRow> columns = new ArrayList<>();
  private final Map<String, Integer> columnIndexes;

  private final List<BeeRow> rows = new ArrayList<>();
  private final Map<String, Integer> rowIndexes;

  private final Map<String, Integer> filterIndexes;

  private final List<BeeRow> headerFilters = new ArrayList<>();
  private final Map<Long, BeeRow> columnFilters = new HashMap<>();
  private final Map<Long, BeeRow> rowFilters = new HashMap<>();

  AnalysisFormData(BeeRowSet headerData, BeeRowSet columnData, BeeRowSet rowData,
      BeeRowSet filterData) {

    this.header = DataUtils.isEmpty(headerData) ? null : headerData.getRow(0);
    this.headerIndexes = getIndexes(headerData);

    if (!DataUtils.isEmpty(columnData)) {
      this.columns.addAll(columnData.getRows());
    }
    this.columnIndexes = getIndexes(columnData);

    if (!DataUtils.isEmpty(rowData)) {
      this.rows.addAll(rowData.getRows());
    }
    this.rowIndexes = getIndexes(rowData);

    this.filterIndexes = getIndexes(filterData);

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

    return messages;
  }
}
