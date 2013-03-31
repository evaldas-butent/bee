package com.butent.bee.client.modules.transport.charts;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

class ChartRowLayout {

  interface Blender {
    boolean willItBlend(HasDateRange x, HasDateRange y);
  }
  
  static class GroupLayout {
    private final Long groupId;
    
    private final int firstRow;
    private final int lastRow;

    private final boolean hasOverlap;

    private GroupLayout(Long groupId, int firstRow, int lastRow, boolean hasOverlap) {
      this.groupId = groupId;
      this.firstRow = firstRow;
      this.lastRow = lastRow;
      this.hasOverlap = hasOverlap;
    }

    Long getGroupId() {
      return groupId;
    }

    int getFirstRow() {
      return firstRow;
    }

    int getLastRow() {
      return lastRow;
    }

    boolean hasOverlap() {
      return hasOverlap;
    }
  }

  static class RowData {
    private final Long groupId;
    private final List<HasDateRange> rowItems = Lists.newArrayList();

    private RowData(Long groupId, HasDateRange item) {
      this.groupId = groupId;
      add(item);
    }

    Long getGroupId() {
      return groupId;
    }

    List<HasDateRange> getRowItems() {
      return rowItems;
    }

    boolean hasGroup(Long id) {
      return Objects.equal(id, groupId);
    }

    private void add(HasDateRange item) {
      if (item != null) {
        rowItems.add(item);
      }
    }
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

  static int countRows(List<ChartRowLayout> layout, int minSize) {
    int result = 0;
    for (ChartRowLayout crl : layout) {
      result += crl.getSize(minSize);
    }
    return result;
  }

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

  private final List<RowData> rows = Lists.newArrayList();

  private final Set<Range<JustDate>> overlap = Sets.newHashSet();

  ChartRowLayout(int dataIndex) {
    this.dataIndex = dataIndex;
  }

  void addInactivity(Collection<? extends HasDateRange> items, Range<JustDate> activeRange) {
    if (!items.isEmpty()) {
      inactivity.addAll(items);

      if (!rows.isEmpty()) {
        for (HasDateRange item : items) {
          for (RowData rowData : rows) {
            Set<Range<JustDate>> over = clash(rowData.getRowItems(), item.getRange(), activeRange);
            if (!over.isEmpty()) {
              overlap.addAll(over);
            }
          }
        }
      }
    }
  }

  void addItem(Long itemGroupId, HasDateRange item, Range<JustDate> activeRange, Blender blender) {
    boolean added = false;

    for (RowData rowData : rows) {
      Set<Range<JustDate>> over = clash(rowData.getRowItems(), item.getRange(), activeRange);

      if (over.isEmpty()) {
        if (!added && rowData.hasGroup(itemGroupId)) {
          rowData.add(item);
          added = true;
        }

      } else if (blender == null) {
        overlap.addAll(over);

      } else {
        List<HasDateRange> incompatible = Lists.newArrayList();
        for (HasDateRange rowItem : rowData.getRowItems()) {
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
      rows.add(new RowData(itemGroupId, item));
    }
  }

  void addItems(Long itemGroupId, Collection<? extends HasDateRange> items,
      Range<JustDate> activeRange) {
    addItems(itemGroupId, items, activeRange, null);
  }

  void addItems(Long itemGroupId, Collection<? extends HasDateRange> items,
      Range<JustDate> activeRange, Blender blender) {

    for (HasDateRange item : items) {
      addItem(itemGroupId, item, activeRange, blender);
    }
  }

  int getDataIndex() {
    return dataIndex;
  }
  
  List<GroupLayout> getGroups() {
    List<GroupLayout> result = Lists.newArrayList();
    if (rows.isEmpty()) {
      return result;
    }
    
    Long lastGroup = null;
    int firstRow = 0;
    boolean over = false;
    
    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      Long currentGroup = rows.get(rowIndex).getGroupId();

      if (rowIndex == 0) {
        lastGroup = currentGroup;

      } else if (!Objects.equal(lastGroup, currentGroup)) {
        result.add(new GroupLayout(lastGroup, firstRow, rowIndex - 1, over));

        lastGroup = currentGroup;
        firstRow = rowIndex;
        over = false;
      }

      if (!over && hasOverlap()) {
        over = overlaps(rows.get(rowIndex).getRowItems());
      }
      
      if (rowIndex == rows.size() - 1) {
        result.add(new GroupLayout(lastGroup, firstRow, rowIndex, over));
      }
    }
    
    return result;
  }

  Set<HasDateRange> getInactivity() {
    return inactivity;
  }

  Set<Range<JustDate>> getOverlap(Range<JustDate> activeRange) {
    return clash(overlap, activeRange);
  }

  List<RowData> getRows() {
    return rows;
  }

  int getSize(int minSize) {
    return Math.max(rows.size(), minSize);
  }

  boolean hasOverlap() {
    return !overlap.isEmpty();
  }
  
  boolean isEmpty() {
    return rows.isEmpty();
  }
  
  private boolean overlaps(Collection<? extends HasDateRange> items) {
    if (items == null || !hasOverlap()) {
      return false;
    }
    
    for (Range<JustDate> range : overlap) {
      if (BeeUtils.intersects(items, range)) {
        return true;
      }
    }
    return false;
  }
}
