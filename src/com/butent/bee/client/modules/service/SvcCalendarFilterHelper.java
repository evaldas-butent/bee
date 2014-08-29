package com.butent.bee.client.modules.service;

import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.event.logical.CloseEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.shared.i18n.Localized;

final class SvcCalendarFilterHelper {

  static final String STYLE_PREFIX = "bee-svc-chart-filter-";
  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";
  private static final String STYLE_CONTENT = STYLE_PREFIX + "content";

  static void openDialog() {
    DialogBox filterDialog = DialogBox.create(Localized.getConstants().filter(), STYLE_DIALOG);
    Flow filterContent = new Flow();
    int contentWidth = 800;
    int contentHeight = 600;

    filterDialog.addCloseHandler(new CloseEvent.Handler() {

      @Override
      public void onClose(CloseEvent event) {
        // TODO:
      }
    });

    filterDialog.setHideOnEscape(true);
    filterDialog.setAnimationEnabled(true);

    filterContent.addStyleName(STYLE_CONTENT);
    StyleUtils.setSize(filterContent, contentWidth, contentHeight);
    filterDialog.add(filterContent);

    filterDialog.center();

  }

  private SvcCalendarFilterHelper() {
  }
}
