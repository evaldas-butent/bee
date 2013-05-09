package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
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

  private final List<String> captions;
  private final int nullIndex;

  private final List<DataItem> data = Lists.newArrayList();

  public EnumFilterSupplier(String viewName, BeeColumn column, String options, String key) {
    super(viewName, column, options);

    this.captions = Captions.getCaptions(key);
    this.nullIndex = (captions == null) ? BeeConst.UNDEF : captions.size();
  }

  @Override
  public String getDisplayHtml() {
    List<String> values = Lists.newArrayList();
    for (int row : getSelectedItems()) {
      values.add(getCaption(data.get(row)));
    }
    return BeeUtils.join(BeeConst.STRING_COMMA, values);
  }

  @Override
  public void onRequest(final Element target, final NotificationListener notificationListener,
      final Callback<Boolean> callback) {

    if (captions == null) {
      callback.onFailure(NameUtils.getName(this) + ": enum captions not available");
      return;
    }

    getHistogram(new Callback<SimpleRowSet>() {
      @Override
      public void onFailure(String... reason) {
        super.onFailure(reason);
        callback.onFailure(reason);
      }

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
              notificationListener.notifySevere("Invalid value: " + ordinal);
            }
          } else {
            nullCount = row[countIndex];
          }
        }

        if (data.isEmpty()) {
          notificationListener.notifyInfo(messageAllEmpty(nullCount));
          callback.onSuccess(reset());

        } else if (data.size() == 1 && BeeUtils.isEmpty(nullCount)) {
          notificationListener.notifyInfo(messageOneValue(captions.get(data.get(0).getIndex()),
              data.get(0).getCount()));
          callback.onSuccess(reset());

        } else {
          if (!BeeUtils.isEmpty(nullCount)) {
            data.add(new DataItem(nullIndex, nullCount));
          }
          openDialog(target, createWidget(), callback);
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
  protected void doClear() {
    clearDisplay();
    super.doClear();
  }

  @Override
  protected void doCommit() {
    update(buildFilter(getSelectedItems()));
  }

  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList(SupplierAction.COMMIT, SupplierAction.CLEAR);
  }

  private Filter buildFilter(Collection<Integer> ordinals) {
    if (BeeUtils.isEmpty(ordinals)) {
      return null;
    }

    CompoundFilter compoundFilter = Filter.or();

    for (Integer ordinal : ordinals) {
      if (ordinal != null) {
        if (ordinal.equals(nullIndex)) {
          compoundFilter.add(Filter.isEmpty(getColumnId()));
        } else {
          compoundFilter.add(ComparisonFilter.isEqual(getColumnId(), new IntegerValue(ordinal)));
        }
      }
    }

    if (compoundFilter.isEmpty()) {
      return null;
    } else if (compoundFilter.size() == 1) {
      return compoundFilter.getSubFilters().get(0);
    } else {
      return compoundFilter;
    }
  }
  
  private Widget createWidget() {
    HtmlTable display = createDisplay(true);
    
    int row = 0;
    for (DataItem dataItem : data) {
      int col = 0;
      display.setText(row, col++, getCaption(dataItem));
      addBinSize(display, row, col, dataItem.getCount());
      row++;
    }

    return wrapDisplay(display, false);
  }

  private String getCaption(DataItem dataItem) {
    return (dataItem.getIndex() == nullIndex)
        ? NULL_VALUE_LABEL : captions.get(dataItem.getIndex());
  }
}
