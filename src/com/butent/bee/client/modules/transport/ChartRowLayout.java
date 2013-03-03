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

  private final Set<Range<JustDate>> inactivity = Sets.newHashSet();

  private final List<List<HasDateRange>> rows = Lists.newArrayList();

  private final Set<Range<JustDate>> overlap = Sets.newHashSet();

  ChartRowLayout(int dataIndex) {
    this.dataIndex = dataIndex;
  }

  void add(Collection<? extends HasDateRange> items, Range<JustDate> range) {
    for (HasDateRange item : items) {
      boolean added = false;

      for (List<HasDateRange> row : rows) {
        Set<Range<JustDate>> over = ChartHelper.getOverlap(row, item.getRange(), range);

        if (over.isEmpty()) {
          row.add(item);
          added = true;
          break;
        } else {
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

  Set<Range<JustDate>> getInactivity() {
    return inactivity;
  }

  Set<Range<JustDate>> getOverlap() {
    return overlap;
  }

  List<List<HasDateRange>> getRows() {
    return rows;
  }

  void setInactivity(HasDateRange ttt, Range<JustDate> range) {
    List<Range<JustDate>> off = ChartHelper.getInactivity(ttt, range);
    if (off.isEmpty()) {
      return;
    }

    inactivity.addAll(off);
    if (rows.isEmpty()) {
      return;
    }

    for (Range<JustDate> item : off) {
      for (List<HasDateRange> row : rows) {
        Set<Range<JustDate>> over = ChartHelper.getOverlap(row, item, range);
        if (!over.isEmpty()) {
          overlap.addAll(over);
        }
      }
    }
  }

  int size() {
    return Math.max(rows.size(), 1);
  }
}
