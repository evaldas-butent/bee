package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.InputEvent;
import com.butent.bee.client.event.InputHandler;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.modules.transport.charts.ChartData.Item;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.CustomWidget;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

class FilterHelper {

  abstract static class DialogCallback extends Callback<DialogBox> {
  }

  private static class DataWidget extends Flow {

    private static final int MIN_SIZE_FOR_COMMAND_ALL = 2;
    private static final int MIN_SIZE_FOR_SEARCH = 5;

    private final ChartData data;

    private final Element unselectedContainer;
    private final Element selectedContainer;

    private final CustomDiv unselectedSizeWidget;
    private final BeeImage selectAllWidget;

    private final CustomDiv selectedSizeWidget;
    private final BeeImage deselectAllWidget;

    private final InputText searchBox;

    private String searchQuery = null;
    private int numberOfHiddenItems = 0;

    private DataWidget(ChartData data) {
      super();
      this.data = data;

      addStyleName(STYLE_DATA_PANEL);

      this.unselectedContainer = Document.get().createDivElement();
      this.selectedContainer = Document.get().createDivElement();

      int itemCount = addItems(data.getItems());

      CustomDiv caption = new CustomDiv(STYLE_DATA_CAPTION);
      caption.setText(data.getType().getCaption());
      add(caption);

      CustomWidget unselectedPanel = new CustomWidget(unselectedContainer, STYLE_DATA_UNSELECTED);
      unselectedPanel.addStyleName(STYLE_DATA_ITEM_CONTAINER);

      unselectedPanel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          DataWidget.this.onItemClick(event, false);
        }
      });

      add(unselectedPanel);

      Flow unselectedControls = new Flow();
      unselectedControls.addStyleName(STYLE_DATA_UNSELECTED);
      unselectedControls.addStyleName(STYLE_DATA_CONTROLS);

      this.unselectedSizeWidget = new CustomDiv(STYLE_DATA_SIZE);
      unselectedControls.add(unselectedSizeWidget);

      if (itemCount >= MIN_SIZE_FOR_COMMAND_ALL) {
        this.selectAllWidget = new BeeImage(Global.getImages().arrowDownDouble());
        selectAllWidget.addStyleName(STYLE_DATA_COMMAND_ALL);
        selectAllWidget.setTitle(Global.CONSTANTS.selectAll());

        selectAllWidget.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            DataWidget.this.doAll(false);
          }
        });

        unselectedControls.add(selectAllWidget);
      } else {
        this.selectAllWidget = null;
      }

      add(unselectedControls);

      if (itemCount >= MIN_SIZE_FOR_SEARCH) {
        this.searchBox = new InputText();
        searchBox.addStyleName(STYLE_DATA_SEARCH);
        DomUtils.setInputType(searchBox, DomUtils.TYPE_SEARCH);

        searchBox.addInputHandler(new InputHandler() {
          @Override
          public void onInput(InputEvent event) {
            DataWidget.this.doSearch(DataWidget.this.searchBox.getValue());
          }
        });

        searchBox.addKeyDownHandler(new KeyDownHandler() {
          @Override
          public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
              DataWidget.this.onEnterKeyDown(EventUtils.hasModifierKey(event.getNativeEvent()));
            }
          }
        });

        add(searchBox);
      } else {
        this.searchBox = null;
      }

      Flow selectedControls = new Flow();
      selectedControls.addStyleName(STYLE_DATA_SELECTED);
      selectedControls.addStyleName(STYLE_DATA_CONTROLS);

      this.selectedSizeWidget = new CustomDiv(STYLE_DATA_SIZE);
      selectedControls.add(selectedSizeWidget);

      if (itemCount >= MIN_SIZE_FOR_COMMAND_ALL) {
        this.deselectAllWidget = new BeeImage(Global.getImages().arrowUpDouble());
        deselectAllWidget.addStyleName(STYLE_DATA_COMMAND_ALL);
        deselectAllWidget.setTitle(Global.CONSTANTS.deselectAll());

        deselectAllWidget.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            DataWidget.this.doAll(true);
          }
        });

        selectedControls.add(deselectAllWidget);
      } else {
        this.deselectAllWidget = null;
      }

      add(selectedControls);

      CustomWidget selectedPanel = new CustomWidget(selectedContainer, STYLE_DATA_SELECTED);
      selectedPanel.addStyleName(STYLE_DATA_ITEM_CONTAINER);

      selectedPanel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          DataWidget.this.onItemClick(event, true);
        }
      });

      add(selectedPanel);

      refresh();
    }
    
    private int addItems(List<Item> items) {
      int count = 0;

      for (int i = 0; i < items.size(); i++) {
        Item item = items.get(i);
        if (!item.isEnabled()) {
          continue;
        }
        
        Element itemElement = Document.get().createDivElement();
        itemElement.addClassName(STYLE_DATA_ITEM);
        itemElement.setInnerText(item.getName());
        DomUtils.setDataIndex(itemElement, i);

        if (item.isSelected()) {
          selectedContainer.appendChild(itemElement);
        } else {
          unselectedContainer.appendChild(itemElement);
        }
        count++;
      }
      
      return count;
    }

    private void doAll(boolean wasSelected) {
      List<Element> children = DomUtils.getVisibleChildren(wasSelected
          ? selectedContainer : unselectedContainer);

      boolean updated = false;
      for (Element child : children) {
        updated |= moveItem(child, wasSelected);
      }

      if (updated) {
        refresh();
      }
    }

    private void doSearch(String input) {
      String oldQuery = getSearchQuery();
      String newQuery = BeeUtils.trim(input);
      setSearchQuery(newQuery);
      
      if (BeeUtils.same(oldQuery, newQuery) || data.getNumberOfEnabledUnselectedItems() <= 0) {
        return;
      }
      if (getNumberOfVisibleUnselectedItems() <= 0 && BeeUtils.containsSame(newQuery, oldQuery)) {
        return;
      }
      if (getNumberOfHiddenItems() <= 0 && BeeUtils.isEmpty(newQuery)) {
        return;
      }
      
      int hideCnt = 0;
      
      for (Element itemElement = unselectedContainer.getFirstChildElement(); itemElement != null;
          itemElement = itemElement.getNextSiblingElement()) {
        if (StyleUtils.hasClassName(itemElement, STYLE_DATA_ITEM)) {
          boolean match = match(itemElement, newQuery);
          StyleUtils.setVisible(itemElement, match);
          
          if (!match) {
            hideCnt++;
          }
        }
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
      return data.getNumberOfEnabledUnselectedItems() - getNumberOfHiddenItems();
    }

    private String getSearchQuery() {
      return searchQuery;
    }
    
    private boolean match(Element itemElement, String query) {
      return BeeUtils.isEmpty(query) 
          ? true : BeeUtils.containsSame(itemElement.getInnerText(), query);
    }

    private boolean moveItem(Element itemElement, boolean wasSelected) {
      boolean updated = StyleUtils.hasClassName(itemElement, STYLE_DATA_ITEM)
          && data.setSelected(DomUtils.getDataIndex(itemElement), !wasSelected);

      if (updated) {
        if (wasSelected) {
          selectedContainer.removeChild(itemElement);
          
          if (!match(itemElement, getSearchQuery())) {
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

    private void onEnterKeyDown(boolean hasModifiers) {
      if (BeeUtils.isEmpty(getSearchQuery()) || getNumberOfVisibleUnselectedItems() <= 0) {
        return;
      }

      if (hasModifiers) {
        doAll(false);
      } else if (moveItem(DomUtils.getFirstVisibleChild(unselectedContainer), false)) {
        refresh();
      }
    }

    private void onItemClick(ClickEvent event, boolean wasSelected) {
      if (moveItem(EventUtils.getEventTargetElement(event), wasSelected)) {
        refresh();
      }
    }

    private void refresh() {
      int cnt = data.getNumberOfEnabledUnselectedItems();
      if (unselectedSizeWidget != null) {
        String text;
        if (cnt <= 0) {
          text = BeeConst.STRING_EMPTY;
        } else if (getNumberOfHiddenItems() > 0) {
          text = BeeUtils.join(BeeConst.STRING_SLASH, getNumberOfVisibleUnselectedItems(), cnt);
        } else {
          text = BeeUtils.toString(cnt);
        }
        unselectedSizeWidget.setText(text);
      }

      if (selectAllWidget != null) {
        StyleUtils.setVisible(selectAllWidget, getNumberOfVisibleUnselectedItems() > 0);
      }

      cnt = data.getNumberOfSelectedItems();
      if (selectedSizeWidget != null) {
        String text = (cnt > 0) ? BeeUtils.toString(cnt) : BeeConst.STRING_EMPTY;
        selectedSizeWidget.setText(text);
      }
      
      if (deselectAllWidget != null) {
        StyleUtils.setVisible(deselectAllWidget, cnt > 0);
      }
    }
    
    private void reset() {
      if (searchBox != null) {
        searchBox.clearValue();
        setSearchQuery(null);
        setNumberOfHiddenItems(0);
      }
      
      if (data.getNumberOfSelectedItems() > 0) {
        data.deselectAll();
      }
      
      DomUtils.clear(unselectedContainer);
      DomUtils.clear(selectedContainer);
      
      addItems(data.getItems());
      
      refresh();
    }

    private void setNumberOfHiddenItems(int numberOfHiddenItems) {
      this.numberOfHiddenItems = numberOfHiddenItems;
    }

    private void setSearchQuery(String searchQuery) {
      this.searchQuery = searchQuery;
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(FilterHelper.class);

  private static final String STYLE_PREFIX = "bee-tr-chart-filter-";

  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";
  private static final String STYLE_CONTENT = STYLE_PREFIX + "content";

  private static final String STYLE_DATA_PREFIX = STYLE_PREFIX + "data-";
  private static final String STYLE_DATA_WRAPPER = STYLE_DATA_PREFIX + "wrapper";
  private static final String STYLE_DATA_CONTAINER = STYLE_DATA_PREFIX + "container";
  private static final String STYLE_DATA_PANEL = STYLE_DATA_PREFIX + "panel";
  private static final String STYLE_DATA_UNSELECTED = STYLE_DATA_PREFIX + "unselected";
  private static final String STYLE_DATA_SELECTED = STYLE_DATA_PREFIX + "selected";

  private static final String STYLE_DATA_CAPTION = STYLE_DATA_PREFIX + "caption";

  private static final String STYLE_DATA_ITEM_CONTAINER = STYLE_DATA_PREFIX + "itemContainer";
  private static final String STYLE_DATA_ITEM = STYLE_DATA_PREFIX + "item";

  private static final String STYLE_DATA_SEARCH = STYLE_DATA_PREFIX + "search";
  private static final String STYLE_DATA_CONTROLS = STYLE_DATA_PREFIX + "controls";
  private static final String STYLE_DATA_SIZE = STYLE_DATA_PREFIX + "size";
  private static final String STYLE_DATA_COMMAND_ALL = STYLE_DATA_PREFIX + "commandAll";

  private static final String STYLE_COMMAND_GROUP = STYLE_PREFIX + "commandGroup";
  private static final String STYLE_COMMAND_CLEAR = STYLE_PREFIX + "commandClear";
  private static final String STYLE_COMMAND_FILTER = STYLE_PREFIX + "commandFilter";

  private static final int DATA_SPLITTER_WIDTH = 3;
  private static final int DATA_PANEL_MIN_WIDTH = 100;
  private static final int DATA_PANEL_MAX_WIDTH = 250;
  private static final int DATA_PANEL_MIN_HEIGHT = 200;
  private static final int DATA_PANEL_MAX_HEIGHT = 400;

  private static final int COMMAND_GROUP_HEIGHT = 32;

  private static final double DIALOG_MAX_WIDTH_FACTOR = 0.8;
  private static final double DIALOG_MAX_HEIGHT_FACTOR = 0.8;
  
  static List<ChartData> getSelectedData(Collection<ChartData> data) {
    List<ChartData> result = Lists.newArrayList();

    if (data != null) {
      for (ChartData input : data) {
        if (input != null && input.hasSelection()) {
          ChartData selected = new ChartData(input.getType());
          selected.getItems().addAll(input.getSelectedItems());
          
          result.add(selected);
        }
      }
    }

    return result;
  }
  
  static boolean matched(EnumMap<Filterable.FilterType, Boolean> results,
      Filterable.FilterType filterType) {
    if (results.containsKey(filterType)) {
      return BeeUtils.unbox(results.get(filterType));
    } else {
      return false;
    }
  }

  static List<ChartData> notEmptyData(Collection<ChartData> data) {
    List<ChartData> result = Lists.newArrayList();

    if (data != null) {
      for (ChartData cd : data) {
        if (cd != null && !cd.isEmpty()) {
          result.add(cd);
        }
      }
    }

    return result;
  }

  static void openDialog(List<ChartData> filterData, final DialogCallback callback) {
    boolean ok = false;
    int dataCounter = 0;

    for (ChartData data : filterData) {
      if (data.getNumberOfEnabledItems() > 1) {
        ok = true;
      }
      if (!data.isEmpty()) {
        dataCounter++;
      }
    }

    if (!ok) {
      BeeKeeper.getScreen().notifyWarning(Global.CONSTANTS.tooLittleData());
      return;
    }

    int dialogMaxWidth = BeeUtils.round(BeeKeeper.getScreen().getWidth()
        * DIALOG_MAX_WIDTH_FACTOR);
    int dialogMaxHeight = BeeUtils.round(BeeKeeper.getScreen().getHeight()
        * DIALOG_MAX_HEIGHT_FACTOR);

    int dataPanelWidth = (dialogMaxWidth - DATA_SPLITTER_WIDTH * (dataCounter - 1)) / dataCounter;
    int dataPanelHeight = dialogMaxHeight - DialogBox.HEADER_HEIGHT - COMMAND_GROUP_HEIGHT
        - DomUtils.getScrollBarHeight();

    if (dialogMaxWidth < DATA_PANEL_MIN_WIDTH || dataPanelHeight < DATA_PANEL_MIN_HEIGHT) {
      logger.warning("get a real computer", BeeKeeper.getScreen().getWidth(),
          BeeKeeper.getScreen().getHeight(), dataPanelWidth, dataPanelHeight);
      return;
    }

    dataPanelWidth = BeeUtils.clamp(dataPanelWidth, DATA_PANEL_MIN_WIDTH, DATA_PANEL_MAX_WIDTH);
    dataPanelHeight = BeeUtils.clamp(dataPanelHeight, DATA_PANEL_MIN_HEIGHT, DATA_PANEL_MAX_HEIGHT);

    int dataContainerWidth = dataPanelWidth * dataCounter + DATA_SPLITTER_WIDTH * (dataCounter - 1);
    int dataContainerHeight = dataPanelHeight;

    int dataWrapperWidth = Math.min(dataContainerWidth, dialogMaxWidth);
    int dataWrapperHeight = dataContainerHeight + DomUtils.getScrollBarHeight();

    int contentWidth = dataWrapperWidth;
    int contentHeight = dataWrapperHeight + COMMAND_GROUP_HEIGHT;

    final DialogBox dialog = DialogBox.create(Global.CONSTANTS.filter(), STYLE_DIALOG);

    final Split dataContainer = new Split(DATA_SPLITTER_WIDTH);
    dataContainer.addStyleName(STYLE_DATA_CONTAINER);
    StyleUtils.setSize(dataContainer, dataContainerWidth, dataContainerHeight);

    int dataIndex = 0;
    for (ChartData data : filterData) {
      if (!data.isEmpty()) {
        DataWidget dataWidget = new DataWidget(data);
        dataIndex++;

        if (dataIndex < dataCounter) {
          dataContainer.addWest(dataWidget, dataPanelWidth, DATA_SPLITTER_WIDTH);
        } else {
          dataContainer.add(dataWidget);
        }
      }
    }

    Flow commands = new Flow();
    commands.addStyleName(STYLE_COMMAND_GROUP);

    BeeButton filter = new BeeButton(Global.CONSTANTS.doFilter(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        callback.onSuccess(dialog);
      }
    });
    filter.addStyleName(STYLE_COMMAND_FILTER);
    commands.add(filter);

    BeeButton clear = new BeeButton(Global.CONSTANTS.clear(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        for (Widget widget : dataContainer) {
          if (widget instanceof DataWidget) {
            ((DataWidget) widget).reset();
          }
        }
      }
    });
    clear.addStyleName(STYLE_COMMAND_CLEAR);
    commands.add(clear);

    Simple dataWrapper = new Simple(dataContainer);
    dataWrapper.addStyleName(STYLE_DATA_WRAPPER);
    StyleUtils.setSize(dataWrapper, dataWrapperWidth, dataWrapperHeight);

    Flow content = new Flow();
    content.addStyleName(STYLE_CONTENT);
    StyleUtils.setSize(content, contentWidth, contentHeight);

    content.add(dataWrapper);
    content.add(commands);

    dialog.setWidget(content);

    dialog.setHideOnEscape(true);
    dialog.setAnimationEnabled(true);

    dialog.center();
    filter.setFocus(true);
  }
}
