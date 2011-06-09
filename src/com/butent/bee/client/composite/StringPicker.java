package com.butent.bee.client.composite;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages a user interface component for selecting text values from a cell list.
 */

public class StringPicker extends Composite implements HasConstrainedValue<String>, Editor {

  /**
   * Manages a single cell in a string picker component.
   */

  private static class DefaultCell extends AbstractCell<String> {
    private DefaultCell() {
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
      if (!BeeUtils.isEmpty(value)) {
        sb.appendEscaped(value);
      }
    }
  }

  private final CellList<String> cellList = new CellList<String>(new DefaultCell());
  private final SingleSelectionModel<String> smodel = new SingleSelectionModel<String>();

  private String value;
  private boolean nullable = true;

  public StringPicker() {
    initWidget(cellList);
    cellList.setSelectionModel(smodel);

    smodel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      public void onSelectionChange(SelectionChangeEvent event) {
        setValue(smodel.getSelectedObject(), true);
      }
    });

    createId();
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public StringPicker asWidget() {
    return this;
  }

  public void createId() {
    DomUtils.createId(this, "string-picker");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getNormalizedValue() {
    String v = getValue();
    if (BeeUtils.isEmpty(v) && isNullable()) {
      return null;
    } else {
      return BeeUtils.trimRight(v);
    }
  }

  public int getTabIndex() {
    return cellList.getTabIndex();
  }

  public String getValue() {
    return value;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setAcceptableValues(Collection<String> values) {
    cellList.setRowData(0, new ArrayList<String>(values));
  }

  public void setAccessKey(char key) {
    cellList.setAccessKey(key);
  }

  public void setFocus(boolean focused) {
    cellList.setFocus(focused);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setNullable(boolean nullable) {
    setNullable(nullable);
  }

  public void setTabIndex(int index) {
    cellList.setTabIndex(index);
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
    if (BeeUtils.equalsTrimRight(value, getValue())) {
      return;
    }
    this.value = value;
    smodel.setSelected(value, true);
    if (fireEvents) {
      ValueChangeEvent.fire(this, value);
    }
  }

  public void startEdit(String oldValue, char charCode) {
    String v = BeeUtils.trimRight(oldValue);
    setValue(v);
  }

  public String validate() {
    return null;
  }
}
