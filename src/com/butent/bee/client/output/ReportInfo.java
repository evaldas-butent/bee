package com.butent.bee.client.output;

import com.butent.bee.client.output.ReportItem.Function;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReportInfo implements BeeSerializable {

  private enum Serial {
    CAPTION, ROW_ITEMS, COL_ITEMS, FILTER_ITEMS, ROW_GROUPING, COL_GROUPING
  }

  private enum ItemSerial {
    ITEM, RELATION, FUNCTION, COL_SUMMARY, GROUP_SUMMARY, ROW_SUMMARY
  }

  public final class ReportInfoItem implements BeeSerializable {

    private ReportItem item;
    private String relation;

    private Function function;
    private boolean colSummary;
    private boolean groupSummary;
    private boolean rowSummary;

    private ReportInfoItem(ReportItem item) {
      this.item = Assert.notNull(item);
    }

    private ReportInfoItem() {
    }

    @Override
    public void deserialize(String data) {
      Map<String, String> map = Codec.deserializeMap(data);

      if (!BeeUtils.isEmpty(map)) {
        for (ItemSerial key : ItemSerial.values()) {
          String value = map.get(key.name());

          switch (key) {
            case COL_SUMMARY:
              colSummary = BeeUtils.toBoolean(value);
              break;
            case FUNCTION:
              function = EnumUtils.getEnumByName(Function.class, value);
              break;
            case GROUP_SUMMARY:
              groupSummary = BeeUtils.toBoolean(value);
              break;
            case ITEM:
              item = ReportItem.restore(value);
              break;
            case RELATION:
              setRelation(value);
              break;
            case ROW_SUMMARY:
              rowSummary = BeeUtils.toBoolean(value);
              break;
            default:
              break;
          }
        }
      }
    }

    public ReportItem getItem() {
      return item;
    }

    public Function getFunction() {
      return function;
    }

    public String getRelation() {
      return relation;
    }

    public String getStyle() {
      if (getFunction() != null) {
        switch (getFunction()) {
          case LIST:
            return ReportItem.STYLE_TEXT;

          case COUNT:
          case SUM:
            return ReportItem.STYLE_NUM;

          default:
            break;
        }
      }
      return getItem().getStyle();
    }

    public boolean isColSummary() {
      return colSummary;
    }

    public boolean isGroupSummary() {
      return groupSummary;
    }

    public boolean isRowSummary() {
      return rowSummary;
    }

    @Override
    public String serialize() {
      Map<String, Object> map = new HashMap<>();

      for (ItemSerial key : ItemSerial.values()) {
        Object value = null;

        switch (key) {
          case COL_SUMMARY:
            value = isColSummary();
            break;
          case FUNCTION:
            value = getFunction();
            break;
          case GROUP_SUMMARY:
            value = isGroupSummary();
            break;
          case ITEM:
            value = getItem();
            break;
          case RELATION:
            value = getRelation();
            break;
          case ROW_SUMMARY:
            value = isRowSummary();
            break;
        }
        map.put(key.name(), value);
      }
      return Codec.beeSerialize(map);
    }

    public void setRelation(String relation) {
      this.relation = relation;
    }
  }

  private String caption;
  private Long id;

  private final List<ReportInfoItem> colItems = new ArrayList<>();
  private final List<ReportItem> filterItems = new ArrayList<>();
  private final List<ReportInfoItem> rowItems = new ArrayList<>();
  private ReportInfoItem colGrouping;
  private ReportInfoItem rowGrouping;

  public ReportInfo(String caption) {
    setCaption(caption);
  }

  private ReportInfo() {
  }

  public void addColItem(ReportItem colItem) {
    colItems.add(new ReportInfoItem(colItem));
    int idx = colItems.size() - 1;

    if (colItem instanceof ReportNumericItem) {
      setFunction(idx, Function.SUM);
      setColSummary(idx, true);
      setGroupSummary(idx, true);
      setRowSummary(idx, true);
    } else {
      setFunction(idx, Function.LIST);
    }
  }

  public void addRowItem(ReportItem rowItem) {
    rowItems.add(new ReportInfoItem(rowItem));
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
            ReportInfoItem groupItem = null;

            if (!BeeUtils.isEmpty(value)) {
              groupItem = new ReportInfoItem();
              groupItem.deserialize(value);
            }
            colGrouping = groupItem;
            break;
          case COL_ITEMS:
            colItems.clear();
            String[] items = Codec.beeDeserializeCollection(value);

            if (!ArrayUtils.isEmpty(items)) {
              for (String item : items) {
                ReportInfoItem infoItem = new ReportInfoItem();
                infoItem.deserialize(item);
                colItems.add(infoItem);
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
            groupItem = null;

            if (!BeeUtils.isEmpty(value)) {
              groupItem = new ReportInfoItem();
              groupItem.deserialize(value);
            }
            rowGrouping = groupItem;
            break;
          case ROW_ITEMS:
            rowItems.clear();
            items = Codec.beeDeserializeCollection(value);

            if (!ArrayUtils.isEmpty(items)) {
              for (String item : items) {
                ReportInfoItem infoItem = new ReportInfoItem();
                infoItem.deserialize(item);
                rowItems.add(infoItem);
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

  public ReportInfoItem getColGrouping() {
    return colGrouping;
  }

  public List<ReportInfoItem> getColItems() {
    return colItems;
  }

  public List<ReportItem> getFilterItems() {
    return filterItems;
  }

  public Long getId() {
    return id;
  }

  public ReportInfoItem getRowGrouping() {
    return rowGrouping;
  }

  public List<ReportInfoItem> getRowItems() {
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
    ReportInfoItem infoItem = null;

    if (groupItem != null) {
      infoItem = new ReportInfoItem(groupItem);

    }
    colGrouping = infoItem;
  }

  public void setColSummary(int colIndex, boolean summary) {
    ReportInfoItem item = BeeUtils.getQuietly(getColItems(), colIndex);

    if (item != null) {
      item.colSummary = summary;
    }
  }

  public void setFunction(int colIndex, Function function) {
    ReportInfoItem item = BeeUtils.getQuietly(getColItems(), colIndex);

    if (item != null) {
      item.function = Assert.notNull(function);
    }
  }

  public void setGroupSummary(int colIndex, boolean summary) {
    ReportInfoItem item = BeeUtils.getQuietly(getColItems(), colIndex);

    if (item != null) {
      item.groupSummary = summary;
    }
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setRowGrouping(ReportItem groupItem) {
    ReportInfoItem infoItem = null;

    if (groupItem != null) {
      infoItem = new ReportInfoItem(groupItem);
    }
    rowGrouping = infoItem;
  }

  public void setRowSummary(int colIndex, boolean summary) {
    ReportInfoItem item = BeeUtils.getQuietly(getColItems(), colIndex);

    if (item != null) {
      item.rowSummary = summary;
    }
  }

  private void setCaption(String caption) {
    this.caption = Assert.notEmpty(caption);
  }
}
