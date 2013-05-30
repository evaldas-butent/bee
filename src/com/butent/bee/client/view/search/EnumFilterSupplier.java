package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;

public class EnumFilterSupplier extends AbstractFilterSupplier {

  private static class DataItem {
    private final int index;
    private final String count;

    private DataItem(int index, String count) {
      this.index = index;
      this.count = count;
    }

    private String getCount() {
      return count;
    }

    private int getIndex() {
      return index;
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(EnumFilterSupplier.class);
  
  private final List<String> captions;
  private final int nullIndex;

  private final List<DataItem> data = Lists.newArrayList();
  private final List<Integer> values = Lists.newArrayList();

  public EnumFilterSupplier(String viewName, BeeColumn column, String label, String options,
      String key) {
    super(viewName, column, label, options);

    this.captions = Captions.getCaptions(key);
    this.nullIndex = (captions == null) ? BeeConst.UNDEF : captions.size();
  }

  @Override
  public String getLabel() {
    if (values.isEmpty()) {
      return null;
    }

    List<String> labels = Lists.newArrayList();
    for (int index : values) {
      labels.add(getCaption(index));
    }
    return BeeUtils.join(BeeConst.STRING_COMMA, labels);
  }

  @Override
  public String getValue() {
    return BeeUtils.joinInts(values);
  }

  @Override
  public void onRequest(final Element target, final Scheduler.ScheduledCommand onChange) {

    if (captions == null) {
      logger.severe(NameUtils.getName(this) + ": enum captions not available");
      return;
    }

    getHistogram(new Callback<SimpleRowSet>() {
      @Override
      public void onSuccess(SimpleRowSet result) {
        data.clear();

        int valueIndex = result.getColumnIndex(getColumnId());
        int countIndex = result.getNumberOfColumns() - 1;

        String nullCount = null;

        for (String[] row : result.getRows()) {
          if (BeeUtils.isInt(row[valueIndex])) {
            int ordinal = BeeUtils.toInt(row[valueIndex]);
            if (BeeUtils.isIndex(captions, ordinal)) {
              data.add(new DataItem(ordinal, row[countIndex]));
            } else {
              logger.severe("Invalid value: " + ordinal);
            }
          } else {
            nullCount = row[countIndex];
          }
        }

        if (data.isEmpty()) {
          notifyInfo(messageAllEmpty(nullCount));

        } else if (data.size() == 1 && BeeUtils.isEmpty(nullCount)) {
          notifyInfo(messageOneValue(captions.get(data.get(0).getIndex()), data.get(0).getCount()));

        } else {
          if (!BeeUtils.isEmpty(nullCount)) {
            data.add(new DataItem(nullIndex, nullCount));
          }
          openDialog(target, createWidget(), onChange);
        }
      }
    });
  }

  @Override
  public Filter parse(String value) {
    if (BeeUtils.isEmpty(value)) {
      return null;
    } else {
      return buildFilter(BeeUtils.toInts(value));
    }
  }

  @Override
  public boolean reset() {
    data.clear();
    return super.reset();
  }

  @Override
  public void setValue(String value) {
    BeeUtils.overwrite(values, BeeUtils.toInts(value));
  }

  @Override
  protected void doClear() {
    clearDisplay();
    super.doClear();
  }

  @Override
  protected void doCommit() {
    List<Integer> newValues = Lists.newArrayList();
    for (int row : getSelectedItems()) {
      newValues.add(data.get(row).getIndex());
    }
    
    boolean changed = !BeeUtils.sameElements(values, newValues);
    
    BeeUtils.overwrite(values, newValues);
    update(changed);
  }

  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList(SupplierAction.COMMIT, SupplierAction.CLEAR);
  }

  private Filter buildFilter(Collection<Integer> ordinals) {
    if (BeeUtils.isEmpty(ordinals)) {
      return null;
    }
    
    List<Filter> filters = Lists.newArrayList();

    for (Integer ordinal : ordinals) {
      if (ordinal != null) {
        if (ordinal.equals(nullIndex)) {
          filters.add(Filter.isEmpty(getColumnId()));
        } else {
          filters.add(ComparisonFilter.isEqual(getColumnId(), new IntegerValue(ordinal)));
        }
      }
    }

    return Filter.or(filters);
  }

  private Widget createWidget() {
    clearSelection();
    HtmlTable display = createDisplay(true);

    int row = 0;
    for (DataItem dataItem : data) {
      int col = 0;
      display.setText(row, col++, getCaption(dataItem.getIndex()));
      addBinSize(display, row, col, dataItem.getCount());
      
      if (values.contains(dataItem.getIndex())) {
        selectRow(display, row);
      }
      
      row++;
    }

    return wrapDisplay(display, false);
  }

  private String getCaption(int index) {
    return (index == nullIndex) ? NULL_VALUE_LABEL : captions.get(index);
  }
}
