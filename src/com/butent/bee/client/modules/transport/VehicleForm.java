package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.SVC_GET_REPAIRS;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;

public class VehicleForm extends AbstractFormInterceptor implements ClickHandler {

  FaLabel service;

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (service == null) {
      service = new FaLabel(FontAwesome.WRENCH);
      service.setTitle(Localized.getConstants().vehicleRepairs());
      service.addClickHandler(this);
      form.getViewPresenter().getHeader().addCommandItem(service);
    }
    service.setVisible(!BeeUtils.isEmpty(row.getString(form
        .getDataIndex(ClassifierConstants.COL_ITEM_EXTERNAL_CODE))));
    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new VehicleForm();
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    service.setEnabled(false);
    ParameterList args = TransportHandler.createArgs(SVC_GET_REPAIRS);
    args.addDataItem(ClassifierConstants.COL_ITEM_EXTERNAL_CODE,
        getStringValue(ClassifierConstants.COL_ITEM_EXTERNAL_CODE));

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        service.setEnabled(true);
        response.notify(getFormView());

        if (response.hasErrors()) {
          return;
        }
        SimpleRowSet simple = SimpleRowSet.restore(response.getResponseAsString());

        if (DataUtils.isEmpty(simple)) {
          getFormView().notifyInfo(Localized.getConstants().noData());
        } else {
          BeeRowSet rs = new BeeRowSet();

          for (String col : simple.getColumnNames()) {
            rs.addColumn(ValueType.TEXT, BeeUtils.proper(col, BeeConst.CHAR_UNDER));
          }
          int c = 0;

          for (SimpleRow row : simple) {
            rs.addRow(++c, row.getValues());
          }
          Global.showModalGrid(Localized.getConstants().vehicleRepairs(), rs);
        }
      }
    });
  }
}
