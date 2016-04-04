package com.butent.bee.client.modules.ec;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;

import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

class EcEventHandler implements KeyPressHandler {

  private Boolean enabled;

  private boolean listPriceEnabled;
  private boolean priceEnabled;
  private boolean stockLimitEnabled;

  EcEventHandler() {
    super();
  }

  @Override
  public void onKeyPress(KeyPressEvent event) {
    if (BeeUtils.isTrue(getEnabled())) {
      if (Character.isLetter(event.getCharCode())) {
        char ch = Character.toLowerCase(event.getCharCode());

        switch (ch) {
          case 'b':
            if (isListPriceEnabled()) {
              EcKeeper.toggleListPriceVisibility();
            }
            break;

          case 'j':
          case 'k':
            if (isPriceEnabled()) {
              EcKeeper.togglePriceVisibility();
            }
            break;

          case 'l':
          case 's':
            if (isStockLimitEnabled()) {
              EcKeeper.toggleStockLimited();
            }
            break;
        }

      } else if (event.getCharCode() == BeeConst.CHAR_QUESTION) {
        HtmlTable table = new HtmlTable(EcStyles.name("keyboard", "shortcuts"));

        int row = 0;

        if (isListPriceEnabled()) {
          table.setHtml(row, 0, "B");
          table.setHtml(row, 1, Localized.dictionary().ecToggleListPrice());
          row++;
        }

        if (isPriceEnabled()) {
          table.setHtml(row, 0, "K");
          table.setHtml(row, 1, Localized.dictionary().ecTogglePrice());
          row++;
        }

        if (isStockLimitEnabled()) {
          table.setHtml(row, 0, "S");
          table.setHtml(row, 1, Localized.dictionary().ecToggleStockLimit());
        }

        Popup popup = new Popup(OutsideClick.CLOSE);
        popup.setWidget(table);

        popup.setAnimationEnabled(true);
        popup.setHideOnEscape(true);
        popup.showRelativeTo(EventUtils.getEventTargetElement(event));
      }
    }
  }

  Boolean getEnabled() {
    return enabled;
  }

  boolean isListPriceEnabled() {
    return listPriceEnabled;
  }

  boolean isPriceEnabled() {
    return priceEnabled;
  }

  boolean isStockLimitEnabled() {
    return stockLimitEnabled;
  }

  void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  void setListPriceEnabled(boolean listPriceEnabled) {
    this.listPriceEnabled = listPriceEnabled;
  }

  void setPriceEnabled(boolean priceEnabled) {
    this.priceEnabled = priceEnabled;
  }

  void setStockLimitEnabled(boolean stockLimitEnabled) {
    this.stockLimitEnabled = stockLimitEnabled;
  }
}
