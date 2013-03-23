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
      
      List<Item> items = data.getList();
      int selectedCounter = 0;
      
      for (Item item : items) {
        Element itemElement = Document.get().createDivElement();
        itemElement.addClassName(STYLE_DATA_ITEM);
        itemElement.setInnerText(item.getName());
        
        if (item.isSelected()) {
          selectedContainer.appendChild(itemElement);
          selectedCounter++;
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
      unselectedSize.setText(BeeUtils.toString(items.size() - selectedCounter));
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
      selectedSize.setText(BeeUtils.toString(selectedCounter));
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
  private static final String STYLE_WRAPPER = STYLE_PREFIX + "wrapper";
  private static final String STYLE_CONTAINER = STYLE_PREFIX + "container";

  private static final String STYLE_DATA_PREFIX = STYLE_PREFIX + "data-";
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
  private static final int DATA_PANEL_WIDTH = 150;
  private static final int DATA_PANEL_HEIGHT = 400;

  private static final int COMMAND_GROUP_HEIGHT = 30;
  private static final int COMMAND_SPLITTER_HEIGHT = 0;

  private static final int CONTAINER_WIDTH_RESERVE = 100;
  private static final int CONTAINER_HEIGHT_RESERVE = 60;
  
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

    int containerWidth = DATA_PANEL_WIDTH * dataCounter + DATA_SPLITTER_WIDTH * (dataCounter - 1);
    int containerHeight = DATA_PANEL_HEIGHT + COMMAND_SPLITTER_HEIGHT + COMMAND_GROUP_HEIGHT;
    
    int wrapperWidth = Math.min(containerWidth,
        BeeKeeper.getScreen().getWidth() - CONTAINER_WIDTH_RESERVE);
    int wrapperHeight = Math.min(containerHeight,
        BeeKeeper.getScreen().getHeight() - CONTAINER_HEIGHT_RESERVE);
    
    if (wrapperWidth < 50 || wrapperHeight < COMMAND_GROUP_HEIGHT + 50) {
      logger.warning("get a real computer", BeeKeeper.getScreen().getWidth(),
          BeeKeeper.getScreen().getHeight());
      return;
    }
    
    final DialogBox dialog = DialogBox.create(Global.CONSTANTS.filter(), STYLE_DIALOG);

    Split container = new Split(DATA_SPLITTER_WIDTH);
    container.addStyleName(STYLE_CONTAINER);
    StyleUtils.setSize(container, containerWidth, containerHeight);
    
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
          cd.setSelected(false);
        }
      }
    });
    clear.addStyleName(STYLE_COMMAND_CLEAR);
    commands.add(clear);
    
    container.addSouth(commands, COMMAND_GROUP_HEIGHT, COMMAND_SPLITTER_HEIGHT);
    
    int dataIndex = 0;
    for (ChartData data : filterData) {
      if (!data.isEmpty()) {
        DataWidget dataWidget = new DataWidget(data);
        dataIndex++;

        if (dataIndex < dataCounter) {
          container.addWest(dataWidget, DATA_PANEL_WIDTH, DATA_SPLITTER_WIDTH);
        } else {
          container.add(dataWidget);
        }
      }
    }
    
    Simple wrapper = new Simple(container);
    wrapper.addStyleName(STYLE_WRAPPER);
    StyleUtils.setSize(wrapper, wrapperWidth, wrapperHeight);

    dialog.setWidget(wrapper);
    
    dialog.setHideOnEscape(true);
    dialog.setAnimationEnabled(true);
    
    dialog.center();
    filter.setFocus(true);
  }
}
