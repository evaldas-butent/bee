package com.butent.bee.client.output;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReportInfo implements BeeSerializable {

  private enum Serial {
    CAPTION, ROW_ITEMS, COL_ITEMS, FILTER_ITEMS, ROW_GROUPING, COL_GROUPING
  }

  private String caption;
  private Long id;

  private final List<ReportItem> colItems = new ArrayList<>();
  private final List<ReportItem> filterItems = new ArrayList<>();
  private final List<ReportItem> rowItems = new ArrayList<>();
  private ReportItem colGrouping;
  private ReportItem rowGrouping;

  public ReportInfo(String caption) {
    setCaption(caption);
  }

  private ReportInfo() {
  }

  public void addColItem(ReportItem colItem) {
    if (colItem.getFunction() == null) {
      colItem.enableCalculation();
    }
    colItems.add(colItem);
  }

  public void addRowItem(ReportItem rowItem) {
    rowItems.add(rowItem.setFunction(null));
  }

  @Override
  public void deserialize(String data) {
    Map<String, String> map = Codec.deserializeMap(data);

    if (!BeeUtils.isEmpty(map)) {
      for (Serial key : Serial.values()) {
        String value = map.get(key.name());

        switch (key) {
          case CAPTION:
            if (BeeUtils.isEmpty(getCaption())) {
              setCaption(value);
            }
            break;
          case COL_GROUPING:
            setColGrouping(ReportItem.restore(value));
            break;
          case COL_ITEMS:
            colItems.clear();
            String[] items = Codec.beeDeserializeCollection(value);

            if (!ArrayUtils.isEmpty(items)) {
              for (String item : items) {
                addColItem(ReportItem.restore(item));
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
                addRowItem(ReportItem.restore(item));
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

  public List<ReportItem> getColItems() {
    return colItems;
  }

  public List<ReportItem> getFilterItems() {
    return filterItems;
  }

  public Long getId() {
    return id;
  }

  public ReportItem getRowGrouping() {
    return rowGrouping;
  }

  public List<ReportItem> getRowItems() {
    return rowItems;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(caption);
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getRowItems()) && BeeUtils.isEmpty(getColItems());
  }

  public static ReportInfo restore(String data) {
    ReportInfo reportInfo = new ReportInfo();
    reportInfo.deserialize(Assert.notEmpty(data));
    return reportInfo;
  }

  @Override
  public String serialize() {
    Map<String, Object> map = new HashMap<>();

    for (Serial key : Serial.values()) {
      Object value = null;

      switch (key) {
        case CAPTION:
          value = getCaption();
          break;
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
    if (groupItem != null) {
      groupItem.setFunction(null);
    }
    colGrouping = groupItem;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setRowGrouping(ReportItem groupItem) {
    if (groupItem != null) {
      groupItem.setFunction(null);
    }
    rowGrouping = groupItem;
  }

  private void setCaption(String caption) {
    this.caption = Assert.notEmpty(caption);
  }
}
