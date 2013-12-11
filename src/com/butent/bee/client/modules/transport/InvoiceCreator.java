package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.VIEW_CARGO_INVOICE_INCOMES;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;

class InvoiceCreator extends Button implements ClickHandler {

  private final Filter filter;

  public InvoiceCreator(Filter filter) {
    super(Localized.getConstants().createInvoice());
    addClickHandler(this);

    Assert.notNull(filter);
    this.filter = filter;
  }

  @Override
  public void onClick(ClickEvent event) {
    final Filter flt = Filter.and(filter, Filter.isEmpty(TradeConstants.COL_SALE));

    Queries.getRowCount(VIEW_CARGO_INVOICE_INCOMES, flt, new IntCallback() {
      @Override
      public void onSuccess(Integer result) {
        if (BeeUtils.isPositive(result)) {
          GridPanel grid = new GridPanel(VIEW_CARGO_INVOICE_INCOMES, GridOptions.forFilter(flt));

          StyleUtils.setSize(grid, 800, 600);

          DialogBox dialog = DialogBox.create(null);
          dialog.setWidget(grid);
          dialog.setAnimationEnabled(true);
          dialog.setHideOnEscape(true);
          dialog.center();
        } else {
          BeeKeeper.getScreen().notifyWarning(Localized.getConstants().noData());
        }
      }
    });
  }
}