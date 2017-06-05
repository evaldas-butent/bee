package com.butent.bee.client.modules.transport.charts;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AutocompleteProvider;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.CustomWidget;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

class FilterDataWidget extends Flow {

  private static final String STYLE_PREFIX = FilterHelper.STYLE_DATA_PREFIX;

  private static final String STYLE_DATA_PANEL = STYLE_PREFIX + "panel";
  private static final String STYLE_DATA_UNSELECTED = STYLE_PREFIX + "unselected";
  private static final String STYLE_DATA_SELECTED = STYLE_PREFIX + "selected";

  private static final String STYLE_DATA_CAPTION = STYLE_PREFIX + "caption";

  private static final String STYLE_DATA_ITEM_CONTAINER = STYLE_PREFIX + "itemContainer";
  private static final String STYLE_DATA_ITEM = STYLE_PREFIX + "item";

  private static final String STYLE_DATA_SEARCH = STYLE_PREFIX + "search";
  private static final String STYLE_DATA_CONTROLS = STYLE_PREFIX + "controls";
  private static final String STYLE_DATA_SIZE = STYLE_PREFIX + "size";
  private static final String STYLE_DATA_COMMAND_ALL = STYLE_PREFIX + "commandAll";

  private static final int MIN_SIZE_FOR_COMMAND_ALL = 2;
  private static final int MIN_SIZE_FOR_SEARCH = 1;

  private final ChartData data;

  private final Element unselectedContainer;
  private final Element selectedContainer;

  private final CustomDiv unselectedSizeWidget;

  private final CustomDiv selectedSizeWidget;
  private final Image deselectAllWidget;

  private final InputText searchBox;

  private String searchQuery;
  private int numberOfHiddenItems;

  FilterDataWidget(ChartData data) {
    super();
    this.data = data;

    addStyleName(STYLE_DATA_PANEL);

    this.unselectedContainer = Document.get().createDivElement();
    this.selectedContainer = Document.get().createDivElement();

    int itemCount = addItems(data.getOrderedItems(), data.getSelectedItems());

    CustomDiv caption = new CustomDiv(STYLE_DATA_CAPTION);
    caption.setHtml(data.getType().getCaption());
    add(caption);

    CustomWidget unselectedPanel = new CustomWidget(unselectedContainer, STYLE_DATA_UNSELECTED);
    unselectedPanel.addStyleName(STYLE_DATA_ITEM_CONTAINER);

    unselectedPanel.addClickHandler(event -> onItemClick(event, false));

    add(unselectedPanel);

    Flow unselectedControls = new Flow();
    unselectedControls.addStyleName(STYLE_DATA_UNSELECTED);
    unselectedControls.addStyleName(STYLE_DATA_CONTROLS);

    this.unselectedSizeWidget = new CustomDiv(STYLE_DATA_SIZE);
    unselectedControls.add(unselectedSizeWidget);

    add(unselectedControls);

    this.searchBox = new InputText();
    searchBox.addStyleName(STYLE_DATA_SEARCH);
    DomUtils.setSearch(searchBox);

    AutocompleteProvider.enableAutocomplete(searchBox, "tr-chart-filter-"
        + data.getType().name().toLowerCase());

    searchBox.addInputHandler(event -> doSearch(searchBox.getValue()));

    searchBox.addKeyDownHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        boolean hasModifiers = EventUtils.hasModifierKey(event.getNativeEvent());
        if (onEnterKeyDown(hasModifiers)) {
          AutocompleteProvider.retainValue(searchBox);
        }
      }
    });

    add(searchBox);

    Flow selectedControls = new Flow();
    selectedControls.addStyleName(STYLE_DATA_SELECTED);
    selectedControls.addStyleName(STYLE_DATA_CONTROLS);

    this.selectedSizeWidget = new CustomDiv(STYLE_DATA_SIZE);
    selectedControls.add(selectedSizeWidget);

    this.deselectAllWidget = new Image(Global.getImages().arrowUpDouble());
    deselectAllWidget.addStyleName(STYLE_DATA_COMMAND_ALL);
    deselectAllWidget.setTitle(Localized.dictionary().deselectAll());

    deselectAllWidget.addClickHandler(event -> doAll(true));

    selectedControls.add(deselectAllWidget);

    add(selectedControls);

    CustomWidget selectedPanel = new CustomWidget(selectedContainer, STYLE_DATA_SELECTED);
    selectedPanel.addStyleName(STYLE_DATA_ITEM_CONTAINER);

    selectedPanel.addClickHandler(event -> onItemClick(event, true));

    add(selectedPanel);

    updateVisibility(itemCount);
    refresh();
  }

  void addItem(String item, boolean selected) {
    Element itemElement = Document.get().createDivElement();
    itemElement.addClassName(STYLE_DATA_ITEM);
    itemElement.setInnerText(item);

    if (selected) {
      selectedContainer.appendChild(itemElement);
    } else {
      unselectedContainer.appendChild(itemElement);
    }
  }

  void refresh() {
    int cnt = data.getNumberOfUnselectedItems();
    if (unselectedSizeWidget != null) {
      String text;
      if (cnt <= 0) {
        text = BeeConst.STRING_EMPTY;
      } else if (getNumberOfHiddenItems() > 0) {
        text = BeeUtils.join(BeeConst.STRING_SLASH, getNumberOfVisibleUnselectedItems(), cnt);
      } else {
        text = BeeUtils.toString(cnt);
      }
      unselectedSizeWidget.setHtml(text);
    }

    cnt = data.getNumberOfSelectedItems();
    if (selectedSizeWidget != null) {
      String text = (cnt > 0) ? BeeUtils.toString(cnt) : BeeConst.STRING_EMPTY;
      selectedSizeWidget.setHtml(text);
    }

    if (deselectAllWidget != null) {
      StyleUtils.setVisible(deselectAllWidget, cnt >= MIN_SIZE_FOR_COMMAND_ALL);
    }
  }

  void reset(boolean resetData) {
    if (searchBox != null) {
      searchBox.clearValue();
      setSearchQuery(null);
      setNumberOfHiddenItems(0);
    }

    if (resetData) {
      data.deselectAll();
    }

    DomUtils.clear(unselectedContainer);
    DomUtils.clear(selectedContainer);

    int itemCount = addItems(data.getOrderedItems(), data.getSelectedItems());

    updateVisibility(itemCount);
    refresh();
  }

  private int addItems(List<String> items, Collection<String> selectedItems) {
    for (String item : items) {
      addItem(item, selectedItems.contains(item));
    }

    return items.size();
  }

  private boolean doAll(boolean wasSelected) {
    List<Element> children = DomUtils.getVisibleChildren(wasSelected
        ? selectedContainer : unselectedContainer);

    boolean updated = false;
    for (Element child : children) {
      updated |= moveItem(child, wasSelected);
    }

    if (updated) {
      refresh();
    }
    return updated;
  }

  private void doSearch(String input) {
    String oldQuery = getSearchQuery();
    String newQuery = BeeUtils.trim(input);
    setSearchQuery(newQuery);

    if (BeeUtils.same(oldQuery, newQuery) || data.getNumberOfUnselectedItems() <= 0) {
      return;
    }
    if (getNumberOfVisibleUnselectedItems() <= 0 && BeeUtils.containsSame(newQuery, oldQuery)) {
      return;
    }
    if (getNumberOfHiddenItems() <= 0 && BeeUtils.isEmpty(newQuery)) {
      return;
    }

    int hideCnt = 0;

    Element itemElement = unselectedContainer.getFirstChildElement();
    while (itemElement != null) {
      if (StyleUtils.hasClassName(itemElement, STYLE_DATA_ITEM)) {
        boolean match = matches(itemElement, newQuery);
        StyleUtils.setVisible(itemElement, match);

        if (!match) {
          hideCnt++;
        }
      }

      itemElement = itemElement.getNextSiblingElement();
    }

    if (getNumberOfHiddenItems() != hideCnt) {
      setNumberOfHiddenItems(hideCnt);
      refresh();
    }
  }

  private int getNumberOfHiddenItems() {
    return numberOfHiddenItems;
  }

  private int getNumberOfVisibleUnselectedItems() {
    return data.getNumberOfUnselectedItems() - getNumberOfHiddenItems();
  }

  private String getSearchQuery() {
    return searchQuery;
  }

  private static boolean matches(Element itemElement, String query) {
    return BeeUtils.isEmpty(query) || BeeUtils.containsSame(itemElement.getInnerText(), query);
  }

  private boolean moveItem(Element itemElement, boolean wasSelected) {
    boolean updated = StyleUtils.hasClassName(itemElement, STYLE_DATA_ITEM)
        && data.setItemSelected(itemElement.getInnerText(), !wasSelected);

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
      return true;

    } else {
      return true;
    }
  }

  private void onItemClick(ClickEvent event, boolean wasSelected) {
    if (moveItem(EventUtils.getEventTargetElement(event), wasSelected)) {
      refresh();
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
}
