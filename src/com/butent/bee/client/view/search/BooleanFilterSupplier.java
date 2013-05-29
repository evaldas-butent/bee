package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class BooleanFilterSupplier extends AbstractFilterSupplier {
  
  private Boolean value = null;

  public BooleanFilterSupplier(String viewName, BeeColumn column, String label, String options) {
    super(viewName, column, label, options);
  }

  @Override
  public String getFilterLabel(String ownerLabel) {
    return BeeUtils.isEmpty(getColumnLabel()) ? super.getFilterLabel(ownerLabel) : getLabel();
  }

  @Override
  public String getLabel() {
    if (value == null) {
      return null;
    } else {
      return value ? getLabelForNotEmpty() : getLabelForEmpty();
    }
  }

  @Override
  public String getValue() {
    return BooleanValue.pack(value);
  }

  @Override
  public void onRequest(Element target, Scheduler.ScheduledCommand onChange) {
    openDialog(target, createWidget(), onChange);
  }

  @Override
  public Filter parse(String input) {
    Boolean b = BooleanValue.unpack(input);
    if (b == null) {
      return null;
    } else {
      return b ? Filter.notEmpty(getColumnId()) : Filter.isEmpty(getColumnId()); 
    }
  }

  @Override
  public void setValue(String value) {
    this.value = BooleanValue.unpack(value);
  }

  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList();
  }
  
  @Override
  protected String getStylePrefix() {
    return DEFAULT_STYLE_PREFIX + "boolean-";
  }
  
  private Widget createWidget() {
    HtmlTable container = new HtmlTable();
    container.addStyleName(getStylePrefix() + "container");

    BeeButton notEmpty = new BeeButton(getLabelForNotEmpty());
    notEmpty.addStyleName(getStylePrefix() + "notEmpty");

    notEmpty.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean changed = !BeeUtils.isTrue(value);
        value = true;
        update(changed);
      }
    });

    container.setWidget(0, 0, notEmpty);
    
    BeeButton empty = new BeeButton(getLabelForEmpty());
    empty.addStyleName(getStylePrefix() + "empty");

    empty.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean changed = !BeeUtils.isFalse(value);
        value = false;
        update(changed);
      }
    });

    container.setWidget(0, 1, empty);

    BeeButton all = new BeeButton(Localized.constants.filterAll());
    all.addStyleName(getStylePrefix() + "all");

    all.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean changed = (value != null);
        value = null;
        update(changed);
      }
    });

    container.setWidget(1, 0, all);

    BeeButton cancel = new BeeButton(Localized.constants.cancel());
    cancel.addStyleName(getStylePrefix() + "cancel");

    cancel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        closeDialog();
      }
    });

    container.setWidget(1, 1, cancel);

    return container;
  }
  
  private String getLabelForEmpty() {
    return BeeUtils.isEmpty(getColumnLabel()) 
        ? NULL_VALUE_LABEL : Localized.messages.not(getColumnLabel());
  }

  private String getLabelForNotEmpty() {
    return BeeUtils.isEmpty(getColumnLabel()) ? NOT_NULL_VALUE_LABEL : getColumnLabel();
  }
}
