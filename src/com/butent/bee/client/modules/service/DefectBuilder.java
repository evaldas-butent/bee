package com.butent.bee.client.modules.service;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.SimpleCheckBox;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.HasCheckedness;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

final class DefectBuilder {

  private static final String STYLE_PREFIX = ServiceKeeper.STYLE_PREFIX + "defect-builder-";

  static void start(IdentifiableWidget sourceWidget) {
    if (sourceWidget == null) {
      return;
    }

    final FormView form = UiHelper.getForm(sourceWidget.asWidget());
    if (form == null || !form.isEnabled() || !VIEW_SERVICE_OBJECTS.equals(form.getViewName())) {
      return;
    }

    final long objId = form.getActiveRowId();
    if (!DataUtils.isId(objId)) {
      return;
    }

    Filter filter = Filter.and(Filter.equals(COL_SERVICE_OBJECT, objId),
        Filter.isNull(COL_MAINTENANCE_DEFECT));

    Queries.getRowSet(VIEW_MAINTENANCE, null, filter, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (DataUtils.isEmpty(result)) {
          form.notifyInfo(Localized.getConstants().noData());
        } else {
          selectItems(objId, result);
        }
      }
    });
  }

  private static void buildHeader(long objId, final BeeRowSet items) {
    Queries.getRow(VIEW_SERVICE_OBJECTS, objId, new RowCallback() {
      @Override
      public void onSuccess(BeeRow objRow) {
        DataInfo dfInfo = Data.getDataInfo(VIEW_SERVICE_DEFECTS);
        BeeRow dfRow = RowFactory.createEmptyRow(dfInfo, true);
        
        dfRow.setValue(dfInfo.getColumnIndex(COL_SERVICE_OBJECT), objRow.getId());

        Long customer = Data.getLong(VIEW_SERVICE_OBJECTS, objRow, COL_SERVICE_CUSTOMER);
        if (DataUtils.isId(customer)) {
          dfRow.setValue(dfInfo.getColumnIndex(COL_SERVICE_CUSTOMER), customer);
          dfRow.setValue(dfInfo.getColumnIndex(ALS_SERVICE_CUSTOMER_NAME),
              Data.getString(VIEW_SERVICE_OBJECTS, objRow, ALS_SERVICE_CUSTOMER_NAME));
        }

        UserData userData = BeeKeeper.getUser().getUserData();
        if (userData != null) {
          dfRow.setValue(dfInfo.getColumnIndex(COL_DEFECT_MANAGER), userData.getUserId());
          RelationUtils.setUserFields(dfInfo, dfRow, COL_DEFECT_MANAGER, userData);

          dfRow.setValue(dfInfo.getColumnIndex(COL_DEFECT_SUPPLIER), userData.getCompany());
          dfRow.setValue(dfInfo.getColumnIndex(ALS_DEFECT_SUPPLIER_NAME),
              userData.getCompanyName());
        }

        for (BeeRow row : items) {
          Long currency = row.getLong(items.getColumnIndex(AdministrationConstants.COL_CURRENCY));

          if (DataUtils.isId(currency)) {
            dfRow.setValue(dfInfo.getColumnIndex(AdministrationConstants.COL_CURRENCY), currency);
            dfRow.setValue(dfInfo.getColumnIndex(AdministrationConstants.ALS_CURRENCY_NAME),
                row.getString(items.getColumnIndex(AdministrationConstants.ALS_CURRENCY_NAME)));
            break;
          }
        }

        RowFactory.createRow("NewServiceDefect", null, dfInfo, dfRow, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            ParameterList params = ServiceKeeper.createArgs(SVC_CREATE_DEFECT_ITEMS);

            final long dfId = result.getId();
            params.addQueryItem(COL_DEFECT, dfId);

            Long currency = Data.getLong(VIEW_SERVICE_DEFECTS, result,
                AdministrationConstants.COL_CURRENCY);
            if (DataUtils.isId(currency)) {
              params.addQueryItem(AdministrationConstants.COL_CURRENCY, currency);
            }

            List<Long> ids = Lists.newArrayList();
            for (IsRow item : items) {
              ids.add(item.getId());
            }

            params.addDataItem(VIEW_MAINTENANCE, DataUtils.buildIdList(ids));

            BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
              @Override
              public void onResponse(ResponseObject response) {
                response.notify(BeeKeeper.getScreen());

                if (!response.hasErrors()) {
                  DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_MAINTENANCE);
                  DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_SERVICE_OBJECTS);

                  RowEditor.openRow(VIEW_SERVICE_DEFECTS, dfId, true, null);
                }
              }
            });
          }
        });
      }
    });
  }

  private static void selectItems(final long objId, final BeeRowSet data) {
    final HtmlTable table = new HtmlTable(STYLE_PREFIX + "table");
    int r = 0;

    int dateIndex = data.getColumnIndex(COL_MAINTENANCE_DATE);
    int itemNameIndex = data.getColumnIndex(ALS_MAINTENANCE_ITEM_NAME);
    int quantityIndex = data.getColumnIndex(TradeConstants.COL_TRADE_ITEM_QUANTITY);
    int priceIndex = data.getColumnIndex(TradeConstants.COL_TRADE_ITEM_PRICE);
    int currencyNameIndex = data.getColumnIndex(AdministrationConstants.ALS_CURRENCY_NAME);

    TotalRenderer totalRenderer = new TotalRenderer(data.getColumns());

    NumberFormat priceFormat = Format.getDefaultCurrencyFormat();
    NumberFormat amountFormat = Format.getDefaultCurrencyFormat();

    for (IsRow row : data) {
      DateTime date = row.getDateTime(dateIndex);
      String itemName = row.getString(itemNameIndex);
      Double quantity = row.getDouble(quantityIndex);
      Double price = row.getDouble(priceIndex);
      String currencyName = row.getString(currencyNameIndex);

      int c = 0;

      SimpleCheckBox checkBox = new SimpleCheckBox(true);
      table.setWidgetAndStyle(r, c++, checkBox, STYLE_PREFIX + "check");

      Label dateLabel = new Label();
      if (date != null) {
        dateLabel.setText(date.toCompactString());
      }
      table.setWidgetAndStyle(r, c++, dateLabel, STYLE_PREFIX + "date");

      Label itemLabel = new Label(itemName);
      table.setWidgetAndStyle(r, c++, itemLabel, STYLE_PREFIX + "item");

      Label quantityLabel = new Label();
      if (quantity != null) {
        quantityLabel.setText(BeeUtils.toString(quantity));
      }
      table.setWidgetAndStyle(r, c++, quantityLabel, STYLE_PREFIX + "quantity");

      Label priceLabel = new Label();
      if (price != null) {
        priceLabel.setText(priceFormat.format(price));
      }
      table.setWidgetAndStyle(r, c++, priceLabel, STYLE_PREFIX + "price");

      Label currencyLabel = new Label(currencyName);
      table.setWidgetAndStyle(r, c++, currencyLabel, STYLE_PREFIX + "currency");

      Label amountLabel = new Label();
      if (quantity != null && price != null) {
        Double amount = totalRenderer.getTotal(row);
        if (amount != null) {
          amountLabel.setText(amountFormat.format(amount));
        }
      }
      table.setWidgetAndStyle(r, c++, amountLabel, STYLE_PREFIX + "amount");

      r++;
    }

    Simple wrapper = new Simple(table);
    wrapper.addStyleName(STYLE_PREFIX + "wrapper");

    Flow panel = new Flow(STYLE_PREFIX + "panel");
    panel.add(wrapper);

    Flow commands = new Flow(STYLE_PREFIX + "commands");

    final DialogBox dialog = DialogBox.create(Localized.getConstants().svcNewDefect(),
        STYLE_PREFIX + "dialog");

    Button build = new Button(Localized.getConstants().actionCreate());
    build.addStyleName(STYLE_PREFIX + "build");
    build.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        List<BeeRow> selectedRows = Lists.newArrayList();

        for (int i = 0; i < data.getNumberOfRows(); i++) {
          Widget widget = table.getWidget(i, 0);
          if (widget instanceof HasCheckedness && ((HasCheckedness) widget).isChecked()) {
            selectedRows.add(data.getRow(i));
          }
        }

        if (selectedRows.isEmpty()) {
          BeeKeeper.getScreen().notifyWarning(Localized.getConstants().selectAtLeastOneRow());

        } else {
          dialog.close();

          BeeRowSet dfItems = new BeeRowSet(data.getViewName(), data.getColumns(), selectedRows);
          buildHeader(objId, dfItems);
        }
      }
    });

    Button cancel = new Button(Localized.getConstants().cancel());
    cancel.addStyleName(STYLE_PREFIX + "cancel");
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

  private DefectBuilder() {
  }
}
