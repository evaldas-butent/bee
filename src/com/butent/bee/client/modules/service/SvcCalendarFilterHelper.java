package com.butent.bee.client.modules.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.CloseEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.modules.service.ServiceConstants.ServiceFilterDataType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

final class SvcCalendarFilterHelper {

  interface DialogCallback {
    void onClear();

    void onFilter(Map<ServiceFilterDataType, List<Long>> selectedData, String label);

    void onSelectionChange(HasWidgets dataContainer);
  }

  static final String STYLE_PREFIX = "bee-svc-calendar-filter-";
  static final String STYLE_DATA_PREFIX = STYLE_PREFIX + "data-";

  private static final LocalizableConstants localizedConstants = Localized.getConstants();
  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";
  private static final String STYLE_CONTENT = STYLE_PREFIX + "content";
  private static final String STYLE_ACTIONS_GROUP = STYLE_PREFIX + "actionsGroup";

  private static final String STYLE_ACTION_CLEAR = STYLE_PREFIX + "actionClear";
  private static final String STYLE_ACTION_FILTER = STYLE_PREFIX + "actionFilter";
  private static final String STYLE_DATA_WRAPPER = STYLE_PREFIX + "data-wrapper";
  private static final String STYLE_DATA_CONTAINER = STYLE_PREFIX + "data-conatainer";

  private static final int DATA_SPLITTER_WIDTH = 3;
  private static final int DEFAULT_DATA_COUNT = 4;
  private static final double DIALOG_SIZE_MAX_FACTOR = 0.8;
  private static final int COMAND_GROUP_HEIGHT = 32;

  private static Map<Long, ServiceObjectWrapper> categoryObjects = Maps.newHashMap();
  private static Map<Long, ServiceObjectWrapper> addressObjects = Maps.newHashMap();
  private static Map<Long, ServiceObjectWrapper> customerObjects = Maps.newHashMap();
  private static Map<Long, ServiceObjectWrapper> contractorObjects = Maps.newHashMap();

  static void openDialog(final Multimap<Long, ServiceObjectWrapper> objects,
      final DialogCallback callback) {
    DialogBox filterDialog = DialogBox.create(Localized.getConstants().filter(), STYLE_DIALOG);
    Flow filterContent = new Flow();
    Flow actions = new Flow();
    Button filterButton = new Button(localizedConstants.doFilter());
    Button clearButton = new Button(localizedConstants.clear());
    final Split dataContainer = new Split(DATA_SPLITTER_WIDTH);
    Simple dataWrapper = new Simple(dataContainer);

    int dialogMaxWidth = BeeUtils.round(BeeKeeper.getScreen().getWidth()
        * DIALOG_SIZE_MAX_FACTOR);
    int dialogMaxHeight = BeeUtils.round(BeeKeeper.getScreen().getHeight()
        * DIALOG_SIZE_MAX_FACTOR);

    int dataPanelWidth =
        (dialogMaxWidth - DATA_SPLITTER_WIDTH * (DEFAULT_DATA_COUNT - 1)) / DEFAULT_DATA_COUNT;
    int dataPanelHeight =
        dialogMaxHeight - DialogBox.HEADER_HEIGHT - COMAND_GROUP_HEIGHT
            - -DomUtils.getScrollBarHeight();

    int dataContainerWidth =
        dataPanelWidth * DEFAULT_DATA_COUNT + DATA_SPLITTER_WIDTH * (DEFAULT_DATA_COUNT - 1);
    int dataContainerHeight = dataPanelHeight;

    int dataWrapperWidth = Math.min(dataContainerWidth, dialogMaxWidth);
    int dataWrapperHeigh = dataContainerHeight + DomUtils.getScrollBarHeight();

    int contentWidth = dataWrapperWidth;
    int contentHeight = dataWrapperHeigh + COMAND_GROUP_HEIGHT;



    filterDialog.setHideOnEscape(true);
    filterDialog.setAnimationEnabled(true);

    filterContent.addStyleName(STYLE_CONTENT);
    StyleUtils.setSize(filterContent, contentWidth, contentHeight);
    filterDialog.add(filterContent);

    dataContainer.addStyleName(STYLE_DATA_CONTAINER);
    StyleUtils.setSize(dataContainer, dataContainerWidth, dataContainerHeight);

    SelectionHandler<ServiceConstants.ServiceFilterDataType> selectionHandler =
        getSelectionHandler(callback, dataContainer);

    int dataIndex = 0;
    prepareObjectsByType(objects);
    for (ServiceConstants.ServiceFilterDataType type : ServiceConstants.ServiceFilterDataType
        .values()) {
      for (ServiceObjectWrapper obj : objects.values()) {
        obj.saveState(type);
      }


      Map<Long, ServiceObjectWrapper> typedObjects;

      switch (type) {
        case ADDRESS:
          typedObjects = addressObjects;
          break;
        case CATEGORY:
          typedObjects = categoryObjects;
          break;
        case CONTRACTOR:
          typedObjects = contractorObjects;
          break;
        case CUSTOMER:
          typedObjects = customerObjects;
          break;
        default:
          typedObjects = addressObjects;
          break;
      }

      SvcFilterDataWidget dataWidget = new SvcFilterDataWidget(type, typedObjects);
      dataWidget.addSelectionHandler(selectionHandler);
      dataIndex++;
      if (dataIndex < DEFAULT_DATA_COUNT) {
        dataContainer.addWest(dataWidget, dataPanelWidth, DATA_SPLITTER_WIDTH);
      } else {
        dataContainer.add(dataWidget);
      }
    }

    dataWrapper.addStyleName(STYLE_DATA_WRAPPER);
    StyleUtils.setSize(dataWrapper, dataWrapperWidth, dataWrapperHeigh);
    filterContent.add(dataWrapper);

    actions.addStyleName(STYLE_ACTIONS_GROUP);
    filterContent.add(actions);

    filterButton.addStyleName(STYLE_ACTION_FILTER);
    filterButton.addClickHandler(getFilterClickHandler(filterDialog, callback, dataContainer));
    actions.add(filterButton);

    clearButton.addStyleName(STYLE_ACTION_CLEAR);
    clearButton.addClickHandler(getClearClickHandler(dataContainer, callback));
    actions.add(clearButton);

    filterDialog.addCloseHandler(getFilterDialogCloseHandler(dataContainer));
    filterDialog.center();
    filterButton.setFocus(true);
  }

  private static ClickHandler getFilterClickHandler(final DialogBox filterDialog,
      final DialogCallback callback, final Split dataContainer) {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        filterDialog.close();
        Map<ServiceFilterDataType, List<Long>> selectedData = Maps.newHashMap();
        String filterName = BeeConst.STRING_EMPTY;
        List<String> names = Lists.newArrayList();
        for (Widget widget : dataContainer) {
          if (widget instanceof SvcFilterDataWidget) {
            SvcFilterDataWidget filter = (SvcFilterDataWidget) widget;
            selectedData.put(filter.getDataType(), filter.getSelectedDataIds());
            names.add(filter.getFilterLabel());
          }
        }

        filterName = BeeUtils.join(BeeConst.STRING_COMMA, names);
        callback.onFilter(selectedData, filterName);
      }
    };
  }

  private static ClickHandler getClearClickHandler(
      final Split dataContainer,
      final DialogCallback callback) {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        for (Widget widget : dataContainer) {
          if (widget instanceof SvcFilterDataWidget) {
            ((SvcFilterDataWidget) widget).reset(true);
          }
        }

        callback.onClear();
      }
    };
  }

  private static CloseEvent.Handler getFilterDialogCloseHandler(final Split dataContainer) {
    return new CloseEvent.Handler() {

      @Override
      public void onClose(CloseEvent event) {
        for (Widget widget : dataContainer) {
          if (widget instanceof SvcFilterDataWidget) {
            ((SvcFilterDataWidget) widget).restoreDataState();
          }
        }
      }
    };
  }

  private static SelectionHandler<ServiceFilterDataType> getSelectionHandler(
      final DialogCallback callback, final Split dataContainer) {
    return new SelectionHandler<ServiceFilterDataType>() {

      @Override
      public void onSelection(SelectionEvent<ServiceConstants.ServiceFilterDataType> event) {
        // TODO: remove related items.
        callback.onSelectionChange(dataContainer);
      }
    };
  }

  private static void prepareObjectsByType(Multimap<Long, ServiceObjectWrapper> objects) {
    categoryObjects.clear();
    addressObjects.clear();
    contractorObjects.clear();
    customerObjects.clear();

    for (ServiceObjectWrapper object : objects.values()) {

      if (DataUtils.isId(object.getCategoryId())) {
        categoryObjects.put(object.getCategoryId(), object);
      }

      if (DataUtils.isId(object.getId())) {
        addressObjects.put(object.getId(), object);
      }

      if (DataUtils.isId(object.getContractorId())) {
        contractorObjects.put(object.getContractorId(), object);
      }

      if (DataUtils.isId(object.getCustomerId())) {
        customerObjects.put(object.getCategoryId(), object);
      }
    }
  }

  private SvcCalendarFilterHelper() {
  }
}
