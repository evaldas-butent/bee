package com.butent.bee.client.modules.trade;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.event.logical.DataReceivedEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class TradeItemsForReturnGrid extends AbstractGridInterceptor {

  private static final String STYLE_PREFIX = TradeKeeper.STYLE_PREFIX + "customer-return-";

  private static final String STYLE_SAVE = STYLE_PREFIX + "save";
  private static final String STYLE_POPUP = STYLE_PREFIX + "popup";
  private static final String STYLE_INPUT = STYLE_PREFIX + "input";

  private static final String PROP_SELECTED_QTY = "SelectedQty";

  private final Map<Long, Double> selection = new HashMap<>();

  TradeItemsForReturnGrid() {
  }

  Map<Long, Double> getSelection() {
    return selection;
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (presenter != null && presenter.getHeader() != null) {
      Button save = new Button(Localized.dictionary().save());
      save.addStyleName(STYLE_SAVE);
      save.setEnabled(false);

      presenter.getHeader().addCommandItem(save);
    }

    super.afterCreatePresenter(presenter);
  }

  @Override
  public void onDataReceived(DataReceivedEvent event) {
    if (!selection.isEmpty() && !BeeUtils.isEmpty(event.getRows())) {
      event.getRows().forEach(row -> row.setNonZero(PROP_SELECTED_QTY, selection.get(row.getId())));
    }

    super.onDataReceived(event);
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (DataUtils.hasId(event.getRowValue()) && event.hasSource(PROP_SELECTED_QTY)) {
      event.consume();

      IsRow row = event.getRowValue();
      char charCode = BeeUtils.toChar(event.getCharCode());

      switch (charCode) {
        case EditStartEvent.DELETE:
          update(row, null);
          break;

        case EditStartEvent.ENTER:
          boolean edit = selection.containsKey(row.getId()) || !selectAvailableQuantity(row);
          if (edit) {
            openEditor(row, event.getSourceElement(), charCode);
          }
          break;

        case BeeConst.CHAR_ASTERISK:
          selectAvailableQuantity(row);
          break;

        default:
          openEditor(row, event.getSourceElement(), charCode);
      }
    }

    super.onEditStart(event);
  }

  private void openEditor(IsRow row, Element sourceElement, char charCode) {
    InputNumber input = new InputNumber();
    input.addStyleName(STYLE_INPUT);

    input.setScale(Data.getColumnScale(VIEW_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_QUANTITY));

    if (BeeUtils.isDigit(charCode)) {
      input.setValue(BeeUtils.toString(charCode));
      input.setCursorPos(1);

    } else if (selection.containsKey(row.getId())) {
      Double value = selection.get(row.getId());

      if (BeeUtils.isPositive(value)) {
        input.setValue(value);
        input.selectAll();
      }
    }

    Popup popup = new Popup(Popup.OutsideClick.CLOSE, STYLE_POPUP);

    input.addKeyDownHandler(event -> {
      switch (event.getNativeKeyCode()) {
        case KeyCodes.KEY_ENTER:
        case KeyCodes.KEY_DOWN:
        case KeyCodes.KEY_UP:
          popup.close();
          update(row, input.getNumber());

          int key = (event.getNativeKeyCode() == KeyCodes.KEY_UP)
              ? KeyCodes.KEY_UP : KeyCodes.KEY_DOWN;
          getGridView().getGrid().handleKeyboardNavigation(key, false);
          break;
      }
    });

    StyleUtils.setSize(input, sourceElement.getOffsetWidth(), sourceElement.getOffsetHeight());

    popup.setWidget(input);

    popup.setHideOnEscape(true);

    popup.focusOnOpen(input);
    popup.addCloseHandler(event -> sourceElement.focus());

    popup.showAt(sourceElement.getAbsoluteLeft(), sourceElement.getAbsoluteTop());
  }

  private double getAvailableQuantity(IsRow row) {
    Double quantity = row.getDouble(getDataIndex(COL_TRADE_ITEM_QUANTITY));
    Double returned = row.getDouble(getDataIndex(ALS_RETURNED_QTY));

    double available = BeeUtils.unbox(quantity) - BeeUtils.unbox(returned);
    return Math.max(available, BeeConst.DOUBLE_ZERO);
  }

  private boolean selectAvailableQuantity(IsRow row) {
    double value = getAvailableQuantity(row);

    if (BeeUtils.isPositive(value)) {
      return update(row, value);
    } else {
      return false;
    }
  }

  private boolean update(IsRow row, Double value) {
    Double oldValue = selection.get(row.getId());
    Double newValue = BeeUtils.isPositive(value) ? value : null;

    if (Objects.equals(oldValue, newValue)) {
      return false;

    } else {
      if (newValue == null) {
        selection.remove(row.getId());
        row.removeProperty(PROP_SELECTED_QTY);

      } else {
        selection.put(row.getId(), newValue);
        row.setProperty(PROP_SELECTED_QTY, newValue);
      }

      getGridView().getGrid().refreshRowById(row.getId());

      HeaderView header = getHeaderView();
      if (header != null) {
        header.enableCommandByStyleName(STYLE_SAVE, !selection.isEmpty());
      }

      return true;
    }
  }
}
