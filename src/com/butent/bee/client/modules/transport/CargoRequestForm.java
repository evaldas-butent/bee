package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.transport.TransportConstants.CargoRequestStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;

class CargoRequestForm extends AbstractFormInterceptor {

  private Button activateCommand;
  private Button templateCommand;

  private FileCollector collector;

  CargoRequestForm() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof FileCollector) {
      this.collector = (FileCollector) widget;
      this.collector.bindDnd(getFormView());
    }
  }

  @Override
  public void afterInsertRow(IsRow result) {
    // if (getCollector() != null && !getCollector().isEmpty()) {
    // SelfServiceUtils.sendFiles(result.getId(), getCollector().getFiles(), null);
    // }

    super.afterInsertRow(result);
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    CargoRequestStatus status;

    if (DataUtils.hasId(row)) {
      status = EnumUtils.getEnumByIndex(CargoRequestStatus.class,
          Data.getInteger(getViewName(), row, COL_CARGO_REQUEST_STATUS));

    } else {
      status = null;
    }

    refreshCommands(status);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoRequestForm();
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    Widget tWidget = form.getWidgetByName("Template");
    if (tWidget instanceof Editor) {
      ((Editor) tWidget).clearValue();
    }

    if (getCollector() != null) {
      getCollector().clear();
    }

    SelfServiceUtils.setDefaultExpeditionType(form, newRow, COL_CARGO_REQUEST_EXPEDITION);
    SelfServiceUtils.setDefaultShippingTerm(form, newRow, COL_CARGO_SHIPPING_TERM);

    super.onStartNewRow(form, oldRow, newRow);
  }

  private FileCollector getCollector() {
    return collector;
  }

  private void onCreateOrder() {
    Global.confirm(Localized.getConstants().trConfirmCreateNewOrder(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        String company = getDataValue(ALS_REQUEST_CUSTOMER_COMPANY);
        if (!DataUtils.isId(BeeUtils.toLongOrNull(company))) {
          return;
        }

        List<String> colNames = Lists.newArrayList(COL_CUSTOMER);
        List<String> values = Lists.newArrayList(company);

        String manager = getDataValue(COL_CARGO_REQUEST_MANAGER);
        if (!BeeUtils.isEmpty(manager)) {
          colNames.add(COL_ORDER_MANAGER);
          values.add(manager);
        }

        List<BeeColumn> columns = Data.getColumns(VIEW_ORDERS, colNames);

        Queries.insert(VIEW_ORDERS, columns, values, null, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            // getActiveRow().setValue(getDataIndex(COL_ORDER), result.getId());
            //
            // CargoRequestStatus status = CargoRequestStatus.ACTIVE;
            // SelfServiceUtils.updateStatus(getFormView(), COL_CARGO_REQUEST_STATUS, status);
            // refreshCommands(status);
            //
            DataChangeEvent.fireRefresh(VIEW_ORDERS);
          }
        });
      }
    });
  }

  private void onSaveAsTemplate() {
    DataInfo tInfo = Data.getDataInfo(VIEW_CARGO_REQUEST_TEMPLATES);
    if (tInfo == null) {
      return;
    }

    IsRow rRow = getActiveRow();
    if (rRow == null) {
      return;
    }
    BeeRow tRow = RowFactory.createEmptyRow(tInfo, false);

    List<BeeColumn> rColumns = getFormView().getDataColumns();
    for (int tIndex = 0; tIndex < tInfo.getColumnCount(); tIndex++) {
      int rIndex = DataUtils.getColumnIndex(tInfo.getColumns().get(tIndex).getId(), rColumns);
      if (rIndex >= 0) {
        String value = rRow.getString(rIndex);
        if (!BeeUtils.isEmpty(value)) {
          tRow.setValue(tIndex, value);
        }
      }
    }

    RowFactory.createRow(tInfo, tRow, new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        DataChangeEvent.fireRefresh(VIEW_CARGO_REQUEST_TEMPLATES);
      }
    });
  }

  private void refreshCommands(CargoRequestStatus status) {
    HeaderView header = getHeaderView();
    if (header == null) {
      return;
    }

    if (header.hasCommands()) {
      header.clearCommandPanel();
    }

    if (status == CargoRequestStatus.NEW && Data.isViewEditable(VIEW_ORDERS)) {
      if (this.activateCommand == null) {
        this.activateCommand =
            new Button(Localized.getConstants().trCommandCreateNewOrder(), new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                onCreateOrder();
              }
            });
      }
      header.addCommandItem(this.activateCommand);
    }

    if (status != null && Data.isViewEditable(VIEW_CARGO_REQUEST_TEMPLATES)) {
      if (this.templateCommand == null) {
        this.templateCommand =
            new Button(Localized.getConstants().trCommandSaveRequestAsTemplate(),
                new ClickHandler() {
                  @Override
                  public void onClick(ClickEvent event) {
                    onSaveAsTemplate();
                  }
                });
      }
      header.addCommandItem(this.templateCommand);
    }
  }
}
