package com.butent.bee.client.modules.service;

import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.CloseEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

final class SvcCalendarFilterHelper {

  interface DialogCallback {
    void onClear();

    void onFilter();

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

  enum DataType implements HasCaption {
    CATEGORY(localizedConstants.category()),
    ADDRESS(localizedConstants.address()),
    CUSTOMER(localizedConstants.customer()),
    CONTRACTOR(localizedConstants.svcContractor());

    private final String caption;

    private DataType(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }

  }

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

    // int contentWidth = dataWrapperWidth;
    // int contentHeight = dataWrapperHeigh + COMAND_GROUP_HEIGHT;

    filterDialog.addCloseHandler(getFilterDialogCloseHandler());

    filterDialog.setHideOnEscape(true);
    filterDialog.setAnimationEnabled(true);

    filterContent.addStyleName(STYLE_CONTENT);
    // StyleUtils.setSize(filterContent, contentMaxWidth, contentMaxHeight);
    filterDialog.add(filterContent);

    dataContainer.addStyleName(STYLE_DATA_CONTAINER);
    StyleUtils.setSize(dataContainer, dataContainerWidth, dataContainerHeight);

    // SelectionHandler<DataType> selectionHandler = new SelectionHandler<DataType>() {
    //
    // @Override
    // public void onSelection(SelectionEvent<DataType> event) {
    // callback.onSelectionChange(dataContainer);
    // }
    // };

    int dataIndex = 0;
    for (DataType type : DataType.values()) {
      for (ServiceObjectWrapper obj : objects.values()) {
        obj.saveState(type);
      }
      SvcFilterDataWidget dataWidget = new SvcFilterDataWidget(type, objects);
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
    filterButton.addClickHandler(getFilterClickHandler(filterDialog, callback));
    actions.add(filterButton);

    clearButton.addStyleName(STYLE_ACTION_CLEAR);
    clearButton.addClickHandler(getClearClickHandler(dataContainer, callback));
    actions.add(clearButton);

    filterDialog.center();

  }

  private SvcCalendarFilterHelper() {
  }

  private static ClickHandler getFilterClickHandler(final DialogBox filterDialog,
      final DialogCallback callback) {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        filterDialog.close();
        callback.onFilter();
      }
    };
  }

  private static ClickHandler getClearClickHandler(
      @SuppressWarnings("unused") final Split dataContainer,
      final DialogCallback callback) {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        // for (Widget widget : dataContainer) {
        // // TODO: reset filter types
        // }

        callback.onClear();
      }
    };
  }

  private static CloseEvent.Handler getFilterDialogCloseHandler() {
    return new CloseEvent.Handler() {

      @Override
      public void onClose(CloseEvent event) {
        // if (event.userCaused()) {
        // // TODO: restore data;
        // }
      }
    };
  }
}
