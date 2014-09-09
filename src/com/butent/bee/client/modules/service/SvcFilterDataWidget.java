package com.butent.bee.client.modules.service;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.InputEvent;
import com.butent.bee.client.event.InputHandler;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.service.SvcCalendarFilterHelper.DataType;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AutocompleteProvider;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.CustomWidget;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SvcFilterDataWidget extends Flow implements HasSelectionHandlers<DataType> {

  private static final int MIN_SIZE_FOR_SEARCH = 5;
  private static final int MIN_SIZE_FOR_COMMAND_ALL = 2;
  private static final String STYLE_PREFIX = SvcCalendarFilterHelper.STYLE_DATA_PREFIX;
  private static final String STYLE_DATA_PANEL = STYLE_PREFIX + "panel";

  private static final String STYLE_DATA_CAPTION = STYLE_PREFIX + "caption";
  private static final String STYLE_DATA_COMMAND_ALL = STYLE_PREFIX + "commandAll";
  private static final String STYLE_DATA_CONTROLS = STYLE_PREFIX + "controls";
  private static final String STYLE_DATA_ITEM = STYLE_PREFIX + "item";
  private static final String STYLE_DATA_UNSELECTED = STYLE_PREFIX + "unselected";
  private static final String STYLE_DATA_ITEM_CONTAINER = STYLE_DATA_ITEM + "Container";
  private static final String STYLE_DATA_SIZE = STYLE_PREFIX + "size";
  private static final String STYLE_DATA_SEARCH = STYLE_PREFIX + "search";
  private static final String STYLE_DATA_SELECTED = STYLE_PREFIX + "selected";

  Map<Long, ServiceObjectWrapper> data;
  SvcCalendarFilterHelper.DataType dataType;

  private final Element unselectedContainer;
  private final Element selectedContainer;

  private final CustomDiv unselectedSizeWidget;
  private final Image selectAllWidget;

  private final CustomDiv selectedSizeWidget;
  private final Image deselectAllWidget;

  private final InputText searchBox;

  private final LocalizableConstants localizableConstants = Localized.getConstants();

  private String searchQuery;
  private int numberOfHiddenItems;

  SvcFilterDataWidget(SvcCalendarFilterHelper.DataType dataType,
      Map<Long, ServiceObjectWrapper> objects) {
    super();
    this.data = objects;
    this.dataType = dataType;

    addStyleName(STYLE_DATA_PANEL);

    this.unselectedContainer = Document.get().createDivElement();
    this.selectedContainer = Document.get().createDivElement();

    int itemCount = addItems(data.values());

    CustomDiv caption = new CustomDiv(STYLE_DATA_CAPTION);
    caption.setHtml(dataType.getCaption());
    add(caption);

    CustomWidget unselectedPanel = new CustomWidget(unselectedContainer, STYLE_DATA_UNSELECTED);
    unselectedPanel.addStyleName(STYLE_DATA_ITEM_CONTAINER);
    unselectedPanel.addClickHandler(getUnselectedPanelClickHandler());
    add(unselectedPanel);

    Flow unselectedControls = new Flow();
    unselectedControls.addStyleName(STYLE_DATA_UNSELECTED);
    unselectedControls.addStyleName(STYLE_DATA_CONTROLS);

    this.unselectedSizeWidget = new CustomDiv(STYLE_DATA_SIZE);
    unselectedControls.add(unselectedSizeWidget);

    this.selectAllWidget = new Image(Global.getImages().arrowDownDouble());
    selectAllWidget.addStyleName(STYLE_DATA_COMMAND_ALL);
    selectAllWidget.setTitle(localizableConstants.selectAll());
    selectAllWidget.addClickHandler(getSelectAllClickHandler());

    unselectedControls.add(selectAllWidget);
    add(unselectedControls);

    this.searchBox = new InputText();
    searchBox.addStyleName(STYLE_DATA_SEARCH);
    DomUtils.setSearch(searchBox);

    AutocompleteProvider.enableAutocomplete(searchBox, "svc-calendar-filter-"
        + dataType.name().toLowerCase());

    searchBox.addInputHandler(getSearchBoxInputHandler());
    searchBox.addKeyDownHandler(getSearchBoxKeyDownHandler());

    add(searchBox);

    Flow selectedControls = new Flow();
    selectedControls.addStyleName(STYLE_DATA_SELECTED);
    selectedControls.addStyleName(STYLE_DATA_CONTROLS);

    this.selectedSizeWidget = new CustomDiv(STYLE_DATA_SIZE);
    selectedControls.add(selectedSizeWidget);

    this.deselectAllWidget = new Image(Global.getImages().arrowUpDouble());
    deselectAllWidget.addStyleName(STYLE_DATA_COMMAND_ALL);
    deselectAllWidget.setTitle(localizableConstants.deselectAll());

    deselectAllWidget.addClickHandler(getDeselectAllClickHandler());

    selectedControls.add(deselectAllWidget);

    add(selectedControls);

    CustomWidget selectedPanel = new CustomWidget(selectedContainer, STYLE_DATA_SELECTED);
    selectedPanel.addStyleName(STYLE_DATA_ITEM_CONTAINER);

    selectedPanel.addClickHandler(getSelectedPanelClickHandler());
    add(selectedPanel);
    updateVisibility(itemCount);
    refresh();
  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<DataType> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  private static boolean matches(Element itemElement, String query) {
    return BeeUtils.isEmpty(query)
        ? true : BeeUtils.containsSame(itemElement.getInnerText(), query);
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

  private boolean doAll(boolean wasSelected) {
    List<Element> children =
        DomUtils.getVisibleChildren(wasSelected ? selectedContainer : unselectedContainer);

    boolean updated = false;
    for (Element child : children) {
      updated |= moveItem(child, wasSelected);
    }

    if (updated) {
      refresh();
      fireSelection();
    }

    return updated;
  }

  private void doSearch(String input) {
    String oldQuery = getSearchQuery();
    String newQuery = BeeUtils.trim(input);
    setSearchQuery(newQuery);

    if (BeeUtils.same(oldQuery, newQuery) || getDataNumberOfEnabledUnselectedItems() <= 0) {
      return;
    }

    if (getNumberOfVisibleUnselectedItems() <= 0 && BeeUtils.containsSame(newQuery, oldQuery)) {
      return;
    }

    if (getNumberOfHiddenItems() <= 0 && BeeUtils.isEmpty(newQuery)) {
      return;
    }

    int hideCount = 0;

    for (Element itemElement = unselectedContainer.getFirstChildElement(); itemElement != null;
        itemElement = itemElement.getNextSiblingElement()) {
      if (StyleUtils.hasClassName(itemElement, STYLE_DATA_ITEM)) {
        boolean match = matches(itemElement, newQuery);
        StyleUtils.setVisible(itemElement, match);

        if (!match) {
          hideCount++;
        }
      }
    }

    if (getNumberOfHiddenItems() != hideCount) {
      setNumberOfHiddenItems(hideCount);
      refresh();
    }
  }

  private void fireSelection() {
    SelectionEvent.fire(this, dataType);
  }

  private ServiceObjectWrapper getDataByIndex(int index) {
    Collection<ServiceObjectWrapper> objects = data.values();
    int pos = 0;

    for (ServiceObjectWrapper object : objects) {
      if (pos == index) {
        return object;
      }
    }

    return null;
  }

  private int getDataNumberOfDisabledItems() {
    int result = 0;

    for (ServiceObjectWrapper obj : data.values()) {
      if (!obj.isEnabled(dataType)) {
        result++;
      }
    }

    return result;
  }

  private int getDataNumberOfEnabledUnselectedItems() {
    return getDataSize() - getDataNumberOfSelectedItems() - getDataNumberOfDisabledItems();
  }

  private int getNumberOfHiddenItems() {
    return numberOfHiddenItems;
  }

  private int getDataNumberOfSelectedItems() {
    int result = 0;

    for (ServiceObjectWrapper obj : data.values()) {
      if (!obj.isSelected(dataType)) {
        result++;
      }
    }

    return result;
  }

  private int getDataSize() {
    return data.values().size();
  }

  private ClickHandler getDeselectAllClickHandler() {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        SvcFilterDataWidget.this.doAll(false);
      }
    };
  }

  private int getNumberOfVisibleUnselectedItems() {
    return getDataNumberOfEnabledUnselectedItems() - getNumberOfHiddenItems();
  }

  private InputHandler getSearchBoxInputHandler() {
    return new InputHandler() {

      @Override
      public void onInput(InputEvent event) {
        SvcFilterDataWidget.this.doSearch(SvcFilterDataWidget.this.searchBox.getValue());
      }
    };
  }

  private KeyDownHandler getSearchBoxKeyDownHandler() {
    return new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          boolean hasModifiers = EventUtils.hasModifierKey(event.getNativeEvent());
          if (SvcFilterDataWidget.this.onEnterKeyDown(hasModifiers)) {
            AutocompleteProvider.retainValue(searchBox);
          }
        }
      }
    };
  }

  private String getSearchQuery() {
    return searchQuery;
  }

  private ClickHandler getSelectAllClickHandler() {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        boolean updated = SvcFilterDataWidget.this.doAll(false);
        if (updated && !BeeUtils.isEmpty(getSearchQuery())) {
          AutocompleteProvider.retainValue(searchBox);
        }
      }
    };
  }

  private ClickHandler getSelectedPanelClickHandler() {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        SvcFilterDataWidget.this.onItemClick(event, true);
      }
    };
  }

  private ClickHandler getUnselectedPanelClickHandler() {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        SvcFilterDataWidget.this.onItemClick(event, false);
      }
    };
  }

  private boolean moveItem(Element itemElement, boolean wasSelected) {
    ServiceObjectWrapper object = getDataByIndex(DomUtils.getDataIndexInt(itemElement));
    boolean updated = StyleUtils.hasClassName(itemElement, STYLE_DATA_ITEM)
        && object != null;
    if (object != null) {
      object.setSelected(dataType, !wasSelected);
    }

    if (updated) {
      if (wasSelected) {
        selectedContainer.removeChild(itemElement);

        if (!matches(itemElement, getSearchQuery())) {
          StyleUtils.setVisible(itemElement, false);
          setNumberOfHiddenItems(getNumberOfHiddenItems() + 1);
        }

        unselectedContainer.appendChild(itemElement);
      } else {
        unselectedContainer.removeChild(itemElement);
        selectedContainer.appendChild(itemElement);
      }
    }

    return updated;
  }

  private boolean onEnterKeyDown(boolean hasModifiers) {
    if (BeeUtils.isEmpty(getSearchQuery()) || getNumberOfVisibleUnselectedItems() <= 0) {
      return false;
    } else if (hasModifiers) {
      return doAll(false);
    } else if (moveItem(DomUtils.getFirstVisibleChild(unselectedContainer), false)) {
      refresh();
      fireSelection();
      return true;
    }

    return true;
  }

  private void onItemClick(ClickEvent event, boolean wasSelected) {
    if (moveItem(EventUtils.getEventTargetElement(event), wasSelected)) {
      refresh();
      fireSelection();
    }
  }

  private void refresh() {
    int count = getDataNumberOfEnabledUnselectedItems();
    if (unselectedSizeWidget != null) {
      String text;
      if (count <= 0) {
        text = BeeConst.STRING_EMPTY;
      } else if (getNumberOfHiddenItems() > 0) {
        text = BeeUtils.join(BeeConst.STRING_SLASH, getNumberOfVisibleUnselectedItems(), count);
      } else {
        text = BeeUtils.toString(count);
      }

      unselectedSizeWidget.setHtml(text);
    }

    if (selectAllWidget != null) {
      StyleUtils.setVisible(selectAllWidget,
          getDataNumberOfEnabledUnselectedItems() >= MIN_SIZE_FOR_COMMAND_ALL);
    }

    count = getDataNumberOfSelectedItems();

    if (selectedSizeWidget != null) {
      String text = (count > 0) ? BeeUtils.toString(count) : BeeConst.STRING_EMPTY;
      selectedSizeWidget.setHtml(text);
    }

    if (deselectAllWidget != null) {
      StyleUtils.setVisible(deselectAllWidget, count >= MIN_SIZE_FOR_COMMAND_ALL);
    }
  }

  private void setNumberOfHiddenItems(int numberOfHiddenItems) {
    this.numberOfHiddenItems = numberOfHiddenItems;
  }

  private void setSearchQuery(String searchQuery) {
    this.searchQuery = searchQuery;
  }

  private void updateVisibility(int itemCount) {
    StyleUtils.setVisible(searchBox, itemCount >= MIN_SIZE_FOR_SEARCH);
  }

  void addItem(ServiceObjectWrapper item, int index) {
    Element itemElement = Document.get().createDivElement();
    itemElement.addClassName(STYLE_DATA_ITEM);
    itemElement.setInnerText(item.getFilterListName(dataType));
    DomUtils.setDataIndex(itemElement, index);

    if (item.isSelected(dataType)) {
      selectedContainer.appendChild(itemElement);
    } else {
      if (!matches(itemElement, getSearchQuery())) {
        StyleUtils.setVisible(itemElement, false);
        setNumberOfHiddenItems(getNumberOfHiddenItems() + 1);
      }
      unselectedContainer.appendChild(itemElement);
    }
  }
}
