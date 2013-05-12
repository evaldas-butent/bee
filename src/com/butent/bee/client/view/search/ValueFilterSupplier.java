package com.butent.bee.client.view.search;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

import com.butent.bee.client.Callback;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ValueFilterSupplier extends AbstractFilterSupplier {
  
  private static final int MIN_EDITOR_WIDTH = 60;
  private static final int MAX_EDITOR_WIDTH = 200;
  
  private final Editor editor;
  private int lastWidth = BeeConst.UNDEF;
  
  private String oldValue = null;

  public ValueFilterSupplier(String viewName, final BeeColumn column, String options) {
    super(viewName, column, options);
    
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
  public String getLabel() {
    return editor.getValue();
  }
  
  @Override
  public String getValue() {
    return Strings.emptyToNull(BeeUtils.trim(editor.getValue()));
  }

  @Override
  public void onRequest(Element target, NotificationListener notificationListener,
      final Callback<Boolean> callback) {
    int width = BeeUtils.clamp(target.getOffsetWidth(), MIN_EDITOR_WIDTH, MAX_EDITOR_WIDTH);
    if (width != getLastWidth()) {
      StyleUtils.setWidth(editor.asWidget(), width);
      setLastWidth(width);
    }
    
    setOldValue(getValue());
    
    openDialog(target, editor.asWidget(), callback);
    editor.setFocus(true);
  }
  
  @Override
  public Filter parse(String value) {
    return BeeUtils.isEmpty(value) ? null : buildFilter(BeeUtils.trim(value));
  }
  
  @Override
  public boolean reset() {
    editor.clearValue();
    return super.reset();
  }

  @Override
  public void setValue(String value) {
    editor.setValue(value);
  }

  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList();
  }

  private Filter buildFilter(String value) {
    if (BeeUtils.isEmpty(value)) {
      return null;
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
    
    return filter;
  }
  
  private int getLastWidth() {
    return lastWidth;
  }

  private String getOldValue() {
    return oldValue;
  }

  private void onSave() {
    update(!Objects.equal(buildFilter(getOldValue()), buildFilter(getValue())));
  }

  private void setLastWidth(int lastWidth) {
    this.lastWidth = lastWidth;
  }

  private void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }
}
