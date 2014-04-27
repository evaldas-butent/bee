package com.butent.bee.client.modules.service;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.trade.TotalRenderer;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.ColumnInfo;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.SimpleCheckBox;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.HasCheckedness;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class MaintenanceGrid extends AbstractGridInterceptor {

  private Long mainItem;

  MaintenanceGrid() {
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    Button button = new Button(Localized.getConstants().createInvoice());
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        selectInvoiceItems();
      }
    });

    presenter.getHeader().addCommandItem(button);
  }

  @Override
  public GridInterceptor getInstance() {
    return new MaintenanceGrid();
  }

  private void buildInvoice(final List<IsRow> items) {
    Long objId = items.get(0).getLong(getDataIndex(COL_SERVICE_OBJECT));

    Queries.getRow(VIEW_OBJECTS, objId, new RowCallback() {
      @Override
      public void onSuccess(BeeRow objRow) {
        DataInfo invInfo = Data.getDataInfo(VIEW_INVOICES);
        BeeRow invRow = RowFactory.createEmptyRow(invInfo, true);

        invRow.setValue(invInfo.getColumnIndex(TradeConstants.COL_TRADE_KIND), 1);

        Long customer = Data.getLong(VIEW_OBJECTS, objRow, COL_OBJECT_CUSTOMER);
        if (DataUtils.isId(customer)) {
          invRow.setValue(invInfo.getColumnIndex(TradeConstants.COL_TRADE_CUSTOMER), customer);
          invRow.setValue(invInfo.getColumnIndex(TradeConstants.ALS_CUSTOMER_NAME),
              Data.getString(VIEW_OBJECTS, objRow, ALS_CUSTOMER_NAME));
        }

        UserData userData = BeeKeeper.getUser().getUserData();
        if (userData != null) {
          invRow.setValue(invInfo.getColumnIndex(TradeConstants.COL_TRADE_MANAGER),
              userData.getUserId());
          RelationUtils.setUserFields(invInfo, invRow, TradeConstants.COL_TRADE_MANAGER, userData);

          invRow.setValue(invInfo.getColumnIndex(TradeConstants.COL_TRADE_SUPPLIER),
              userData.getCompany());
          invRow.setValue(invInfo.getColumnIndex(TradeConstants.ALS_SUPPLIER_NAME),
              userData.getCompanyName());
        }

        Integer days = Data.getInteger(VIEW_OBJECTS, objRow,
            ClassifierConstants.COL_COMPANY_CREDIT_DAYS);
        if (BeeUtils.isPositive(days)) {
          invRow.setValue(invInfo.getColumnIndex(TradeConstants.COL_TRADE_TERM),
              TimeUtils.nextDay(TimeUtils.today(), days));
        }

        for (IsRow row : items) {
          Long currency = row.getLong(getDataIndex(AdministrationConstants.COL_CURRENCY));

          if (DataUtils.isId(currency)) {
            invRow.setValue(invInfo.getColumnIndex(AdministrationConstants.COL_CURRENCY), currency);
            invRow.setValue(invInfo.getColumnIndex(AdministrationConstants.ALS_CURRENCY_NAME),
                row.getString(getDataIndex(AdministrationConstants.ALS_CURRENCY_NAME)));
            break;
          }
        }

        FormInterceptor interceptor = new AbstractFormInterceptor() {
          @Override
          public FormInterceptor getInstance() {
            return this;
          }

          @Override
          public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
            setMainItem(BeeUtils.toLongOrNull(getActiveRow().getProperty(PROP_MAIN_ITEM)));
          }
        };

        RowFactory.createRow("NewServiceInvoice", null, invInfo, invRow, null, interceptor,
            new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                ParameterList params = ServiceKeeper.createArgs(SVC_CREATE_INVOICE_ITEMS);

                params.addQueryItem(COL_MAINTENANCE_INVOICE, result.getId());

                Long currency = Data.getLong(VIEW_INVOICES, result,
                    AdministrationConstants.COL_CURRENCY);
                if (DataUtils.isId(currency)) {
                  params.addQueryItem(AdministrationConstants.COL_CURRENCY, currency);
                }

                if (DataUtils.isId(getMainItem())) {
                  params.addQueryItem(PROP_MAIN_ITEM, getMainItem());
                }

                List<Long> ids = Lists.newArrayList();
                for (IsRow item : items) {
                  ids.add(item.getId());
                }

                params.addDataItem(VIEW_MAINTENANCE, DataUtils.buildIdList(ids));

                final long invId = result.getId();

                BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
                  @Override
                  public void onResponse(ResponseObject response) {
                    response.notify(getGridView());

                    if (!response.hasErrors()) {
                      getGridPresenter().refresh(true);
                      DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_INVOICES);

                      RowEditor.openRow(VIEW_INVOICES, invId, false, null);
                    }
                  }
                });
              }
            });
      }
    });
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

  private Long getMainItem() {
    return mainItem;
  }

  private TotalRenderer getTotalRenderer() {
    List<ColumnInfo> columnInfos = getGridView().getGrid().getColumns();
    for (ColumnInfo columnInfo : columnInfos) {
      AbstractCellRenderer renderer = columnInfo.getColumn().getOptionalRenderer();
      if (renderer instanceof TotalRenderer) {
        return (TotalRenderer) renderer;
      }
    }
    return null;
  }

  private void selectInvoiceItems() {
    final List<IsRow> rows = getInvoiceCandidates();
    if (rows.isEmpty()) {
      getGridView().notifyInfo(Localized.getConstants().noData());
      return;
    }

    String stylePrefix = STYLE_PREFIX + "invoice-builder-";

    final HtmlTable table = new HtmlTable(stylePrefix + "table");
    int r = 0;

    int dateIndex = getDataIndex(COL_MAINTENANCE_DATE);
    int itemNameIndex = getDataIndex(ALS_ITEM_NAME);
    int quantityIndex = getDataIndex(TradeConstants.COL_TRADE_ITEM_QUANTITY);
    int priceIndex = getDataIndex(TradeConstants.COL_TRADE_ITEM_PRICE);
    int currencyNameIndex = getDataIndex(AdministrationConstants.ALS_CURRENCY_NAME);

    TotalRenderer totalRenderer = getTotalRenderer();

    NumberFormat priceFormat = Format.getDefaultCurrencyFormat();
    NumberFormat amountFormat = Format.getDefaultCurrencyFormat();

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
        Double amount = (totalRenderer == null) ? quantity * price : totalRenderer.getTotal(row);
        if (amount != null) {
          amountLabel.setText(amountFormat.format(amount));
        }
      }
      table.setWidgetAndStyle(r, c++, amountLabel, stylePrefix + "amount");

      r++;
    }

    Simple wrapper = new Simple(table);
    wrapper.addStyleName(stylePrefix + "wrapper");

    Flow panel = new Flow(stylePrefix + "panel");
    panel.add(wrapper);

    Flow commands = new Flow(stylePrefix + "commands");
    
    final DialogBox dialog = DialogBox.create(Localized.getConstants().trdNewInvoice(),
        stylePrefix + "dialog");

    Button build = new Button(Localized.getConstants().createInvoice());
    build.addStyleName(stylePrefix + "build");
    build.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        List<IsRow> selectedRows = Lists.newArrayList();

        for (int i = 0; i < rows.size(); i++) {
          Widget widget = table.getWidget(i, 0);
          if (widget instanceof HasCheckedness && ((HasCheckedness) widget).isChecked()) {
            selectedRows.add(rows.get(i));
          }
        }

        if (selectedRows.isEmpty()) {
          BeeKeeper.getScreen().notifyWarning(Localized.getConstants().selectAtLeastOneRow());
        } else {
          dialog.close();
          buildInvoice(selectedRows);
        }
      }
    });

    Button cancel = new Button(Localized.getConstants().cancel());
    cancel.addStyleName(stylePrefix + "cancel");
    cancel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.close();
      }
    });

    commands.add(build);
    commands.add(cancel);

    panel.add(commands);

    dialog.setHideOnEscape(true);
    dialog.setAnimationEnabled(true);

    dialog.setWidget(panel);
    dialog.center();
  }

  private void setMainItem(Long mainItem) {
    this.mainItem = mainItem;
  }
}
