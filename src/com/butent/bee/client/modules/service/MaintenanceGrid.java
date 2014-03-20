package com.butent.bee.client.modules.service;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.SimpleCheckBox;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class MaintenanceGrid extends AbstractGridInterceptor {

  MaintenanceGrid() {
  }
  
  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    Button button = new Button(Localized.getConstants().createInvoice());
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        buildInvoice();
      }
    });
    
    presenter.getHeader().addCommandItem(button);
  }

  @Override
  public GridInterceptor getInstance() {
    return new MaintenanceGrid();
  }
  
  private void buildInvoice() {
    final List<IsRow> rows = getInvoiceCandidates();
    if (rows.isEmpty()) {
      getGridView().notifyInfo(Localized.getConstants().noData());
      return;
    }
    
    String stylePrefix = STYLE_PREFIX + "invoice-builder-";
    
    HtmlTable table = new HtmlTable(stylePrefix + "table");
    int r = 0;
    
    int dateIndex = getDataIndex(COL_MAINTENANCE_DATE);
    int itemNameIndex = getDataIndex(ALS_ITEM_NAME);
    int quantityIndex = getDataIndex(COL_MAINTENANCE_QUANTITY);
    int priceIndex = getDataIndex(COL_MAINTENANCE_PRICE);
    int currencyNameIndex = getDataIndex(AdministrationConstants.ALS_CURRENCY_NAME);
    
    NumberFormat priceFormat = Format.getNumberFormat("0.00");
    NumberFormat amountFormat = Format.getNumberFormat("0.00");
    
    for (IsRow row : rows) {
      DateTime date = row.getDateTime(dateIndex);
      String itemName = row.getString(itemNameIndex);
      Double quantity = row.getDouble(quantityIndex);
      Double price = row.getDouble(priceIndex);
      String currencyName = row.getString(currencyNameIndex);

      int c = 0;
      
      SimpleCheckBox checkBox = new SimpleCheckBox(true);
      table.setWidgetAndStyle(r, c++, checkBox, stylePrefix + "check");
      
      Label dateLabel = new Label();
      if (date != null) {
        dateLabel.setText(date.toCompactString());
      }
      table.setWidgetAndStyle(r, c++, dateLabel, stylePrefix + "date");
      
      Label itemLabel = new Label(itemName);
      table.setWidgetAndStyle(r, c++, itemLabel, stylePrefix + "item");
      
      Label quantityLabel = new Label();
      if (quantity != null) {
        quantityLabel.setText(BeeUtils.toString(quantity));
      }
      table.setWidgetAndStyle(r, c++, quantityLabel, stylePrefix + "quantity");

      Label priceLabel = new Label();
      if (price != null) {
        priceLabel.setText(priceFormat.format(price));
      }
      table.setWidgetAndStyle(r, c++, priceLabel, stylePrefix + "price");
      
      Label currencyLabel = new Label(currencyName);
      table.setWidgetAndStyle(r, c++, currencyLabel, stylePrefix + "currency");

      Label amountLabel = new Label();
      if (quantity != null && price != null) {
        amountLabel.setText(amountFormat.format(quantity * price));
      }
      table.setWidgetAndStyle(r, c++, amountLabel, stylePrefix + "amount");
      
      r++;
    }
    
    Simple wrapper = new Simple(table);
    wrapper.addStyleName(stylePrefix + "wrapper");
    
    Flow panel = new Flow(stylePrefix + "panel");
    panel.add(wrapper);
    
    Flow commands = new Flow(stylePrefix + "commands");

    final Popup popup = new Popup(OutsideClick.CLOSE, stylePrefix + "popup");
    
    Button build = new Button(Localized.getConstants().createInvoice());
    build.addStyleName(stylePrefix + "build");
    build.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        popup.close();
      }
    });

    Button cancel = new Button(Localized.getConstants().cancel());
    cancel.addStyleName(stylePrefix + "cancel");
    cancel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        popup.close();
      }
    });
    
    commands.add(build);
    commands.add(cancel);
    
    panel.add(commands);

    popup.setHideOnEscape(true);
    popup.setAnimationEnabled(true);
    
    popup.setWidget(panel);
    popup.center();
  }
  
  private List<IsRow> getInvoiceCandidates() {
    List<IsRow> result = Lists.newArrayList();
    
    List<? extends IsRow> data = getGridView().getRowData();
    if (!BeeUtils.isEmpty(data)) {
      int index = getDataIndex(COL_MAINTENANCE_INVOICE);
      
      for (IsRow row : data) {
        if (row.isNull(index)) {
          result.add(row);
        }
      }
    }
    
    return result;
  }
}
