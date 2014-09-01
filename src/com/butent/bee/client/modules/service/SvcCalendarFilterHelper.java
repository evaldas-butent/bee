package com.butent.bee.client.modules.service;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.event.logical.CloseEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;

final class SvcCalendarFilterHelper {

  static final String STYLE_PREFIX = "bee-svc-calendar-filter-";
  private static final LocalizableConstants localizedConstants = Localized.getConstants();
  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";
  private static final String STYLE_CONTENT = STYLE_PREFIX + "content";
  private static final String STYLE_ACTIONS_GROUP = STYLE_PREFIX + "actionsGroup";

  private static final String STYLE_ACTION_CLEAR = STYLE_PREFIX + "actionClear";
  private static final String STYLE_ACTION_FILTER = STYLE_PREFIX + "actionFilter";

  static void openDialog() {
    DialogBox filterDialog = DialogBox.create(Localized.getConstants().filter(), STYLE_DIALOG);
    Flow filterContent = new Flow();
    Flow actions = new Flow();
    Button filterButton = new Button(localizedConstants.doFilter());
    Button clearButton = new Button(localizedConstants.clear());

    int contentWidth = 800;
    int contentHeight = 600;

    filterDialog.addCloseHandler(getFilterDialogCloseHandler());

    filterDialog.setHideOnEscape(true);
    filterDialog.setAnimationEnabled(true);

    filterContent.addStyleName(STYLE_CONTENT);
    StyleUtils.setSize(filterContent, contentWidth, contentHeight);
    filterDialog.add(filterContent);

    actions.addStyleName(STYLE_ACTIONS_GROUP);
    filterContent.add(actions);

    filterButton.addStyleName(STYLE_ACTION_FILTER);
    filterButton.addClickHandler(getFilterClickHandler());
    actions.add(filterButton);

    clearButton.addStyleName(STYLE_ACTION_CLEAR);
    clearButton.addClickHandler(getClearClickHandler());
    actions.add(clearButton);

    filterDialog.center();

  }

  private SvcCalendarFilterHelper() {
  }

  private static ClickHandler getFilterClickHandler() {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        // TODO Auto-generated method stub
      }
    };
  }

  private static ClickHandler getClearClickHandler() {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        // TODO Auto-generated method stub

      }
    };
  }

  private static CloseEvent.Handler getFilterDialogCloseHandler() {
    return new CloseEvent.Handler() {

      @Override
      public void onClose(CloseEvent event) {
        // TODO Auto-generated method stub
      }
    };
  }
}
