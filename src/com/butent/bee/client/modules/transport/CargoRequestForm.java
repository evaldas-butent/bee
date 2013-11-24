package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.transport.TransportConstants.CargoRequestStatus;
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
    int maxLen = Data.getColumnPrecision(VIEW_CARGO_REQUEST_TEMPLATES,
        COL_CARGO_REQUEST_TEMPLATE_NAME);

    Global.inputString(Localized.getConstants().trRequestTemplateNew(),
        Localized.getConstants().trRequestTemplateName(), new StringCallback() {
          @Override
          public void onSuccess(String value) {
            List<BeeColumn> requestColumns = getFormView().getDataColumns();
            List<BeeColumn> templateColumns = Data.getColumns(VIEW_CARGO_REQUEST_TEMPLATES);
            
            IsRow row = getActiveRow();

            List<BeeColumn> columns = Lists.newArrayList();
            List<String> values = Lists.newArrayList();
            
            for (BeeColumn column : templateColumns) {
              if (column.getId().equals(COL_CARGO_REQUEST_TEMPLATE_NAME)) {
                columns.add(column);
                values.add(value);

              } else if (column.isEditable()) {
                int index = DataUtils.getColumnIndex(column.getId(), requestColumns);
                if (index >= 0 && !row.isNull(index)) {
                  columns.add(column);
                  values.add(row.getString(index));
                }
              }
            }
            
            Queries.insert(VIEW_CARGO_REQUEST_TEMPLATES, columns, values, null, new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                DataChangeEvent.fireRefresh(VIEW_CARGO_REQUEST_TEMPLATES);
              }
            });
          }
        }, null, maxLen);
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
