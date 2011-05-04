package com.butent.bee.client.grid;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.datepicker.client.DatePicker;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.shared.AbstractDate;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasDateValue;
import com.butent.bee.shared.data.value.ValueType;

import java.util.Date;

public class DatePickerCell extends AbstractEditableCell<HasDateValue, HasDateValue> {

  private final SafeHtmlRenderer<String> renderer = SimpleSafeHtmlRenderer.getInstance();
  private final DateTimeFormat format;
  private final ValueType valueType;

  private Object lastKey;
  private Element lastParent;
  private int lastIndex;
  private int lastColumn;
  private HasDateValue lastValue;

  private final DatePicker datePicker;
  private PopupPanel panel;
  private ValueUpdater<HasDateValue> updater;

  public DatePickerCell(ValueType valueType, DateTimeFormat format) {
    super(EventUtils.EVENT_TYPE_CLICK, EventUtils.EVENT_TYPE_KEY_DOWN);
    Assert.notNull(valueType);
    this.valueType = valueType;
    this.format = format;

    this.datePicker = new DatePicker();
    this.panel = new PopupPanel(true, true) {
      @Override
      protected void onPreviewNativeEvent(NativePreviewEvent event) {
        if (Event.ONKEYUP == event.getTypeInt()) {
          if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
            panel.hide();
          }
        }
      }
    };

    panel.addCloseHandler(new CloseHandler<PopupPanel>() {
      public void onClose(CloseEvent<PopupPanel> event) {
        lastKey = null;
        lastValue = null;
        lastIndex = -1;
        lastColumn = -1;

        if (lastParent != null) {
          lastParent.focus();
        }
        lastParent = null;
      }
    });
    panel.add(datePicker);

    datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
      public void onValueChange(ValueChangeEvent<Date> event) {
        Element cellParent = lastParent;
        HasDateValue oldValue = lastValue;
        Object key = lastKey;
        int index = lastIndex;
        int column = lastColumn;

        HasDateValue date = AbstractDate.fromJava(event.getValue(), DatePickerCell.this.valueType);
        setViewData(key, date);
        setValue(new Context(index, column, key), cellParent, oldValue);
        if (updater != null) {
          updater.update(date);
        }
        panel.hide();
      }
    });
  }
  
  @Override
  public boolean handlesSelection() {
    return true;
  }

  @Override
  public boolean isEditing(Context context, Element parent, HasDateValue value) {
    return lastKey != null;
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, HasDateValue value,
      NativeEvent event, ValueUpdater<HasDateValue> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if (EventUtils.isClick(event)) {
      onEnterKeyDown(context, parent, value, event, valueUpdater);
    }
  }

  @Override
  public void render(Context context, HasDateValue value, SafeHtmlBuilder sb) {
    Object key = context.getKey();
    HasDateValue viewData = getViewData(key);
    if (viewData != null && viewData.equals(value)) {
      clearViewData(key);
      viewData = null;
    }
    
    HasDateValue date = null;
    if (viewData != null) {
      date = viewData;
    } else if (value != null) {
      date = value;
    }
    if (date == null) {
      return;
    }
    
    String s = (format == null) ? date.toString() : format.format(date.getJava());
    if (s != null) {
      sb.append(renderer.render(s));
    }
  }

  @Override
  protected void onEnterKeyDown(Context context, Element parent, HasDateValue value,
      NativeEvent event, ValueUpdater<HasDateValue> valueUpdater) {
    this.lastKey = context.getKey();
    this.lastParent = parent;
    this.lastValue = value;
    this.lastIndex = context.getIndex();
    this.lastColumn = context.getColumn();
    this.updater = valueUpdater;

    HasDateValue viewData = getViewData(lastKey);
    HasDateValue date = (viewData == null) ? lastValue : viewData;
    if (date != null) {
      Date jd = date.getJava();
      datePicker.setCurrentMonth(jd);
      datePicker.setValue(jd);
    }
    
    int z;
    if (context instanceof CellContext) {
      CellGrid grid = ((CellContext) context).getGrid();
      Assert.notNull(grid);
      z = grid.getZIndex();
    } else {
      z = StyleUtils.getZIndex(parent);
    }
    StyleUtils.setZIndex(panel, z + 1);

    panel.setPopupPositionAndShow(new PositionCallback() {
      public void setPosition(int offsetWidth, int offsetHeight) {
        panel.setPopupPosition(lastParent.getAbsoluteLeft() + 10,
            lastParent.getAbsoluteTop() + lastParent.getClientHeight());
      }
    });
  }
}
