package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

public class EnumFilterSupplier extends AbstractFilterSupplier {

  private static class DataItem {
    private final int index;
    private final String count;

    private boolean selected = false;

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

    private boolean isSelected() {
      return selected;
    }

    private void setSelected(boolean selected) {
      this.selected = selected;
    }
  }

  private final List<String> captions;
  private final int nullIndex;

  private final List<DataItem> data = Lists.newArrayList();

  private final BeeListBox listBox = new BeeListBox(true);

  public EnumFilterSupplier(String viewName, BeeColumn column, String options, String key) {
    super(viewName, column, options);

    this.captions = Global.getCaptions(key);
    this.nullIndex = (captions == null) ? BeeConst.UNDEF : captions.size();
  }

  @Override
  public String getDisplayHtml() {
    List<String> values = Lists.newArrayList();

    for (DataItem dataItem : data) {
      if (dataItem.isSelected()) {
        values.add(getCaption(dataItem));
      }
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
  public boolean reset() {
    data.clear();
    return super.reset();
  }

  @Override
  protected void doFilterCommand() {
    CompoundFilter compoundFilter = Filter.or();

    for (int i = 0; i < listBox.getItemCount(); i++) {
      boolean selected = listBox.isItemSelected(i);
      data.get(i).setSelected(selected);

      if (selected) {
        int value = data.get(i).getIndex();
        if (value == nullIndex) {
          compoundFilter.add(Filter.isEmpty(getColumnId()));
        } else {
          compoundFilter.add(ComparisonFilter.isEqual(getColumnId(), new IntegerValue(value)));
        }
      }
    }

    Filter newFilter;
    if (compoundFilter.isEmpty()) {
      newFilter = null;
    } else if (compoundFilter.size() == 1) {
      newFilter = compoundFilter.getSubFilters().get(0);
    } else {
      newFilter = compoundFilter;
    }

    update(newFilter);
  }

  @Override
  protected void doResetCommand() {
    for (DataItem dataItem : data) {
      dataItem.setSelected(false);
    }

    for (int i = 0; i < listBox.getItemCount(); i++) {
      listBox.setItemSelected(i, false);
    }
  }
  
  @Override
  protected String getStylePrefix() {
    return super.getStylePrefix() + "Enum-";
  }

  private Widget createWidget() {
    Vertical panel = new Vertical();
    panel.addStyleName(getStylePrefix() + "panel");

    listBox.clear();
    for (DataItem dataItem : data) {
      String item = BeeUtils.joinWords(getCaption(dataItem), dataItem.getCount());
      listBox.addItem(item);
    }

    for (int i = 0; i < data.size(); i++) {
      if (data.get(i).isSelected()) {
        listBox.setItemSelected(i, true);
      }
    }

    listBox.setAllVisible();
    panel.add(listBox);

    panel.add(getCommandWidgets(false));
    return panel;
  }

  private String getCaption(DataItem dataItem) {
    return (dataItem.getIndex() == nullIndex)
        ? NULL_VALUE_LABEL : captions.get(dataItem.getIndex());
  }
}
