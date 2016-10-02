package com.butent.bee.client.view.edit;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumable;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Manages edit start event, gets column id, type, row value and other necessary parameters.
 */

public class EditStartEvent extends GwtEvent<EditStartEvent.Handler> implements Consumable {

  @FunctionalInterface
  public interface Handler extends EventHandler {
    void onEditStart(EditStartEvent event);
  }

  public static final int CLICK = 1;
  public static final int ENTER = 2;
  public static final int DELETE = 3;

  private static final Type<Handler> TYPE = new Type<>();

  public static int getStartKey(int keyCode) {
    int startKey;

    switch (keyCode) {
      case KeyCodes.KEY_ENTER:
        startKey = ENTER;
        break;
      case KeyCodes.KEY_DELETE:
        startKey = DELETE;
        break;
      default:
        startKey = BeeConst.UNDEF;
    }

    return startKey;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public static boolean isClickOrEnter(int code) {
    return code == CLICK || isEnter(code);
  }

  public static boolean isEnter(int code) {
    return code == ENTER || code == KeyCodes.KEY_ENTER;
  }

  private final IsRow rowValue;
  private final String columnId;
  private final CellSource cellSource;

  private final Element sourceElement;
  private final int charCode;

  private final boolean readOnly;

  private boolean consumed;

  private Consumer<FormView> onFormFocus;

  public EditStartEvent(IsRow rowValue, boolean readOnly) {
    this(rowValue, null, null, null, CLICK, readOnly);
  }

  public EditStartEvent(IsRow rowValue, String columnId, CellSource cellSource,
      Element sourceElement, int charCode, boolean readOnly) {

    this.rowValue = rowValue;
    this.columnId = columnId;
    this.cellSource = cellSource;

    this.sourceElement = sourceElement;
    this.charCode = charCode;
    this.readOnly = readOnly;
  }

  @Override
  public void consume() {
    setConsumed(true);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public CellSource getCellSource() {
    return cellSource;
  }

  public int getCharCode() {
    return charCode;
  }

  public String getColumnId() {
    return columnId;
  }

  public Consumer<FormView> getOnFormFocus() {
    return onFormFocus;
  }

  public IsRow getRowValue() {
    return rowValue;
  }

  public Element getSourceElement() {
    return sourceElement;
  }

  public boolean hasAnySource(String first, String second, String... rest) {
    if (hasSource(first) || hasSource(second)) {
      return true;
    }

    if (rest != null) {
      for (String name : rest) {
        if (hasSource(name)) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean hasSource(String name) {
    return getCellSource() != null && Objects.equals(getCellSource().getName(), name);
  }

  @Override
  public boolean isConsumed() {
    return consumed;
  }

  public boolean isDelete() {
    return charCode == DELETE;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public void setConsumed(boolean consumed) {
    this.consumed = consumed;
  }

  public void setOnFormFocus(Consumer<FormView> onFormFocus) {
    this.onFormFocus = onFormFocus;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onEditStart(this);
  }
}
