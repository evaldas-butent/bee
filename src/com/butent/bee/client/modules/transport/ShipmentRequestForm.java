package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.modules.commons.CommonsUtils;
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
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.CargoRequestStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;
import java.util.Map;

class ShipmentRequestForm extends AbstractFormInterceptor {

  private Button activateCommand;
  private Button blockCommand;

  ShipmentRequestForm() {
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    CargoRequestStatus status;
    if (DataUtils.hasId(row)) {
      status = EnumUtils.getEnumByIndex(CargoRequestStatus.class,
          Data.getInteger(getViewName(), row, COL_QUERY_STATUS));
    } else {
      status = null;
    }

    refreshCommands(status);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ShipmentRequestForm();
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    SelfServiceUtils.setDefaultExpeditionType(form, newRow, COL_QUERY_EXPEDITION);
    SelfServiceUtils.setDefaultShippingTerm(form, newRow, COL_CARGO_SHIPPING_TERM);

    super.onStartNewRow(form, oldRow, newRow);
  }

  private void onBlock() {
    String host = getDataValue(COL_QUERY_HOST);
    if (BeeUtils.isEmpty(host)) {
      return;
    }

    String caption = Localized.getConstants().trCommandBlockIpAddress();
    CommonsUtils.blockHost(caption, host, getFormView(), new Callback<String>() {
      @Override
      public void onSuccess(String result) {
        if (getFormView().isInteractive()) {
          getHeaderView().clearCommandPanel();
        }
        updateStatus(CargoRequestStatus.REJECTED);
      }
    });
  }

  private void onCreateOrder() {
    Global.confirm(Localized.getConstants().trConfirmCreateNewOrder(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        String companyName = getDataValue(COL_QUERY_CUSTOMER_NAME);
        if (BeeUtils.isEmpty(companyName)) {
          return;
        }

        Map<String, String> parameters = Maps.newHashMap();
        parameters.put(CommonsConstants.COL_COMPANY_NAME, companyName);

        putField(parameters, COL_QUERY_CUSTOMER_CODE, CommonsConstants.COL_COMPANY_CODE);
        putField(parameters, COL_QUERY_CUSTOMER_VAT_CODE, CommonsConstants.COL_COMPANY_VAT_CODE);
        putField(parameters, COL_QUERY_CUSTOMER_EXCHANGE_CODE,
            CommonsConstants.COL_COMPANY_EXCHANGE_CODE);

        putField(parameters, COL_QUERY_CUSTOMER_EMAIL, CommonsConstants.COL_EMAIL);
        putField(parameters, COL_QUERY_CUSTOMER_ADDRESS, CommonsConstants.COL_ADDRESS);
        putField(parameters, COL_QUERY_CUSTOMER_PHONE, CommonsConstants.COL_PHONE);

        CommonsUtils.createCompany(parameters, getFormView(), new IdCallback() {
          @Override
          public void onSuccess(Long company) {
            List<BeeColumn> columns = Data.getColumns(VIEW_ORDERS,
                Lists.newArrayList(COL_CUSTOMER));

            Queries.insert(VIEW_ORDERS, columns, Queries.asList(company), null, new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                getActiveRow().setValue(getDataIndex(COL_ORDER), result.getId());

                CargoRequestStatus status = CargoRequestStatus.ACTIVE;
                updateStatus(status);
                refreshCommands(status);
                
                DataChangeEvent.fireRefresh(VIEW_ORDERS);
              }
            });
          }
        });
      }
    });
  }

  private void putField(Map<String, String> parameters, String source, String destination) {
    String value = getDataValue(source);
    if (!BeeUtils.isEmpty(value)) {
      parameters.put(destination, value.trim());
    }
  }

  private void refreshCommands(CargoRequestStatus status) {
    HeaderView header = getHeaderView();
    if (header == null) {
      return;
    }

    if (header.hasCommands()) {
      header.clearCommandPanel();
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

      if (!BeeUtils.isEmpty(getDataValue(COL_QUERY_HOST))) {
        if (this.blockCommand == null) {
          this.blockCommand =
              new Button(Localized.getConstants().trCommandBlockIpAddress(), new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  onBlock();
                }
              });
        }
        header.addCommandItem(this.blockCommand);
      }
    }
  }

  private void updateStatus(CargoRequestStatus status) {
    SelfServiceUtils.updateStatus(getFormView(), COL_QUERY_STATUS, status);
  }
}
