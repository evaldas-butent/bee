package com.butent.bee.client.output;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

public class ReportInfo implements BeeSerializable {

  private enum Serial {
    ROW_ITEMS, COL_ITEMS, FILTER_ITEMS, ROW_GROUPING, COL_GROUPING
  }

  private String caption;
  private Long id;

  private Collection<ReportItem> colItems = new LinkedHashSet<>();
  private Collection<ReportItem> filterItems = new LinkedHashSet<>();
  private Collection<ReportItem> rowItems = new LinkedHashSet<>();
  private ReportItem colGrouping;
  private ReportItem rowGrouping;

  public ReportInfo(String caption) {
    this.caption = Assert.notEmpty(caption);
  }

  public void addColItem(ReportItem colItem) {
    colItems.add(colItem.enableCalculation());
  }

  public void addRowItem(ReportItem rowItem) {
    rowItems.add(rowItem);
  }

  @Override
  public void deserialize(String data) {
    Map<String, String> map = Codec.deserializeMap(data);

    if (!BeeUtils.isEmpty(map)) {
      for (Serial key : Serial.values()) {
        String value = map.get(key.name());

        switch (key) {
          case COL_GROUPING:
            setColGrouping(ReportItem.restore(value));
            break;
          case COL_ITEMS:
            colItems.clear();
            String[] items = Codec.beeDeserializeCollection(value);

            if (!ArrayUtils.isEmpty(items)) {
              for (String item : items) {
                colItems.add(ReportItem.restore(item));
              }
            }
            break;
          case FILTER_ITEMS:
            filterItems.clear();
            items = Codec.beeDeserializeCollection(value);

            if (!ArrayUtils.isEmpty(items)) {
              for (String item : items) {
                filterItems.add(ReportItem.restore(item));
              }
            }
            break;
          case ROW_GROUPING:
            setRowGrouping(ReportItem.restore(value));
            break;
          case ROW_ITEMS:
            rowItems.clear();
            items = Codec.beeDeserializeCollection(value);

            if (!ArrayUtils.isEmpty(items)) {
              for (String item : items) {
                rowItems.add(ReportItem.restore(item));
              }
            }
            break;
        }
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ReportInfo)) {
      return false;
    }
    return Objects.equals(caption, ((ReportInfo) obj).caption);
  }

  public String getCaption() {
    return caption;
  }

  public ReportItem getColGrouping() {
    return colGrouping;
  }

  public Collection<ReportItem> getColItems() {
    return colItems;
  }

  public Collection<ReportItem> getFilterItems() {
    return filterItems;
  }

  public Long getId() {
    return id;
  }

  public ReportItem getRowGrouping() {
    return rowGrouping;
  }

  public Collection<ReportItem> getRowItems() {
    return rowItems;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(caption);
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getRowItems()) && BeeUtils.isEmpty(getColItems());
  }

  @Override
  public String serialize() {
    Map<String, Object> map = new HashMap<>();

    for (Serial key : Serial.values()) {
      Object value = null;

      switch (key) {
        case COL_GROUPING:
          value = getColGrouping();
          break;
        case COL_ITEMS:
          value = getColItems();
          break;
        case FILTER_ITEMS:
          value = getFilterItems();
          break;
        case ROW_GROUPING:
          value = getRowGrouping();
          break;
        case ROW_ITEMS:
          value = getRowItems();
          break;
      }
      map.put(key.name(), value);
    }
    return Codec.beeSerialize(map);
  }

  public void setColGrouping(ReportItem groupItem) {
    colGrouping = groupItem;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setRowGrouping(ReportItem groupItem) {
    rowGrouping = groupItem;
  }
}
