package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
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

  CargoRequestForm() {
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    CargoRequestStatus status;

    if (DataUtils.hasId(row)) {
      status = EnumUtils.getEnumByIndex(CargoRequestStatus.class,
          Data.getInteger(getViewName(), row, COL_CARGO_REQUEST_STATUS));
    
    } else {
      status = null;

      Widget tWidget = form.getWidgetByName("Template");
      if (tWidget instanceof Editor) {
        ((Editor) tWidget).clearValue();
      }
    }

    refreshCommands(status);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoRequestForm();
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    SelfServiceUtils.setDefaultExpeditionType(form, newRow, COL_CARGO_REQUEST_EXPEDITION);
    SelfServiceUtils.setDefaultShippingTerm(form, newRow, COL_CARGO_SHIPPING_TERM);

    super.onStartNewRow(form, oldRow, newRow);
  }

  private void onCreateOrder() {
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

    if (status == null) {
      return;
    }

    if (status == CargoRequestStatus.NEW) {
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
