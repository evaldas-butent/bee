package com.butent.bee.client.view.search;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterValue;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ValueFilterSupplier extends AbstractFilterSupplier {
  
  private static final String STYLE_PREFIX = DEFAULT_STYLE_PREFIX + "value-";
  
  private static final Splitter valueSplitter = 
      Splitter.on(BeeConst.CHAR_COMMA).omitEmptyStrings().trimResults();
  
  private final List<String> searchBy = Lists.newArrayList();
  
  private final Editor editor;
  private Boolean emptiness = null;
  
  private String oldValue = null;
  
  public ValueFilterSupplier(String viewName, BeeColumn column, String label,
      List<String> searchColumns, String options) {

    super(viewName, column, label, options);
    
    if (BeeUtils.isEmpty(searchColumns)) {
      searchBy.add(column.getId());
    } else {
      searchBy.addAll(searchColumns);
    }
    
    this.editor = EditorFactory.createEditor(column, false);
    editor.getElement().addClassName(STYLE_PREFIX + "editor");

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
  public FilterValue getFilterValue() {
    if (!BeeUtils.isEmpty(getEditorValue())) {
      return FilterValue.of(getEditorValue());
    } else if (getEmptiness() != null) {
      return FilterValue.of(null, getEmptiness());
    } else {
      return null;
    }
  }
  
  @Override
  public String getLabel() {
    if (!BeeUtils.isEmpty(getEditorValue())) {
      return getEditorValue();
    } else if (getEmptiness() != null) {
      return getEmptinessLabel(getEmptiness());
    } else {
      return null;
    }
  }

  @Override
  public void onRequest(Element target, Scheduler.ScheduledCommand onChange) {
    setOldValue(getEditorValue());
    
    if (hasEmptiness()) {
      Flow panel = new Flow(STYLE_PREFIX + "panel");
      panel.add(editor);
      
      CustomDiv separator = new CustomDiv(STYLE_PREFIX + "separator");
      panel.add(separator);
      
      Button notEmpty = new Button(NOT_NULL_VALUE_LABEL, new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          onEmptiness(false);
        }
      });
      notEmpty.addStyleName(STYLE_PREFIX + "notEmpty");
      panel.add(notEmpty);
      
      Button empty = new Button(NULL_VALUE_LABEL, new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          onEmptiness(true);
        }
      });
      empty.addStyleName(STYLE_PREFIX + "empty");
      panel.add(empty);
      
      openDialog(target, panel, onChange);

    } else {
      openDialog(target, editor.asWidget(), onChange);
    }
    
    editor.setFocus(true);
  }
  
  @Override
  public Filter parse(FilterValue input) {
    if (input == null) {
      return null;

    } else if (input.hasValue()) {
      return buildFilter(BeeUtils.trim(input.getValue()));
    
    } else if (input.hasEmptiness()) {
      return input.getEmptyValues() ? buildIsEmpty() : buildNotEmpty();
    
    } else {
      return null;
    }
  }
  
  @Override
  public void setFilterValue(FilterValue filterValue) {
    if (filterValue == null) {
      setValue(null,  null);
    } else {
      setValue(filterValue.getValue(), filterValue.getEmptyValues());
    }
  }
  
  private Filter buildComparison(Operator operator, String value) {
    if (searchBy.size() <= 1) {
      return ColumnValueFilter.compareWithValue(getColumn(), operator, value);

    } else {
      CompoundFilter filter = Filter.or();
      for (String by : searchBy) {
        filter.add(ColumnValueFilter.compareWithValue(by, operator, new TextValue(value)));
      }
      return filter;
    }
  }

  private Filter buildFilter(String value) {
    if (BeeUtils.isEmpty(value)) {
      return null;
    }
    
    Operator operator = Operator.detectOperator(value);

    if (operator != null) {
      if (value.equals(operator.toTextString())) {
        if (operator == Operator.EQ || operator == Operator.LT || operator == Operator.LE) {
          return buildIsEmpty();
        } else {
          return buildNotEmpty();
        }

      } else {
        return buildComparison(operator,
            BeeUtils.removePrefix(value, operator.toTextString()).trim());
      }

    } else {
      operator = ValueType.isString(getColumnType()) ? Operator.CONTAINS : Operator.EQ;

      if (value.contains(BeeConst.STRING_COMMA)) {
        CompoundFilter filter = Filter.or();
        for (String s : valueSplitter.split(value)) {
          filter.add(buildComparison(operator, s));
        }
        
        if (!filter.isEmpty()) {
          return filter;
        }
      }

      return buildComparison(operator, value);
    }
  }
  
  private Filter buildIsEmpty() {
    if (searchBy.size() <= 1) {
      return Filter.isEmpty(getColumnId());

    } else {
      CompoundFilter filter = Filter.and();
      for (String by : searchBy) {
        filter.add(Filter.isEmpty(by));
      }
      return filter;
    }
  }
  
  private Filter buildNotEmpty() {
    if (searchBy.size() <= 1) {
      return Filter.notEmpty(getColumnId());

    } else {
      CompoundFilter filter = Filter.or();
      for (String by : searchBy) {
        filter.add(Filter.notEmpty(by));
      }
      return filter;
    }
  }

  private String getEditorValue() {
    return BeeUtils.trim(editor.getValue());
  }
  
  private Boolean getEmptiness() {
    return emptiness;
  }
  
  private String getOldValue() {
    return oldValue;
  }

  private void onEmptiness(Boolean value) {
    boolean changed = !BeeUtils.isEmpty(getOldValue()) || !Objects.equal(getEmptiness(), value);
    setValue(null, value);
    update(changed);
  }

  private void onSave() {
    boolean changed = !BeeUtils.equalsTrim(getOldValue(), getEditorValue()) 
        || getEmptiness() != null;
    setEmptiness(null);
    update(changed);
  }

  private void setEmptiness(Boolean emptiness) {
    this.emptiness = emptiness;
  }
  
  private void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  private void setValue(String value, Boolean emptiness) {
    editor.setValue(value);
    this.emptiness = emptiness;
  }
}
