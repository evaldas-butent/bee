package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;

import java.util.Collection;
import java.util.List;
import java.util.Set;

class ChartRowLayout {

  private final int dataIndex;

  private final Set<HasDateRange> inactivity = Sets.newHashSet();

  private final List<List<HasDateRange>> rows = Lists.newArrayList();

  private final Set<Range<JustDate>> overlap = Sets.newHashSet();

  ChartRowLayout(int dataIndex) {
    this.dataIndex = dataIndex;
  }

  void addInactivity(Collection<? extends HasDateRange> items, Range<JustDate> activeRange) {
    if (!items.isEmpty()) {
      inactivity.addAll(items);

      if (!rows.isEmpty()) {
        for (HasDateRange item : items) {
          for (List<HasDateRange> row : rows) {
            Set<Range<JustDate>> over = ChartHelper.getOverlap(row, item.getRange(), activeRange);
            if (!over.isEmpty()) {
              overlap.addAll(over);
            }
          }
        }
      }
    }
  }

  void addItems(Collection<? extends HasDateRange> items, Range<JustDate> activeRange,
      boolean addOverlap) {

    for (HasDateRange item : items) {
      boolean added = false;

      for (List<HasDateRange> row : rows) {
        Set<Range<JustDate>> over = ChartHelper.getOverlap(row, item.getRange(), activeRange);

        if (over.isEmpty()) {
          row.add(item);
          added = true;
          break;
        } else if (addOverlap) {
          overlap.addAll(over);
        }
      }

      if (!added) {
        rows.add(Lists.newArrayList(item));
      }
    }
  }

  int getDataIndex() {
    return dataIndex;
  }

  Set<HasDateRange> getInactivity() {
    return inactivity;
  }

  Set<Range<JustDate>> getOverlap() {
    return overlap;
  }

  List<List<HasDateRange>> getRows() {
    return rows;
  }

  int size() {
    return Math.max(rows.size(), 1);
  }
}
