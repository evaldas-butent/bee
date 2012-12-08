package com.butent.bee.client.view.search;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

import com.butent.bee.client.Callback;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

public class ValueFilterSupplier extends AbstractFilterSupplier {
  
  private static final int MIN_EDITOR_WIDTH = 60;
  private static final int MAX_EDITOR_WIDTH = 200;
  
  private final Editor editor;
  private int lastWidth = BeeConst.UNDEF;

  public ValueFilterSupplier(String viewName, Filter immutableFilter, final BeeColumn column,
      String options) {
    super(viewName, immutableFilter, column, options);
    
    this.editor = EditorFactory.createEditor(column, false);

    editor.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          ValueFilterSupplier.this.onSave();
        }
      }
    });
  }

  @Override
  public String getDisplayHtml() {
    return editor.getValue();
  }

  @Override
  public void onRequest(Element target, NotificationListener notificationListener,
      final Callback<Boolean> callback) {
    int width = BeeUtils.clamp(target.getOffsetWidth(), MIN_EDITOR_WIDTH, MAX_EDITOR_WIDTH);
    if (width != getLastWidth()) {
      StyleUtils.setWidth(editor.asWidget(), width);
      setLastWidth(width);
    }
    
    openDialog(target, editor.asWidget(), callback);
    editor.setFocus(true);
  }

  @Override
  public boolean reset() {
    editor.clearValue();
    return super.reset();
  }
  
  private int getLastWidth() {
    return lastWidth;
  }

  private void onSave() {
    String value = BeeUtils.trim(editor.getValue());
    if (BeeUtils.isEmpty(value)) {
      update(null);
      return;
    }
    
    Filter filter;

    Operator operator = Operator.detectOperator(value);

    if (operator != null) {
      if (value.equals(operator.toTextString())) {
        if (operator == Operator.EQ || operator == Operator.LT || operator == Operator.LE) {
          filter = Filter.isEmpty(getColumnId());
        } else {
          filter = Filter.notEmpty(getColumnId());
        }

      } else {
        filter = ColumnValueFilter.compareWithValue(getColumn(), operator,
            BeeUtils.removePrefix(value, operator.toTextString()).trim());
      }

    } else {
      operator = ValueType.isString(getColumnType()) ? Operator.CONTAINS : Operator.EQ;
      filter = ColumnValueFilter.compareWithValue(getColumn(), operator, value);
    }
    
    update(filter);
  }

  private void setLastWidth(int lastWidth) {
    this.lastWidth = lastWidth;
  }
}
