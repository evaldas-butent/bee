package com.butent.bee.client.modules.service;

import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.service.SvcCalendarFilterHelper.DataType;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.CustomWidget;

import java.util.Collection;

public class SvcFilterDataWidget extends Flow implements HasSelectionHandlers<DataType> {

  private static final String STYLE_PREFIX = SvcCalendarFilterHelper.STYLE_DATA_PREFIX;
  private static final String STYLE_DATA_PANEL = STYLE_PREFIX + "panel";

  private static final String STYLE_DATA_CAPTION = STYLE_PREFIX + "caption";
  private static final String STYLE_DATA_ITEM = STYLE_PREFIX + "item";
  private static final String STYLE_DATA_UNSELECTED = STYLE_PREFIX + "unselected";
  private static final String STYLE_DATA_ITEM_CONTAINER = STYLE_DATA_ITEM + "Container";

  Multimap<Long, ServiceObjectWrapper> data;
  SvcCalendarFilterHelper.DataType dataType;

  private final Element unselectedContainer;
  private final Element selectedContainer;

  SvcFilterDataWidget(SvcCalendarFilterHelper.DataType dataType,
      Multimap<Long, ServiceObjectWrapper> objects) {
    super();
    this.data = objects;
    this.dataType = dataType;

    addStyleName(STYLE_DATA_PANEL);

    this.unselectedContainer = Document.get().createDivElement();
    this.selectedContainer = Document.get().createDivElement();

    /* int itemCount = */addItems(data.values());

    CustomDiv caption = new CustomDiv(STYLE_DATA_CAPTION);
    caption.setHtml(dataType.getCaption());
    add(caption);

    CustomWidget unselectedPanel = new CustomWidget(unselectedContainer, STYLE_DATA_UNSELECTED);
    unselectedPanel.addStyleName(STYLE_DATA_ITEM_CONTAINER);
    add(unselectedPanel);

    // TODO:

  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<DataType> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  void addItem(ServiceObjectWrapper item, int index) {
    Element itemElement = Document.get().createDivElement();
    itemElement.addClassName(STYLE_DATA_ITEM);
    itemElement.setInnerText(item.getFilterListName(dataType));
    DomUtils.setDataIndex(itemElement, index);

    if (item.isSelected(dataType)) {
      selectedContainer.appendChild(itemElement);
    } else {
      // TODO : maching case
      unselectedContainer.appendChild(itemElement);
    }
  }

  private int addItems(Collection<ServiceObjectWrapper> items) {
    int count = 0;
    int index = 0;

    for (ServiceObjectWrapper item : items) {
      if (item.isEnabled(dataType)) {
        addItem(item, index);
        count++;
      }
      index++;
    }
    return count;
  }

}
