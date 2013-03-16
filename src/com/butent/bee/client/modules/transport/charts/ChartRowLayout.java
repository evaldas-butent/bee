package com.butent.bee.client.modules.transport.charts;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;

import java.util.Collection;
import java.util.List;
import java.util.Set;

class ChartRowLayout {
  
  interface Blender {
    boolean willItBlend(HasDateRange x, HasDateRange y);
  }

  private static class FreightBlender implements Blender {
    FreightBlender() {
      super();
    }

    @Override
    public boolean willItBlend(HasDateRange x, HasDateRange y) {
      if (x instanceof Freight && y instanceof Freight) {
        return Objects.equal(((Freight) x).getTripId(), ((Freight) y).getTripId());
      } else {
        return false;
      }
    }
  }

  static final FreightBlender FREIGHT_BLENDER = new FreightBlender();
  
  private static Set<Range<JustDate>> clash(Collection<? extends HasDateRange> items,
      Range<JustDate> range, Range<JustDate> activeRange) {

    Set<Range<JustDate>> overlap = intersection(items, range);
    if (overlap.isEmpty() || activeRange == null) {
      return overlap;
    } else {
      return clash(overlap, activeRange);
    }
  }

  private static Set<Range<JustDate>> clash(Collection<Range<JustDate>> ranges,
      Range<JustDate> range) {

    Set<Range<JustDate>> result = Sets.newHashSet();
    if (ranges == null || range == null) {
      return result;
    }

    for (Range<JustDate> item : ranges) {
      if (item != null && item.isConnected(range)) {
        Range<JustDate> section = item.intersection(range);
        if (!section.isEmpty()) {
          result.add(section);
        }
      }
    }
    return result;
  }
  
  private static Set<Range<JustDate>> intersection(Collection<? extends HasDateRange> items,
      Range<JustDate> range) {
    
    Set<Range<JustDate>> result = Sets.newHashSet();
    if (items == null || range == null) {
      return result;
    }

    for (HasDateRange item : items) {
      if (item != null && item.getRange() != null && item.getRange().isConnected(range)) {
        Range<JustDate> section = item.getRange().intersection(range);
        if (!section.isEmpty()) {
          result.add(section);
        }
      }
    }
    return result;
  }
  
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
            Set<Range<JustDate>> over = clash(row, item.getRange(), activeRange);
            if (!over.isEmpty()) {
              overlap.addAll(over);
            }
          }
        }
      }
    }
  }

  void addItems(Collection<? extends HasDateRange> items, Range<JustDate> activeRange) {
    addItems(items, activeRange, null);
  }
  
  void addItems(Collection<? extends HasDateRange> items, Range<JustDate> activeRange,
      Blender blender) {

    for (HasDateRange item : items) {
      boolean added = false;

      for (List<HasDateRange> row : rows) {
        Set<Range<JustDate>> over = clash(row, item.getRange(), activeRange);

        if (over.isEmpty()) {
          if (!added) {
            row.add(item);
            added = true;
          }

        } else if (blender == null) {
          overlap.addAll(over);

        } else {
          List<HasDateRange> incompatible = Lists.newArrayList();
          for (HasDateRange rowItem : row) {
            if (!blender.willItBlend(item, rowItem)) {
              incompatible.add(rowItem);
            }
          }
          
          if (!incompatible.isEmpty()) {
            overlap.addAll(clash(incompatible, item.getRange(), activeRange));
          }
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

  Set<Range<JustDate>> getOverlap(Range<JustDate> activeRange) {
    return clash(overlap, activeRange);
  }

  List<List<HasDateRange>> getRows() {
    return rows;
  }
  
  boolean hasOverlap() {
    return !overlap.isEmpty();
  }

  int size() {
    return Math.max(rows.size(), 1);
  }
}
