package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
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
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

class FilterHelper {

  abstract static class DialogCallback extends Callback<DialogBox> {
  }

  private static class DataWidget extends Flow {

    private final ChartData data;

    private DataWidget(ChartData data) {
      super();
      this.data = data;

      addStyleName(STYLE_DATA_PANEL);

      Element unselectedContainer = Document.get().createDivElement();
      Element selectedContainer = Document.get().createDivElement();

      List<Item> items = data.getItems();

      for (int i = 0; i < items.size(); i++) {
        Element itemElement = Document.get().createDivElement();
        itemElement.addClassName(STYLE_DATA_ITEM);
        itemElement.setInnerText(items.get(i).getName());
        DomUtils.setDataIndex(itemElement, i);

        if (items.get(i).isSelected()) {
          selectedContainer.appendChild(itemElement);
        } else {
          unselectedContainer.appendChild(itemElement);
        }
      }

      CustomDiv caption = new CustomDiv(STYLE_DATA_CAPTION);
      caption.setText(data.getType().getCaption());
      add(caption);

      CustomWidget unselectedPanel = new CustomWidget(unselectedContainer, STYLE_DATA_UNSELECTED);
      unselectedPanel.addStyleName(STYLE_DATA_ITEM_CONTAINER);
      add(unselectedPanel);

      Flow unselectedControls = new Flow();
      unselectedControls.addStyleName(STYLE_DATA_UNSELECTED);
      unselectedControls.addStyleName(STYLE_DATA_CONTROLS);

      CustomDiv unselectedSize = new CustomDiv(STYLE_DATA_SIZE);
      unselectedSize.setText(BeeUtils.toString(data.getNumberOfUnselectedItems()));
      unselectedControls.add(unselectedSize);

      BeeImage selectAll = new BeeImage(Global.getImages().arrowDownDouble());
      selectAll.addStyleName(STYLE_DATA_COMMAND_ALL);
      selectAll.setTitle(Global.CONSTANTS.selectAll());
      unselectedControls.add(selectAll);

      add(unselectedControls);

      InputText search = new InputText();
      search.addStyleName(STYLE_DATA_SEARCH);
      DomUtils.setInputType(search, DomUtils.TYPE_SEARCH);
      add(search);

      Flow selectedControls = new Flow();
      selectedControls.addStyleName(STYLE_DATA_SELECTED);
      selectedControls.addStyleName(STYLE_DATA_CONTROLS);

      CustomDiv selectedSize = new CustomDiv(STYLE_DATA_SIZE);
      selectedSize.setText(BeeUtils.toString(data.getNumberOfSelectedItems()));
      selectedControls.add(selectedSize);

      BeeImage deselectAll = new BeeImage(Global.getImages().arrowUpDouble());
      deselectAll.addStyleName(STYLE_DATA_COMMAND_ALL);
      deselectAll.setTitle(Global.CONSTANTS.deselectAll());
      selectedControls.add(deselectAll);

      add(selectedControls);

      CustomWidget selectedPanel = new CustomWidget(selectedContainer, STYLE_DATA_SELECTED);
      selectedPanel.addStyleName(STYLE_DATA_ITEM_CONTAINER);
      add(selectedPanel);
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

  static void openDialog(final List<ChartData> filterData, final DialogCallback callback) {
    boolean ok = false;
    int dataCounter = 0;

    for (ChartData data : filterData) {
      if (data.size() > 1) {
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

    Split dataContainer = new Split(DATA_SPLITTER_WIDTH);
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
        for (ChartData cd : filterData) {
          cd.deselectAll();
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
